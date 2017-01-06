import com.google.common.cache.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Will2 on 10/22/2016.
 */
public class DocUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocUtil.class);

    private static final ColumnMapRowMapper mapper = new ColumnMapRowMapper();

    /**
     * Represents a document and a version id.
     */
    public static class DocKey{
        /**
         * The unique identifier for the document
         */
        public String id;

        /**
         * The document's version number
         */
        public String version;

        /**
         * Create a new Composite key from Document Id and a Version id.
         * @param id The unique document id
         * @param version The unique version id.
         */
        public DocKey(String id, String version){
            this.id = id;
            this.version = version;
        }
        @Override
        public boolean equals(Object o){
            if(!(o instanceof DocKey)){
                return false;
            }
            if(((DocKey)o).id == this.id && ((DocKey)o).version == this.version){
                return true;
            }
            return false;
        }
        @Override
        public int hashCode(){
            return id.hashCode() + version.hashCode();
        }
        @Override
        public String toString(){
            return id+":"+version;
        }
    }

    /**
     * We want to keep recent index writers in memory and let the old ones die.  This helps that
     * abstraction
     */
    private static final LoadingCache<DocKey, IndexWriter> cache =
            CacheBuilder.newBuilder()
                    .removalListener(new RemovalListener<DocKey, IndexWriter>() {
                        public void onRemoval(RemovalNotification<DocKey, IndexWriter> notification) {
                            if(notification.getCause() == RemovalCause.EXPIRED){
                                try {
                                    notification.getValue().close();
                                } catch (IOException e) {
                                    String key = notification.getKey().toString();
                                    LOGGER.error("Error closing indexer on cache removal of key "+key, e);
                                }
                            }
                        }
                    })
                    .expireAfterAccess(3, TimeUnit.DAYS)
                    .build(new CacheLoader<DocKey,IndexWriter>(){
                        @Override
                        public IndexWriter load(DocKey key) throws Exception {
                            String dirLoc = key.id + File.separator + key.version;
                            Directory dir = FSDirectory.open(new File(dirLoc).toPath());
                            return new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
                        }
                    });


    /**
     * Get the writer via our adapater.  This just simplifies the API and prevents user's from trying to close
     * our writer that we purposefully keep in memory.
     * @param id The document Id
     * @param version The document's version id.
     * @return
     * @throws IOException
     * @throws ExecutionException
     */
    public static WriterDelegate getWriter(String id, String version) throws IOException, ExecutionException {

        //store a writer if necessary
        IndexWriter writer = cache.get(new DocKey(id,version));

        return new WriterDelegate(writer, id, version);
    }

    public static IndexReader getReader(String id, String version) throws IOException, ExecutionException {
        return getWriter(id, version).reader();
    }

    public static IndexSearcher getSearcher(String id, String version) throws IOException, ExecutionException {
        return new IndexSearcher(getReader(id, version));
    }

    public static IndexSearcher getSearcherByKeys(Set<DocKey> docs) throws IOException, ExecutionException {
        List<IndexReader> readers = new ArrayList<>();
        for(DocKey key : docs){
            readers.add(getReader(key.id, key.version));
        }

        IndexReader[] arrReaders = readers.toArray(new IndexReader[readers.size()]);
        IndexReader all = new MultiReader(arrReaders);
        return new IndexSearcher(all);
    }

    public static IndexSearcher getSearcherByIds(Set<String> ids) throws IOException, ExecutionException {
        List<IndexReader> readers = new ArrayList<>();
        for(Map.Entry<DocKey, IndexWriter> entry: cache.asMap().entrySet()){
            DocKey key = entry.getKey();
            if(ids.contains(key.id)){
                readers.add(getReader(key.id,key.version));
            }
        }

        IndexReader[] arrReaders = readers.toArray(new IndexReader[readers.size()]);
        IndexReader all = new MultiReader(arrReaders);
        return new IndexSearcher(all);
    }

    /**
     * Multithread the search
     * @param ids Set of document IDs to search all versions of
     * @param pool A thread pool to submit Callables to
     * @param docsPerSubmit Every index is a folder (which is a Document) holds each row of a document.  So this is how many indexes (Folders) to search per thread.
     * @param query The query to execute.
     * @param docsPerShard How many rows you want returned per query.  But the query is executed across {@param docsPerSubmit} indexes.
     * @return
     * @throws IOException
     * @throws ExecutionException
     */
    public static List<Future<TopDocs>> searchByIds(Set<String> ids, ExecutorService pool, int docsPerSubmit, final Query query, final int docsPerShard) throws IOException, ExecutionException {
        final List<Future<TopDocs>> results = new ArrayList<>();

        List<IndexReader> readers = new ArrayList<>();

        int cnt = 0;
        for(Map.Entry<DocKey, IndexWriter> entry: cache.asMap().entrySet()){
            DocKey key = entry.getKey();
            if(ids.contains(key.id)){
                readers.add(getReader(key.id,key.version));
            }

            //submit to the pool
            if(cnt>0 && cnt%docsPerSubmit == 0){
                final IndexSearcher searcher = readersToSearchers(readers);

                Callable<TopDocs> runner = new Callable<TopDocs>() {
                    @Override
                    public TopDocs call() throws Exception {
                        try {
                            return searcher.search(query, docsPerShard);

                        } catch (IOException e) {
                            //TODO: add the doc ids that failed to this message
                            LOGGER.error("Shard search thread failed", e);
                            throw new RuntimeException(e);
                        }
                    }
                };

                readers.clear();
                results.add(pool.submit(runner));
            }
        }

        return results;
    }

    /**
     * Find first row from every version that matches the provided query.
     * @param id
     * @param shardSize
     * @param pool
     * @param query
     * @return
     * @throws IOException
     * @throws ExecutionException
     */
    public static List<Future<TopDocs>> searchByIdSimple(String id, int shardSize, ExecutorService pool, final Query query) throws IOException, ExecutionException {
        final List<Future<TopDocs>> results = new ArrayList<>();
        List<IndexReader> readers = new ArrayList<>();

        for(Map.Entry<DocKey, IndexWriter> entry: cache.asMap().entrySet()) {
            DocKey key = entry.getKey();
            if (id != key.id) {
                continue;
            }

            readers.add(getReader(key.id, key.version));

            if(readers.size() == shardSize){
                final IndexSearcher searcher = readersToSearchers(readers);
                readers.clear();

                Callable<TopDocs> runner = new Callable<TopDocs>() {
                    @Override
                    public TopDocs call() throws Exception {
                        try {
                            return searcher.search(query, 1);

                        } catch (IOException e) {
                            //TODO: add the doc ids that failed to this message
                            LOGGER.error("Shard search thread failed", e);
                            throw new RuntimeException(e);
                        }
                    }
                };

                results.add(pool.submit(runner));
            }
        }

        return results;
    }

    /**
     * Search all documents in Set of IDs and first first version that matches.  Potentially searching all versions. This will
     * return the first match.
     * @param ids The unique document IDs to search.  All version of document ID indexes will be searched.
     * @param query The query to be ran.
     * @param pool The executor service that searches will be submitted to.
     * @see DocUtil.SearchRunner
     * @throws IOException
     */
    public static List<Future<TopDocs>> searchByIds(Set<String> ids, final Query query, ExecutorService pool) throws IOException {
        List<Future<TopDocs>> result = new ArrayList<>();
        Map<String,Set<IndexWriter>> allIds = new HashMap<>();

        //find all the IndexWriter that exist for the requested doc IDs
        for(Map.Entry<DocKey, IndexWriter> entry: cache.asMap().entrySet()) {
            String id = entry.getKey().id;
            if(ids.contains(id)){
                if(allIds.get(id) == null){
                    allIds.put(id, new HashSet<IndexWriter>());
                }
                allIds.get(id).add(entry.getValue());
            }
        }

        //search all versions of the matched document IDs
        for(Map.Entry<String, Set<IndexWriter>> entry : allIds.entrySet()){
            int writerCount = entry.getValue().size();
            IndexReader[] readers = new IndexReader[writerCount];
            int i = 0;
            for(IndexWriter writer : entry.getValue()){
                readers[i++] =  DirectoryReader.open(writer);
            }

            IndexSearcher searcher = new IndexSearcher(new MultiReader(readers));
            SearchRunner runner = new SearchRunner(searcher, query, 1);
            result.add(pool.submit(runner));
        }

        return result;
    }

    private static IndexSearcher readersToSearchers(List<IndexReader> readers) throws IOException {
        IndexReader[] arrReaders = readers.toArray(new IndexReader[readers.size()]);
        IndexReader all = new MultiReader(arrReaders);
        return new IndexSearcher(all);
    }

    public static Document create(ResultSet row) throws SQLException {
        Map<String,Object> rowMap = mapper.mapRow(row, 0);

        return create(rowMap);
    }

    public static Document create(Map<String,?> data){
        Document doc = new Document();

        for(Map.Entry<String,?> entry : data.entrySet()) {
            doc.add(createField(entry.getKey(), entry.getValue()));
        }

        return doc;
    }

    private static IndexableField createField(String name, Object obj){
        IndexableField field;

        if(obj instanceof String){
            field = new TextField(name,(String)obj, Field.Store.YES);
        }else if(obj instanceof Number){
            long val = ((Number) obj).longValue();
            field = new NumericDocValuesField(name, val);
        }else{
            field = new TextField(name, obj.toString(), Field.Store.NO);
        }

        return field;
    }

    private static class SearchRunner implements Callable<TopDocs>{
        private final IndexSearcher searcher;
        private final Query query;
        private final int docCount;
        public SearchRunner(IndexSearcher searcher, Query query, int docCount){
            this.searcher = searcher;
            this.query = query;
            this.docCount = docCount;
        }
        @Override
        public TopDocs call() throws Exception {
            try {
                return searcher.search(query, docCount);

            } catch (IOException e) {
                //TODO: add the doc ids that failed to this message
                LOGGER.error("Shard search thread failed", e);
                throw new RuntimeException(e);
            }
        }
    }

}

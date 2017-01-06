import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;

/**
 * Created by Will2 on 10/22/2016.
 */
public final class WriterDelegate {
    private static final String DOCID = "id";
    private static final String VERSION = "versionId";

    private final IndexWriter writer;
    private final String id;
    private final String version;

    public WriterDelegate(IndexWriter writer, String id, String version){
        this.writer = writer;
        this.id = id;
        this.version = version;
    }

    /**
     * Add a document, a row basically, from the result set.  Do not call commit after every add, only call
     * commit after all documents with result set have been added.  This will modify the document adding 2
     * additional fields an "id" field and a "version" field.
     * @param doc
     * @return
     * @throws IOException
     */
    public void addDocument(Document doc) throws IOException {
        doc.add(new StringField(DOCID, id, Field.Store.YES));
        doc.add(new StringField(VERSION, version, Field.Store.YES));
        writer.addDocument(doc);
    }

    /**
     * Persist data to disk.
     * @return
     * @throws IOException
     */
    public void commit() throws IOException {
        writer.commit();
    }

    public void rollback() throws IOException {
        writer.rollback();
    }

    public void deleteAll() throws IOException {
        writer.deleteAll();
    }

    public IndexReader reader() throws IOException {
        return DirectoryReader.open(writer);
    }
}

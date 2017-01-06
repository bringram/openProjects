import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.*;

/**
 * Created by Will2 on 10/22/2016.
 */
public class DocUtilTest {
    @BeforeClass
    public static void init() throws IOException, ExecutionException {
        Map<String,String> row = new HashMap<>();
        row.put("one","hello");
        row.put("two", "world");

        Document doc = DocUtil.create(row);
        WriterDelegate writer = DocUtil.getWriter("1", "1");
        writer.addDocument(doc);
    }

    @Test
    public void verifyCreateAndRead() throws IOException, ExecutionException {

        IndexReader reader = DocUtil.getReader("1", "1");

        assertNotNull(reader);

        assertEquals(1, reader.numDocs());
    }

    @Test
    public void verifySearch() throws IOException, ExecutionException, ParseException {


        Analyzer analyzer = new StandardAnalyzer();
        Query query = new QueryParser("one",analyzer).parse("hel*");
        IndexSearcher searcher = DocUtil.getSearcher("1", "1");
        TopDocs result = searcher.search(query, 10);

        assertEquals(1, result.totalHits);
    }

    //verify that Directory do not search child directories.
    @Test
    public void testDirectories() throws IOException, ExecutionException {

        //now lets see if we get that document?
        String dirLoc = "1";
        Directory dir = FSDirectory.open(new File(dirLoc).toPath());
        IndexWriter writer2 = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
        IndexReader reader = DirectoryReader.open(writer2);

        assertEquals(0, reader.numDocs());
    }
}

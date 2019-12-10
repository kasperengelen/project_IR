import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Part of the class was inspired by the following source:
 *    https://lucene.apache.org/core/5_4_0/demo/src-html/org/apache/lucene/demo/IndexFiles.html
 *    https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
 */
public class Searcher
{
    /**
     * A class that contains information about a search result.
     */
    public static class SearchResult
    {
        /**
         * A list of filenames of the top results.
         */
        public List<String> topResultFilenames = new ArrayList<>();

        /**
         * The requested amount of results.
         */
        int requestedTop;

        /**
         * The total amount of results. This can be either the exact value or it is a lower bound.
         *
         * Note: this is stored as an integer since the total amount of documents in the index fits in
         * an integer.
         */
        int totalResultCount;

        /**
         * True if the total result count is exact, false if it is a lower bound.
         */
        boolean totalCountIsExact;
    }

    public static SearchResult search(String querystring, int top_count, Path index_path) throws IOException, ParseException
    {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(index_path));
        IndexSearcher searcher = new IndexSearcher(reader);

        // list here all the fields that are searched
        String[] fields = {
                DocumentIndexer.FieldNames.TITLE,
                DocumentIndexer.FieldNames.QUESTION,
                DocumentIndexer.FieldNames.TAGS,
                DocumentIndexer.FieldNames.ANSWER,
                DocumentIndexer.FieldNames.FILENAME // useful to find a certain document
        };

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        Query query = parser.parse(querystring);

        TopDocs results = searcher.search(query, top_count);
        ScoreDoc[] hits = results.scoreDocs;

        SearchResult retval = new SearchResult();

        retval.requestedTop = top_count;
        retval.totalResultCount = Math.toIntExact(results.totalHits.value);
        retval.totalCountIsExact = results.totalHits.relation == TotalHits.Relation.EQUAL_TO;

        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);

            String filename = doc.get("filename");
            retval.topResultFilenames.add(filename);
        }

        return retval;
    }
}

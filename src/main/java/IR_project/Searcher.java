package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
         * A list of document identifiers of the top results.
         */
        public List<String> topResultIDs = new ArrayList<>();

        /**
         * A map that maps filenames of the top results list, to their score.
         */
        public Map<String, Float> scores = new HashMap<>();

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

    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private QueryBuilder builder;

    public Searcher() {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Constants.PATH_INDEX));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(Constants.DEFAULT_SIM);
            analyzer = Utils.getAnalyzer();
            builder = new QueryBuilder(analyzer);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Searcher(Path index, Similarity sim) {
        try {
            reader = DirectoryReader.open(FSDirectory.open(index));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(sim);
            analyzer = Utils.getAnalyzer();
            builder = new QueryBuilder(analyzer);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public SearchResult search(String querystring, int top_count) throws IOException
    {

        Query query = builder.createBooleanQuery("body", querystring);

        TopDocs results = searcher.search(query, top_count);
        ScoreDoc[] hits = results.scoreDocs;

        SearchResult retval = new SearchResult();

        retval.requestedTop = top_count;
        retval.totalResultCount = Math.toIntExact(results.totalHits.value);
        retval.totalCountIsExact = results.totalHits.relation == TotalHits.Relation.EQUAL_TO;

        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);

            String docid = doc.get(Constants.FieldNames.IDENTIFIER);
            retval.topResultIDs.add(docid);
            retval.scores.put(docid, hit.score);
        }

        return retval;
    }
}

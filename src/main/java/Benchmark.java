import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that performs a benchmark of the retrieval performance.
 */
public class Benchmark
{
    /**
     * Class that contains the results of the benchmark.
     */
    public static class BenchmarkResult
    {
        public float precision;
        public float recall;
        // TODO add more attributes
    }

    /**
     * Perform the benchmark.
     *
     * @return The result of the benchmark.
     */
    public static BenchmarkResult doBenchmark(boolean do_log, boolean print_results_immediately) throws Exception
    {
//        // open index
//        IndexReader reader = DirectoryReader.open(FSDirectory.open(Constants.PATH_INDEX));
//        IndexSearcher searcher = new IndexSearcher(reader);
//
//        // prepare queries
//        QualityQuery[] queries = M_getQueries();
//        // TODO what parameter for this constructor?
//        QualityQueryParser parser = new SimpleQQParser();
//
//        // create logger
//        PrintWriter logger = (do_log) ? new PrintWriter(System.out) : null;
//
//        // prepare return value
//        BenchmarkResult retval = new BenchmarkResult();
//
//        // iterate over similarities and benchmark each one
//        for(Similarity sim : M_getSimilarities())
//        {
//            if(logger != null) {
//                logger.println(String.format("Starting benchmark for Similarity implementation '%s'", sim.getClass().toString()));
//            }
//
//            // prepare benchmarker
//            searcher.setSimilarity(sim);
//            QualityBenchmark benchmarker = new QualityBenchmark(queries, parser, searcher, Indexer.FieldNames.FILENAME);
//
//            // prepare judge, logger, etc
//            Judge judge = new BenchmarkJudge();
//            SubmissionReport rep = new SubmissionReport(logger, "SIM_" + sim.getClass().toString());
//
//            // run benchmark
//            QualityStats[] results = benchmarker.execute(judge, rep, logger);
//
//            // process results
//            QualityStats avg = QualityStats.average(results);
//
//            // TODO process results
//
//            // TODO print results immediately if needed
//        }
//
//        if(logger != null) {
//            logger.flush();
//            logger.close();
//        }
//
//        return retval;
        return null;
    }

    /**
     * Retrieve a list of similarities that need to be evaluated.
     */
    private static List<Similarity> M_getSimilarities()
    {
        return new ArrayList<>();
    }

    /**
     * Retrieve a list of queries that will be used to measure performance.
     */
    private static QualityQuery[] M_getQueries()
    {
        return null;
    }

    /**
     * Judge class that is used to evaluate retrieval results.
     */
    public static class BenchmarkJudge implements Judge
    {

        /**
         * Determine whether the document with the specified identifier is relevant for the query.
         * @param docName The name of the document. This is the value contained in the document identifier {@link org.apache.lucene.document.Field}.
         * @param query The query that retrieved the document.
         *
         * @return True if relevant, False if not.
         */
        @Override
        public boolean isRelevant(String docName, QualityQuery query)
        {
            return false;
        }


        @Override
        public boolean validateData(QualityQuery[] qq, PrintWriter logger)
        {
            return false;
        }

        /**
         * Determine the total number of documents that are relevant for the specific query.
         *
         * @param query The query.
         *
         * @return Integer that specifies the amount of documents.
         */
        @Override
        public int maxRecall(QualityQuery query)
        {
            return 0;
        }
    }
}

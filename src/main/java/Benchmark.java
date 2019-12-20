import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class that performs a benchmark of the retrieval performance.
 */
public class Benchmark
{

    StackOverflowJudge judge;

    /**
     * Class that contains the results of the benchmark.
     */
    public static class BenchmarkResult
    {
        public float precision;
        public float recall;
        // TODO add more attributes
    }

    public Benchmark(){
        try {
            judge = new StackOverflowJudge();
            judge.readQueries(Files.newBufferedReader(Paths.get("terms.txt"), StandardCharsets.UTF_8));
            judge.readJudgements(Files.newBufferedReader(Paths.get("sets.txt"), StandardCharsets.UTF_8));
        } catch (IOException e) {
            Logger.logDebug("An error occurred while instantiating benchmark:\n%s", e.toString());
        }

    }

    /**
     * Perform the benchmark.
     *
     * @return The result of the benchmark.
     */
    public BenchmarkResult doBenchmark(boolean do_log, boolean print_results_immediately) throws IOException {
        // open index
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Constants.PATH_INDEX));
        IndexSearcher searcher = new IndexSearcher(reader);

        // prepare queries
        QualityQuery[] queries = M_getQueries();
        // TODO what parameter for this constructor?
        QualityQueryParser parser = new SimpleQQParser("term", "question");

        QualityBenchmark benchmark = new QualityBenchmark(queries, parser, searcher, "identifier");
        try {
            PrintWriter quality_out = new PrintWriter(new File("./quality_log.txt"));
            QualityStats[] result = benchmark.execute(judge, null, quality_out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create logger
//        PrintWriter logger = (do_log) ? new PrintWriter(System.out) : null;

        // prepare return value
//        BenchmarkResult retval = new BenchmarkResult();



        // iterate over similarities and benchmark each one
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
    private QualityQuery[] M_getQueries()
    {
        return judge.queries.toArray(new QualityQuery[judge.queries.size()]);
    }

    public static void main(String[] argv)
    {
        Logger.logDebug("Initializing benchmark");
        Benchmark b = new Benchmark();
        Logger.logDebug("Initialized benchmark");
        Logger.logDebug("Started benchmark");
        try {
            b.doBenchmark(true, true);
        } catch (IOException e) {
            Logger.logDebug(e.toString());
        }
        Logger.logDebug("Completed benchmark");
    }
}

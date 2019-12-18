import org.apache.lucene.benchmark.quality.Judge;
import org.apache.lucene.benchmark.quality.QualityQuery;

import java.io.PrintWriter;

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
    }

    /**
     * Perform the benchmark.
     *
     * @return The result of the benchmark.
     */
    public static BenchmarkResult doBenchmark()
    {
        // foreach sim in similarities (i.e. BM25, Language model, etc)
        //      create indexsearcher
        //      create judge
        //      create benchmark
        //      execute benchmark
        //      analyse results
        //
        // return results

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

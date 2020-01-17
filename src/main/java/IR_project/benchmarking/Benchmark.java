package IR_project.benchmarking;

import IR_project.Logger;
import IR_project.Searcher;
import IR_project.Utils;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;

import java.io.*;
import java.nio.file.Paths;


/**
 * Class that performs a benchmark of the retrieval performance.
 */
public class Benchmark
{

    public static void titleQueryBM(Searcher s, String sim) {
        // Open ground truth
        try {
            BufferedReader br = new BufferedReader(new FileReader("./groundTruth/titleToDoc100K.txt"));

            PrintWriter rank_out = new PrintWriter(new File("./titleQueryRank" + sim + ".txt"));

            String line;
            int count = 1;
            while ((line = br.readLine()) != null) {

                int splitIdx = line.lastIndexOf('|');
                String query = line.substring(0, splitIdx-1);
                String docID = line.substring(splitIdx+2).toLowerCase().replace(".xml", "").toUpperCase();

                Searcher.SearchResult result = s.search(query, 100);
                int rank = result.topResultIDs.indexOf(docID) + 1;
                rank_out.println(rank);

                if (count % 1000 == 0) {
                    Logger.logDebug("Processed %d queries", count);
                    rank_out.flush();
                }
                count++;
            }

            rank_out.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] argv)
    {
        Logger.logDebug("Initializing search engines");
        Searcher s1 = new Searcher(Paths.get("../index/"), new BM25Similarity());
        Searcher s2 = new Searcher(Paths.get("../index1/"), new ClassicSimilarity());
        Searcher s3 = new Searcher(Paths.get("../index2/"), new LMJelinekMercerSimilarity((float) 0.8));
        Searcher s4 = new Searcher(Paths.get("../index3/"),  new LMDirichletSimilarity((float) 0.8));

        Logger.logDebug("Started BM25 benchmark");
        titleQueryBM(s1, "BM25");
        Logger.logDebug("Completed BM25 benchmark");
//
        Logger.logDebug("Started TF-IDF benchmark");
        titleQueryBM(s2, "TD-IDF");
        Logger.logDebug("Completed TF-IDF benchmark");

        Logger.logDebug("Started LM Jelinek-Mercer benchmark");
        titleQueryBM(s3, "LMJM");
        Logger.logDebug("Completed LM Jelinek-Mercer benchmark");

        Logger.logDebug("Started LM Dirichlet benchmark");
        titleQueryBM(s4, "LMD");
        Logger.logDebug("Completed LM Dirichlet benchmark");
    }
}

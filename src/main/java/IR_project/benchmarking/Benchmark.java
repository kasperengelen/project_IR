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
import java.util.Arrays;
import java.util.List;


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

                Searcher.SearchResult result = s.search(query, 20);
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

    public static void titleTermQueryBM(Searcher s, String sim) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("./groundTruth/titleTermQueryDoc.txt"));
            PrintWriter code_out = new PrintWriter(new File("./titleTermQueryResults" + sim + ".txt"));

            String line;
            int count = 1;
            while ((line = br.readLine()) != null) {

                String[] queryAndDocs = line.split(" \\| ");
                String query = queryAndDocs[0];
                List<String> docs = Arrays.asList(queryAndDocs[1].split(" "));
//                String docID = line.substring(splitIdx+2).toLowerCase().replace(".xml", "").toUpperCase();

//                Logger.logDebug(query);
                Searcher.SearchResult result = s.search(query, 20);

                StringBuilder code = new StringBuilder(docs.size() + "|");

                for (String docID : result.topResultIDs) {
                    code.append(docs.contains(docID) ? 1 : 0);
                }

                code_out.println(code);

                if (count % 1000 == 0) {
                    Logger.logDebug("Processed %d queries", count);
                    code_out.flush();
                }
                count++;
            }

            code_out.close();

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
        titleTermQueryBM(s1, "BM25");
        Logger.logDebug("Completed BM25 benchmark");
//
        Logger.logDebug("Started TF-IDF benchmark");
        titleTermQueryBM(s2, "TD-IDF");
        Logger.logDebug("Completed TF-IDF benchmark");

        Logger.logDebug("Started LM Jelinek-Mercer benchmark");
        titleTermQueryBM(s3, "LMJM");
        Logger.logDebug("Completed LM Jelinek-Mercer benchmark");

        Logger.logDebug("Started LM Dirichlet benchmark");
        titleTermQueryBM(s4, "LMD");
        Logger.logDebug("Completed LM Dirichlet benchmark");
    }
}

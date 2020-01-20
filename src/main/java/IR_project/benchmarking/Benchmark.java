package IR_project.benchmarking;

import IR_project.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.BooleanQuery;

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

    public static void rocchioBM(Searcher s, String sim, int feedbackRounds) {
        Rocchio algorithm_utils = new Rocchio(0.30,0.75,0.25,1.2, 1.2, 0.75);

        try {
            BufferedReader br = new BufferedReader(new FileReader("./groundTruth/tagsQueryDoc.txt"));
            PrintWriter code_out = new PrintWriter(new File("./rocchioTagsQueryResults" + sim + ".txt"));

            String line;
            int count = 1;
            while ((line = br.readLine()) != null) {

                String[] queryAndDocs = line.split(" \\| ");
                String query = queryAndDocs[0];
                List<String> docs = Arrays.asList(queryAndDocs[1].split(" "));
                StringBuilder code = new StringBuilder(docs.size() + "|");

                RocchioQuery current_rocchio_query = algorithm_utils.parseQuery(query, Utils.getAnalyzer());

                for (int i = 0; i <= feedbackRounds; ++i) {
                    List<Integer> relevant_set = new ArrayList<>();
                    List<Integer> non_relevant_set = new ArrayList<>();
                    Searcher.SearchResult result = s.search(current_rocchio_query.toLuceneQuery(), 20);
                    for (String docID : result.topResultIDs) {
                        if (docs.contains(docID)) {
                            code.append(1);
                            if (relevant_set.isEmpty())
                            relevant_set.add(result.docIdentifierToInternalId.get(docID));
                        } else {
                            code.append(0);
                        }
                    }
                    current_rocchio_query = algorithm_utils.adjustQuery(
                            current_rocchio_query, relevant_set, non_relevant_set, s.getIndexReader());
                    if (i != feedbackRounds) {
                        code.append(" | ");
                    }
                }

                code_out.println(code);

                if (count % 100 == 0) {
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

        BooleanQuery.setMaxClauseCount(16384);

        Logger.logDebug("Initializing search engines");
        Searcher s1 = new Searcher(Paths.get("../index/"), new BM25Similarity());

        Logger.logDebug("Started BM25 benchmark");
        rocchioBM(s1, "BM25", 1);
        Logger.logDebug("Completed BM25 benchmark");
    }
}

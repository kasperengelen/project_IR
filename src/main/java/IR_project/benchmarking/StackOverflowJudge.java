package IR_project.benchmarking;

import IR_project.Utils;
import org.apache.lucene.benchmark.quality.Judge;
import org.apache.lucene.benchmark.quality.QualityQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Judge class that is used to evaluate retrieval results.
 */
public class StackOverflowJudge implements Judge
{

    private static class QueryJudgement {
        private String queryTerm;
        private HashSet<String> relevantDocs;

        QueryJudgement(String queryTerm) {
            this.queryTerm = queryTerm;
            relevantDocs = new HashSet<>();
        }

        public void addRelevantDoc(String docName) {
            relevantDocs.add(docName);
        }

        boolean isRelevant(String docName) {
            return relevantDocs.contains(docName);
        }

        public int maxRecall() {
            return relevantDocs.size();
        }
    }

    ArrayList<QualityQuery> queries;
    HashMap<String, QueryJudgement> judgements;

    /**
     * Read quality queries
     * @param reader where queries are read from
     * @throws IOException If there is a low-level I/O error.
     */
    public void readQueries(BufferedReader reader) throws IOException {
        queries = new ArrayList<>();
        String line;

        try {
            while (null!=(line=reader.readLine())) {
                line = line.trim();
                if (line.length()==0) {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line);
                String queryTerm = st.nextToken();
                assert !st.hasMoreTokens() : "wrong format: " + line + "  next: " + st.nextToken();
                HashMap<String, String> queryVals = new HashMap<String, String>() {
                    {
                        put("term", queryTerm);
                    }
                };
                queries.add(new QualityQuery(queryTerm, queryVals));
            }
        } finally {
            reader.close();
        }
    }

    /**
     * Read query judgements
     * @param  reader where judgements are read from
     * @throws IOException If there is a low-level I/O error.
     */
    public void readJudgements(BufferedReader reader) throws IOException {
        judgements = new HashMap<>();
        QueryJudgement curr = null;
        String line;

        try {
            while (null!=(line=reader.readLine())) {
                line = line.trim();
                if (line.length()==0) {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line);
                String queryTerm = st.nextToken();
                String docID = st.nextToken();
                assert !st.hasMoreTokens() : "wrong format: " + line + "  next: " + st.nextToken();
                if (curr==null || !curr.queryTerm.equals(queryTerm)) {
                    curr = judgements.get(queryTerm);
                    if (curr==null) {
                        curr = new QueryJudgement(queryTerm);
                        judgements.put(queryTerm,curr);
                    }
                    curr.addRelevantDoc(Utils.getDocumentID(Paths.get(docID)));
                }

            }
        } finally {
            reader.close();
        }
    }

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
        QueryJudgement qrj = judgements.get(query.getQueryID());
        return qrj!=null && qrj.isRelevant(docName);
    }


    @Override
    public boolean validateData(QualityQuery[] qq, PrintWriter logger)
    {
        HashMap<String,QueryJudgement> missingQueries = new HashMap<>(judgements);
        ArrayList<String> missingJudgements = new ArrayList<>();
        for (QualityQuery qualityQuery : qq) {
            String id = qualityQuery.getQueryID();
            if (missingQueries.containsKey(id)) {
                missingQueries.remove(id);
            } else {
                missingJudgements.add(id);
            }
        }
        boolean isValid = true;
        if (missingJudgements.size()>0) {
            isValid = false;
            if (logger!=null) {
                logger.println("WARNING: " + missingJudgements.size() + " queries have no judgments! - ");
                for (String missingJudgement : missingJudgements) {
                    logger.println("   " + missingJudgement);
                }
            }
        }
        if (missingQueries.size()>0) {
            isValid = false;
            if (logger!=null) {
                logger.println("WARNING: "+missingQueries.size()+" judgments match no query! - ");
                for (final String id : missingQueries.keySet()) {
                    logger.println("   "+id);
                }
            }
        }
        return isValid;
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
        QueryJudgement qrj = judgements.get(query.getQueryID());
        if (qrj!=null) {
            return qrj.maxRecall();
        }
        return 0;
    }
}
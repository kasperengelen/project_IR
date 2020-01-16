package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://github.com/uiucGSLIS/ir-tools/blob/master/src/main/java/edu/gslis/lucene/expansion/Rocchio.java
 * // TODO other source
 */
public class Rocchio
{
    /**
     * Adjust the specified query to the set of relevant and irrelevant documents.
     *
     * @param old_query The original query.
     * @param relevant_set A list of documents that have been marked as relevant.
     * @param non_relevant_set A list of documents that have been marked as irrelevant.
     *
     * @return The adjusted query.
     */
    public static Query adjustQuery(Query old_query, double a, List<Document> relevant_set, double b, List<Document> non_relevant_set, double c)
    {
        // retval = a * old_query + b * relevant_set + c * non_relevant_set
        return null;
    }

    /**
     * Temp value that contains information about a document.
     */
    private static class DocStats {
        public final int docId;
        public final long docLength;
        public final Map<String, Long> docFreq;

        public DocStats(int doc_id, long doc_length, Map<String, Long> doc_freq)
        {
            this.docId = doc_id;
            this.docLength = doc_length;
            this.docFreq = doc_freq;
        }
    }

    /**
     * Get a term-frequency map for the specified document.
     *
     * @param doc_id The numerical identifier of a document.
     * @param index_reader The index reader that points to the index.
     */
    private static DocStats M_getTermFreqForDoc(int doc_id, IndexReader index_reader) throws IOException
    {
        Map<String, Long> freq_map = new HashMap<>();

        long doc_length = 0;

        Fields doc_fields = index_reader.getTermVectors(doc_id);
        for(String fieldname : doc_fields)
        {
            Terms terms = doc_fields.terms(fieldname);
            TermsEnum enumerator = terms.iterator();
            while(enumerator.next() != null)
            {
                String term = enumerator.term().utf8ToString();
                long freq = enumerator.totalTermFreq();

                freq_map.put(term, freq);

                doc_length += freq;
            }
        }

        return new DocStats(doc_id, doc_length, freq_map);
    }

    /**
     * Given a term-frequency map, construct a map that gives the BM25 weight for a term.
     */
    private static Map<String, Double> M_termFreqToBM25Weights(Map<String, Long> term_freq_map, double doc_length, double k1, double b, IndexReader reader) throws IOException
    {
        Map<String, Double> retval = new HashMap<>();

        // number of documents in collection
        double N = reader.numDocs();

        // total amount of tokens in collection / number of documents in collection
        double avg_doc_len = reader.getSumTotalTermFreq(Constants.FieldNames.BODY) / N;

        // iterate over terms in this document and determine BM25 weight
        for(Map.Entry<String, Long> entry : term_freq_map.entrySet())
        {
            String term = entry.getKey();
            Long tf     = entry.getValue();

            // number of document that term appears in
            double df = reader.docFreq(new Term(Constants.FieldNames.BODY, term));

            // determine idf score
            double idf = Math.log(N / df);

            // calculate BM25 formula
            double x = (k1 + 1) * tf;
            double y = k1 * ((1-b) + b * (doc_length / avg_doc_len)) + tf;

            // determine final weight
            double weight = idf * (x / y);

            retval.put(term, weight);
        }

        return retval;
    }

    /**
     * Given a query, construct a term-weight map.
     */
    private static Map<String, Double> M_getQueryWeights(Query query)
    {
        return null;
    }

    /**
     * Construct a query based on a set of weighted terms.
     *
     * @param weights A map that gives the weights for terms.
     */
    private static Query M_termWeightsToQuery(Map<String, Double> weights) throws ParseException
    {
        String query_string = "";

        for(Map.Entry<String, Double> entry : weights.entrySet())
        {
            query_string += " " + entry.getKey() + "^" + entry.getValue();
        }

        String[] fields = {
                Constants.FieldNames.BODY,
        };

        Analyzer analyzer = Utils.getAnalyzer();
        QueryParser parser = new MultiFieldQueryParser(fields, analyzer);

        return parser.parse(query_string);
    }
}

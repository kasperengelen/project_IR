package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SOURCES:
 * https://github.com/uiucGSLIS/ir-tools/blob/master/src/main/java/edu/gslis/lucene/expansion/Rocchio.java
 * https://www.baeldung.com/lucene-analyzers
 */
public class Rocchio
{
    private double m_alpha;
    private double m_beta;
    private double m_gamma;

    private double m_BM25k1;
    private double m_BM25k3;
    private double m_BM25b;

    /**
     * Initialise the parameters for Rocchio relevance feedback using BM25 weights.
     *
     * @param alpha The alpha parameter for the Rocchio algorithm.
     * @param beta The alpha parameter for the Rocchio algorithm.
     * @param gamma The alpha parameter for the Rocchio algorithm.
     *
     * @param bm25_k1 The k1 parameter for the BM25 algorithm.
     * @param bm25_k3 The k3 parameter for the BM25 algorithm.
     * @param bm25_b The b parameter for the BM25 algorithm.
     */
    public Rocchio(double alpha, double beta, double gamma, double bm25_k1, double bm25_k3, double bm25_b)
    {
        m_alpha = alpha;
        m_beta = beta;
        m_gamma = gamma;

        m_BM25k1 = bm25_k1;
        m_BM25b = bm25_b;
        m_BM25k3 = bm25_k3;
    }

    /**
     * Adjust the specified query to the set of relevant and irrelevant documents.
     *
     * @param old_query The original query.
     * @param relevant_set A list of document ids that have been marked as relevant.
     * @param non_relevant_set A list of document ids that have been marked as irrelevant.
     *
     * @return The adjusted query.
     */
    public RocchioQuery adjustQuery(RocchioQuery old_query, List<Integer> relevant_set, List<Integer> non_relevant_set, IndexReader reader) throws IOException
    {
        // retval = a * old_query + b * relevant_set + c * non_relevant_set
        Map<String, Double> new_query_weights = new HashMap<>();

        for(Map.Entry<String, Double> entry : old_query.weights().entrySet())
        {
            String term = entry.getKey();

            // retrieve old weight that is currently in the map
            double old_weight = (new_query_weights.containsKey(term)) ? new_query_weights.get(term) : 0;

            // increment old weight with new weight
            new_query_weights.put(term, old_weight + entry.getValue() * m_alpha);
        }

        for(Integer relevant_id : relevant_set) {
            DocStats doc_info = M_getTermFreqForDoc(relevant_id, reader);

            for(Map.Entry<String, Double> entry : M_termFreqToBM25Weights(doc_info.docFreq, doc_info.docLength, reader).entrySet())
            {
                String term = entry.getKey();

                // retrieve old weight that is currently in the map
                double old_weight = (new_query_weights.containsKey(term)) ? new_query_weights.get(term) : 0;

                // increment old weight with new weight
                new_query_weights.put(term, old_weight + entry.getValue() * m_beta);
            }
        }

        for(Integer non_relevant_id : non_relevant_set) {
            DocStats doc_info = M_getTermFreqForDoc(non_relevant_id, reader);

            for(Map.Entry<String, Double> entry : M_termFreqToBM25Weights(doc_info.docFreq, doc_info.docLength, reader).entrySet())
            {
                String term = entry.getKey();

                // retrieve old weight that is currently in the map
                double old_weight = new_query_weights.getOrDefault(term, 0.0);

                // increment old weight with new weight
                new_query_weights.put(term, old_weight + entry.getValue() * m_gamma);
            }
        }

        // filter away all zero-weight terms
        new_query_weights.values().removeIf(f -> f < 0.05f);

        return new RocchioQuery(new_query_weights);
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
    private DocStats M_getTermFreqForDoc(int doc_id, IndexReader index_reader) throws IOException
    {
        Map<String, Long> freq_map = new HashMap<>();

        long doc_length = 0;

        Fields doc_fields = index_reader.getTermVectors(doc_id);
        // TODO this returns null, fix this
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
    private Map<String, Double> M_termFreqToBM25Weights(Map<String, Long> term_freq_map, double doc_length, IndexReader reader) throws IOException
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
            double x = (m_BM25k1 + 1) * tf;
            double y = m_BM25k1 * ((1-m_BM25b) + m_BM25b * (doc_length / avg_doc_len)) + tf;

            // determine final weight
            double weight = idf * (x / y);

            retval.put(term, weight);
        }

        return retval;
    }

    /**
     * Construct a query based on the specified query string. The query string is tokenized and preprocessed by the specified
     * analyzer.
     */
    public RocchioQuery parseQuery(String querystring, Analyzer analyzer) throws IOException
    {
        Map<String, Integer> term_freq = new HashMap<>();

        TokenStream tokens = analyzer.tokenStream(Constants.FieldNames.BODY, querystring);
        CharTermAttribute attr = tokens.addAttribute(CharTermAttribute.class);
        tokens.reset();

        while(tokens.incrementToken())
        {
            String term = attr.toString();

            Integer old_freq = term_freq.getOrDefault(term, 0);

            term_freq.put(term, old_freq + 1);
        }

        Map<String, Double> weights = new HashMap<>();

        for(Map.Entry<String, Integer> entry : term_freq.entrySet())
        {
            double x = (m_BM25k3 + 1) * entry.getValue();

            double y = m_BM25k3 + entry.getValue();

            weights.put(entry.getKey(), x / y);
        }

        return new RocchioQuery(weights);
    }
}

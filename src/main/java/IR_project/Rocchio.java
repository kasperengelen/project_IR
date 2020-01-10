package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
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
 */
public class Rocchio
{
    /**
     * Get a term-frequency map for the specified document.
     *
     * @param doc_id The numerical identifier of a document.
     * @param index_reader The index reader that points to the index.
     */
    private Map<String, Long> getTermFreqForDoc(int doc_id, IndexReader index_reader) throws IOException
    {
        Map<String, Long> retval = new HashMap<>();

        Fields doc_fields = index_reader.getTermVectors(doc_id);
        for(String fieldname : doc_fields)
        {
            Terms terms = doc_fields.terms(fieldname);
            TermsEnum enumerator = terms.iterator();
            while(enumerator.next() != null)
            {
                String term = enumerator.term().utf8ToString();
                long freq = enumerator.totalTermFreq();

                retval.put(term, freq);
            }
        }

        return retval;
    }

    /**
     * Given a term-frequency map, construct a term-weight map.
     */
    private Map<String, Double> getQueryWeights()
    {
        return null;
    }

    /**
     * Construct a query based on a set of weighted terms.
     *
     * @param weights A map that gives the weights for terms.
     */
    private Query termVectorToQuery(Map<String, Double> weights) throws ParseException
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

    /**
     * Adjust the specified query to the set of relevant and irrelevant documents.
     *
     * @param old_query The original query.
     * @param relevant_set A list of documents that have been marked as relevant.
     * @param non_relevant_set A list of documents that have been marked as irrelevant.
     *
     * @return
     */
    public static Query adjustQuery(Query old_query, List<Document> relevant_set, List<Document> non_relevant_set)
    {
        return null;
    }
}

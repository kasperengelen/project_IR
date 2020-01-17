package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Special query class for the rocchio algorithm.
 */
public class RocchioQuery
{
    private Map<String, Double> m_weights = new HashMap<>();

    /**
     * Construct a query based on the specified query string. The query string is tokenized and preprocessed by the specified
     * analyzer.
     */
    public RocchioQuery(String querystring, Analyzer analyzer) throws IOException
    {
        TokenStream tokens = analyzer.tokenStream(Constants.FieldNames.BODY, querystring);
        CharTermAttribute attr = tokens.addAttribute(CharTermAttribute.class);
        tokens.reset();

        while(tokens.incrementToken())
        {
            String term = attr.toString();

            if(!m_weights.containsKey(term))
            {
                m_weights.put(term, 1.0);
            }
        }
    }

    /**
     * Construct a query based on the specified term weights.
     */
    public RocchioQuery(Map<String, Double> weights)
    {
        m_weights = weights;
    }

    /**
     * Retrieve the weights of the different terms that are part of the query.
     */
    public Map<String, Double> weights()
    {
        return m_weights;
    }

    public Query toLuceneQuery()
    {
        return null;
    }
}

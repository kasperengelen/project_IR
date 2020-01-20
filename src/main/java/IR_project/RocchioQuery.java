package IR_project;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Special query class for the rocchio algorithm.
 */
public class RocchioQuery
{
    private Map<String, Double> m_weights = new HashMap<>();

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

    /**
     * Convert the {@link RocchioQuery} object into a Lucene {@link Query} object.
     */
    public Query toLuceneQuery()
    {
        // SOURCE: https://stackoverflow.com/questions/34783819/assigning-different-weights-to-different-query-terms-in-lucene
        // SOURCE: https://stackoverflow.com/questions/47289641/how-do-i-boost-queries-in-lucene-7

        BooleanQuery.Builder retval = new BooleanQuery.Builder();

        for(Map.Entry<String, Double> entry : m_weights.entrySet())
        {
            Query term_query = new TermQuery(new Term(Constants.FieldNames.BODY, entry.getKey()));
            Query boosted_query = new BoostQuery(term_query, entry.getValue().floatValue());

            retval.add(boosted_query, BooleanClause.Occur.SHOULD);
        }

        return retval.build();
    }
}

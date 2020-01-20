package IR_project;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class that defines various constants.
 */
public class Constants
{
    /**
     * The default similarity that will be used
     */
    public static final Similarity SIMILARITY = new BM25Similarity();

    /**
     * Path to the directory that contains the index files.
     */
    public static final Path PATH_INDEX = Paths.get("../index/");

    /**
     * Path to the directory that contains the document files.
     */
    public static final Path PATH_DOCUMENTS = Paths.get("../sample_100k/");

    /**
     * Class that contains constants for different field names.
     */
    public static class FieldNames
    {
        public static final String BODY = "body";
        public static final String TITLE = "title";
        public static final String TAGS = "tags";
        public static final String IDENTIFIER = "identifier";
    }
}

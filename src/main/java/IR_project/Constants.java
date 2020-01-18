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
    public static final Similarity DEFAULT_SIM = new BM25Similarity();
//    public static final Similarity DEFAULT_SIM = new ClassicSimilarity();
//    public static final Similarity DEFAULT_SIM = new LMJelinekMercerSimilarity((float) 0.8);
//    public static final Similarity DEFAULT_SIM = new LMDirichletSimilarity((float) 0.8);
    /**
     * Path to the directory that contains the index files.
     */
    public static final Path PATH_INDEX = Paths.get("../index/"); // BM25
//    public static final Path PATH_INDEX = Paths.get("../index1/"); // TD-IDF
//    public static final Path PATH_INDEX = Paths.get("../index2/"); // LMJM
//    public static final Path PATH_INDEX = Paths.get("../index3/"); // LMD

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

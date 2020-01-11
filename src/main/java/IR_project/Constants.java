package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
     * The default similarity that will be used for indexing.
     */
    public static final Similarity DEFAULT_SIM_INDEXER = new BM25Similarity();
//    public static final Similarity DEFAULT_SIM_INDEXER = new ClassicSimilarity();
//    public static final Similarity DEFAULT_SIM_INDEXER = new LMJelinekMercerSimilarity((float) 0.8);
//    public static final Similarity DEFAULT_SIM_INDEXER = new LMDirichletSimilarity((float) 0.8);

    /**
     * The default similarity that will be used for searching.
     */
    public static final Similarity DEFAULT_SIM_SEARCHER = new BM25Similarity();
//    public static final Similarity DEFAULT_SIM_SEARCHER = new ClassicSimilarity();
//    public static final Similarity DEFAULT_SIM_SEARCHER = new LMJelinekMercerSimilarity((float) 0.8);
//    public static final Similarity DEFAULT_SIM_SEARCHER = new LMDirichletSimilarity((float) 0.8);

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
    public static final Path PATH_DOCUMENTS = Paths.get("../ProjectIRSample/sample_500k/");
}

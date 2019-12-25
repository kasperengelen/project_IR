package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
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

    /**
     * The default similarity that will be used for searching.
     */
    public static final Similarity DEFAULT_SIM_SEARCHER = new BM25Similarity();

    /**
     * Path to the directory that contains the index files.
     */
    public static final Path PATH_INDEX = Paths.get("../index/");

    /**
     * Path to the directory that contains the document files.
     */
    public static final Path PATH_DOCUMENTS = Paths.get("../sample_500k/");
}

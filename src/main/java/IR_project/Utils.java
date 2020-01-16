package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;

public class Utils
{
    /**
     * Retrieve the document ID of the document located at the specified path.
     *
     * @param document_file The path of the document.
     */
    public static String getDocumentID(Path document_file)
    {
        return document_file.getFileName().toString().toLowerCase().replace(".xml", "").toUpperCase();
    }

    /**
     * Retrieve an instance of the analyzer used by the project.
     */
    public static Analyzer getAnalyzer() {
        return new EnglishAnalyzer();
    }

}

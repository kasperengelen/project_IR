package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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

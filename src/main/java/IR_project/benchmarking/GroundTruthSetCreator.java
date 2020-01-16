package IR_project.benchmarking;

import IR_project.DocumentXMLHandler;
import IR_project.Logger;
import IR_project.Utils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that creates ground truth sets from a directory of documents.
 */
public class GroundTruthSetCreator
{

    public static void createSets(Path doc_directory, PrintWriter out_sets, PrintWriter out_terms) throws IOException
    {

        Logger.logDebug("Started walking file tree");

        final int[] count = {0};

        Files.walkFileTree(doc_directory, new SimpleFileVisitor<Path>() {
            @SuppressWarnings("DuplicatedCode")
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            {
                try {
                    // parse document
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    DocumentXMLHandler handler = new DocumentXMLHandler();

                    String file_data = "<document>";

                    file_data += new String(Files.readAllBytes(file));

                    file_data += "</document>";

                    InputStream file_stream = new ByteArrayInputStream(file_data.getBytes(StandardCharsets.UTF_8));
                    saxParser.parse(file_stream, handler);

                    String query = handler.getTitle().toLowerCase();

                    out_sets.println(query + " | " + file.getFileName().toString()); // add to output
                    out_terms.println(query);

                    count[0]++;

                    if (count[0] % 1000 == 0) {
                        Logger.logDebug("Processed %d documents.", count[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return FileVisitResult.CONTINUE;
            }
        });

    }
}

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Class that creates ground truth sets from a directory of documents.
 */
public class GroundTruthSetCreator
{

    public static void createSets(Path doc_directory, PrintWriter out_sets, PrintWriter out_terms) throws IOException
    {
        // foreach document:
        //  tokens = document.title.tokenize()
        //  foreach token in tokens:
        //      write(token, document.docid)

        Set<String> unique_term_set = new HashSet<>();

        Files.walkFileTree(doc_directory, new SimpleFileVisitor<Path>() {
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

                    Analyzer analyzer = Utils.getAnalyzer();

                    TokenStream stream = analyzer.tokenStream("??", handler.getTitle());
                    CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);
                    stream.reset();

                    while(stream.incrementToken()) {
                        String token_value = attr.toString();
                        if(token_value.length() > 2) {
                            unique_term_set.add(token_value); // add to set of unique terms
                            out_sets.println(token_value + " " + file.getFileName().toString()); // add to output
                        }
                        //Logger.logDebug("%s %s", token_value, Utils.getDocumentID(file));
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                return FileVisitResult.CONTINUE;
            }
        });

        for(String unique_token : unique_term_set)
        {
            out_terms.println(unique_token);
        }
    }
}

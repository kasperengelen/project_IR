package IR_project;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class that provides printing facilities for documents.
 */
public class DocumentPrinter
{
    /**
     * Print the document pointed to by the specified filename, to the standard output.
     *
     * @param file The path that points to the document.
     */
    public static void printDocument(Path file)
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

            Logger.logOut("FILENAME: %s", file.toString());
            Logger.logOut("TITLE: %s", handler.getTitle());
            Logger.logOut("TAGS: %s", handler.getTags());

        } catch (IOException | SAXException | ParserConfigurationException e) {
            Logger.logError("Cannot print document with filename '%s': %s", file.toString(), e.getMessage());
        }
    }
}

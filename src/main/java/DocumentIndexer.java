import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.Path;

public class DocumentIndexer
{

    public static void documentIndex(Path file, IndexWriter writer)
    {
        try {
            // parse document
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DocumentHandler handler = new DocumentHandler();

            saxParser.parse(file.toFile(), handler);

            // add document to lucene
            Document doc = new Document();

            // TODO
            // add title
            // add tags

            // add question

            // add answers


        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO ctor based on file from dataset
    //  -> read document contents & create fields
    //  -> "addToIndex(Index) method"

    // NOTE: file structure:
    //  - question
    //      - body
    //      - tags
    //  - answer
    //      - body
    //  - answer
    //      ...
    //  ...

    // TODO: fields
    //  - title (indexed)
    //  - tags (indexed)
    //  - original path?
    //  - question (indexed)
    //  - answers (indexed) (all answers are merged together)


}
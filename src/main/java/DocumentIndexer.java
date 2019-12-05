import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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

            // TODO: tokenizers, token filters, analyzers etc

            // NOTE: if STORE is set to true, this means that it will be returned as a search result
            //       if STORE is set to false, it still can be used in queries, but it won't be returned
            //                  as the result of the search

            doc.add(new TextField("title", handler.getTitle(), Field.Store.NO));
            doc.add(new TextField("question", handler.getQuestion(), Field.Store.NO));
            doc.add(new TextField("answers", handler.getAnswers(), Field.Store.NO));
            doc.add(new TextField("tags", handler.getTags(), Field.Store.NO));

            // we use the filename to identify the document.
            doc.add(new StringField("filename", file.getFileName().toString(), Field.Store.YES));


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

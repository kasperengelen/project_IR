import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/**
 * Part of the class was inspired by the following source:
 *    https://lucene.apache.org/core/5_4_0/demo/src-html/org/apache/lucene/demo/IndexFiles.html
 *    https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
 */
public class DocumentIndexer
{
    /**
     * Index the specified directory of documents, and store it to the specified path.
     *
     * @param documents_path The directory that contains the documents that are to be indexed.
     * @param index_path The directory that contains the created index.
     * @param create_new True if the index is created from scratch, False if the index already exists and needs to be updated.
     *
     * @throws IOException If the index and document directories could not be properly accessed.
     */
    public static void createIndex(Path documents_path, Path index_path, boolean create_new) throws IOException
    {
        // TODO analyzer kiezen! dit heeft invloed op tokenization
        Directory dir = FSDirectory.open(index_path);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        //iwc.setRAMBufferSizeMB(5120.0);

        if (create_new) {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        Date start = new Date();

        IndexWriter writer = new IndexWriter(dir, iwc);

        // trick to allow for final declared variable.
        final int[] doc_counter = {0, 0};

        if (Files.isDirectory(documents_path)) {
            Files.walkFileTree(documents_path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    doc_counter[0] += 1;

                    // if the indexing was successfull
                    if(M_indexDocument(file, writer)) {
                        doc_counter[1] += 1;
                    }

                    if(doc_counter[0] % 50 == 0)
                    {
                        System.out.println(String.format(
                                "Processed %d documents of which %d successful.",
                                doc_counter[0], doc_counter[1]
                        ));
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            throw new IOException(String.format(
                "Error path '%s' must point to directory.",
                documents_path.toString()
            ));
        }

        writer.close();

        Date end = new Date();

        System.out.println(String.format(
            "Indexing complete. Indexed %d/%d documents. Took %d miliseconds.",
            doc_counter[1], doc_counter[0], end.getTime() - start.getTime()
        ));
    }

    /**
     * Index the
     * @param file The file that contains information about the document.
     * @param writer The {@link IndexWriter} that writes to a Lucene index.
     *
     * @return True if the document was successfully indexed, False otherwised.
     */
    private static boolean M_indexDocument(Path file, IndexWriter writer)
    {
        try {
            // parse document
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DocumentXMLHandler handler = new DocumentXMLHandler();

            // TODO enclose file in <document> tags so that is has a single root.

            String file_data = "<document>";

            file_data += new String(Files.readAllBytes(file));

            file_data += "</document>";

            InputStream file_stream = new ByteArrayInputStream(file_data.getBytes(StandardCharsets.UTF_8));

            saxParser.parse(file_stream, handler);


            // add document to lucene
            Document doc = new Document();

            // NOTE: if STORE is set to true, this means that it will be returned as a search result
            //       if STORE is set to false, it still can be used in queries, but it won't be returned
            //                  as the result of the search

            doc.add(new TextField("title", handler.getTitle(), Field.Store.NO));
            doc.add(new TextField("question", handler.getQuestion(), Field.Store.NO));
            doc.add(new TextField("answers", handler.getAnswers(), Field.Store.NO));
            doc.add(new TextField("tags", handler.getTags(), Field.Store.NO));

            // we use the filename to identify the document.
            doc.add(new StringField("filename", file.getFileName().toString(), Field.Store.YES));

            // add to index
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                //System.out.println(String.format("Added document '%s'", file.getFileName().toString()));
                writer.addDocument(doc);
            } else {
                // Existing index (an old copy of this document may have been indexed) so
                // we use updateDocument instead to replace the old one matching the exact
                // path, if present:
                //System.out.println(String.format("Updated document '%s'", file.getFileName().toString()));
                writer.updateDocument(new Term("filename", file.getFileName().toString()), doc);
            }

            return true;

        } catch (SAXException | ParserConfigurationException | IOException e) {
            System.out.println(String.format(
               "Error while processing document '%s': %s",
               file.getFileName().toString(), e.getMessage()
            ));

            e.printStackTrace();

            return false;
        }
    }
}

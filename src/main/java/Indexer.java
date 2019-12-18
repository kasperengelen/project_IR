import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
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
public class Indexer
{
    /**
     * Class that contains constants for different field names.
     */
    public static class FieldNames
    {
        public static final String ANSWER = "answer";
        public static final String QUESTION = "question";
        public static final String TITLE = "title";
        public static final String TAGS = "tags";
        public static final String FILENAME = "filename";
    }

    /**
     * Class that contains information about an indexation.
     */
    public static class IndexationStats
    {
        /**
         * The duration of the indexation in miliseconds.
         */
        int runtime;

        /**
         * The amount of files that were successfully indexed.
         */
        int completed;

        /**
         * The total amount of files that the indexer tried to index.
         */
        int total;
    }

    /**
     * Index the specified directory of documents, and store it to the specified path.
     *
     * @param documents_path The directory that contains the documents that are to be indexed.
     * @param index_path The directory that contains the created index.
     * @param create_new True if the index is created from scratch, False if the index already exists and needs to be updated.
     * @param print_progress True if progress messages and error messages should be printed, false otherwise.
     *
     * @throws IOException If the index and document directories could not be properly accessed.
     */
    public static IndexationStats createIndex(Path documents_path, Path index_path, boolean create_new, boolean print_progress) throws IOException
    {
        Directory dir = FSDirectory.open(index_path);
        Analyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(Constants.DEFAULT_SIM_INDEXER);

        // performance options
//        ConcurrentMergeScheduler x = new ConcurrentMergeScheduler();
//
//        x.setMaxMergesAndThreads(1,1);
//        iwc.setMergeScheduler(x);
//
//        iwc.setRAMBufferSizeMB(5120.0);

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

        // keep track of stats
        final IndexationStats stats = new IndexationStats();

        if (Files.isDirectory(documents_path)) {
            Files.walkFileTree(documents_path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    stats.total += 1;

                    // if the indexing was successfull
                    if(M_indexDocument(file, writer, print_progress)) {
                        stats.completed += 1;
                    }

                    if(print_progress && stats.completed % 1000 == 0) {
                        Logger.logOut("Processed %d documents, of which %d successful.", stats.total, stats.completed);
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

        stats.runtime = Math.toIntExact(end.getTime() - start.getTime());

        return stats;
    }

    /**
     * Index the
     * @param file The file that contains information about the document.
     * @param writer The {@link IndexWriter} that writes to a Lucene index.
     * @param print_errors True if errors messages should be printed, false otherwise.
     *
     * @return True if the document was successfully indexed, False otherwise.
     */
    private static boolean M_indexDocument(Path file, IndexWriter writer, boolean print_errors)
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

            // add document to lucene
            Document doc = new Document();

            // NOTE: if STORE is set to true, this means that it will be returned as a search result
            //       if STORE is set to false, it still can be used in queries, but it won't be returned
            //                  as the result of the search

            doc.add(new TextField(FieldNames.TITLE,    handler.getTitle(),                              Field.Store.NO));
            doc.add(new TextField(FieldNames.QUESTION, handler.getQuestion(),                           Field.Store.NO));
            doc.add(new TextField(FieldNames.ANSWER,   String.join(" ", handler.getAnswers()),  Field.Store.NO));
            doc.add(new TextField(FieldNames.TAGS,     handler.getTags(),                               Field.Store.NO));

            // we use the filename to identify the document.
            doc.add(new StringField(FieldNames.FILENAME, file.getFileName().toString(), Field.Store.YES));

            // add to index
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                // add new document
                writer.addDocument(doc);
            } else {
                // update existing document
                writer.updateDocument(new Term(FieldNames.FILENAME, file.getFileName().toString()), doc);
            }

            return true;

        } catch (SAXException | ParserConfigurationException | IOException e) {
            if(print_errors) {
                Logger.logError("Error while processing document '%s': %s", file.getFileName().toString(), e.getMessage());

                e.printStackTrace();
            }

            return false;
        }
    }
}

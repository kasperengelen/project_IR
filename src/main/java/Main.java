import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Scanner;

// TODO exceptions

/**
 * Main class.
 */
public class Main
{
    public static void printTitles(Path documents_path)
    {
        try {
            // format "<doc id> <title>"
            PrintWriter title_out = new PrintWriter(new File("./file_titles.txt"));
            // format "<doc id> <tags>"
            PrintWriter tags_out = new PrintWriter(new File("./file_tags.txt"));

            if (Files.isDirectory(documents_path)) {
                Files.walkFileTree(documents_path, new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
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

                            String doc_id = file.getFileName().toString().replace(".xml", "");

                            title_out.println(doc_id + " " + handler.getTitle());
                            tags_out.println(doc_id + " " + handler.getTags());

                        } catch (SAXException | ParserConfigurationException e) {

                        }

                        return FileVisitResult.CONTINUE;
                    }
                });

                title_out.close();
                tags_out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv)
    {

        // TODO performance-conscious programming
        // TODO allocate more ram (4GB?)

        try {
            Logger.logDebug("test");

            Path sample_dir = Paths.get("../sample_100k/");
            Path index_dir = Paths.get("../index/");

            //printTitles(sample_dir);

            Logger.logOut("Construct index? (y)es or (n)o");
            Scanner input_scanner = new Scanner(System.in);
            String answer = input_scanner.nextLine();

            if(answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes"))
            {
                Indexer.IndexationStats stats = Indexer.createIndex(sample_dir, index_dir, false, true);

                Logger.logOut("Indexing complete. Indexed %d/%d documents. Took %d miliseconds.", stats.completed, stats.total, stats.runtime);
            }

            Logger.logOut("Input query:");
            String query = input_scanner.nextLine();
            Logger.logOut("");

            Logger.logOut("Searching for '" + query + "'...");
            Logger.logOut("");

            Date search_start = new Date();
            Searcher.SearchResult result = Searcher.search(query, 50, index_dir);
            Date search_end = new Date();

            Logger.logOut(
                    "Found results. Took %d miliseconds. %d results total, %d retrieved.",
                    search_end.getTime() - search_start.getTime(), result.totalResultCount, result.topResultFilenames.size()
            );

            for (String filename : result.topResultFilenames)
            {
                Logger.logOut("");
                DocumentPrinter.printDocument("../sample_100k/" + filename);
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }
}

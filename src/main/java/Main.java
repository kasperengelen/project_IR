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
        // format: <exec> mode=benchmark mode=search index=true index=false print_imm=true print_imm=false top=<int>
        try {

            boolean mode_search = true;
            boolean index = false;
            boolean prompt_for_index = true;
            boolean print_imm = true;
            int top = 50;

            for (int i = 1; i < argv.length; i++) {
                String arg = argv[i];

                // split on "="

                if (!arg.contains("=")) {
                    Logger.logError("Invalid formatted argument '%s'", arg);
                    return;
                }

                String[] splitted = arg.split("=");
                String value = splitted[1].toLowerCase();


                switch (splitted[0].toLowerCase()) {
                    case "mode":
                        if(value.equals("search")) {
                            mode_search = true;
                        } else if(value.equals("benchmark")) {
                            mode_search = false;
                        } else {
                            Logger.logError("Invalid mode name '%s'", splitted[1]);
                            return;
                        }

                        break;
                    case "index":
                        if(value.equals("true")) {
                            index = true;
                            prompt_for_index = false;
                        } else if(value.equals("false")) {
                            index = false;
                            prompt_for_index = false;
                        } else {
                            Logger.logError("Invalid boolean '%s'", splitted[1]);
                            return;
                        }

                        break;
                    case "print_imm": {
                        if(value.equals("true")) {
                            print_imm = true;
                        } else if(value.equals("false")) {
                            print_imm = false;
                        } else {
                            Logger.logError("Invalid boolean '%s'", splitted[1]);
                            return;
                        }
                        break;
                    }
                    case "top": {
                        top = Integer.parseInt(value);
                        if(top <= 0) {
                            Logger.logError("Invalid value for top '%d', top needs to be greater than zero.", top);
                        }
                        break;
                    }
                    default:
                        Logger.logError("Invalid argument name '%s'", splitted[0]);
                        return;
                }
            }

            if (mode_search) {
                M_searchMode(index, prompt_for_index, top);
            } else {
                M_benchmarkMode(index, print_imm);
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run the retrieval mode of the program.
     *
     * @param do_index True if the index needs to be created.
     * @param prompt_user_for_index True if the user needs to be asked whether the index is to be created.
     * @param result_count Maximum number of results that are displayed.
     */
    private static void M_searchMode(boolean do_index, boolean prompt_user_for_index, int result_count)
    {
        try {
            Scanner input_scanner = new Scanner(System.in);

            if(!do_index && prompt_user_for_index) {
                Logger.logOut("Construct index? (y)es or (n)o");

                String answer = input_scanner.nextLine();

                do_index = (answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes"));
            }

            if(do_index)
            {
                Indexer.IndexationStats stats = Indexer.createIndex(Constants.PATH_DOCUMENTS, Constants.PATH_INDEX, false, true);

                Logger.logOut("Indexing complete. Indexed %d/%d documents. Took %d miliseconds.", stats.completed, stats.total, stats.runtime);
            }

            Logger.logOut("Input query:");
            String query = input_scanner.nextLine();
            Logger.logOut("");

            Logger.logOut("Searching for '" + query + "'...");
            Logger.logOut("");

            Date search_start = new Date();
            Searcher.SearchResult result = Searcher.search(query, result_count, Constants.PATH_INDEX);
            Date search_end = new Date();

            Logger.logOut(
                    "Found results. Took %d miliseconds. %d results total, %d retrieved.",
                    search_end.getTime() - search_start.getTime(), result.totalResultCount, result.topResultFilenames.size()
            );

            for (String filename : result.topResultFilenames)
            {
                Logger.logOut("");
                DocumentPrinter.printDocument(Paths.get(Constants.PATH_DOCUMENTS.toString(), filename));
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run the benchmark mode of the program.
     *
     * @param do_index True if the index needs to be (re)created.
     * @param print_results_immediately True if results are immediately printed for each similarity, false otherwise.
     */
    private static void M_benchmarkMode(boolean do_index, boolean print_results_immediately)
    {
        try {
            if (do_index) {
                Indexer.IndexationStats stats = Indexer.createIndex(Constants.PATH_DOCUMENTS, Constants.PATH_INDEX, false, true);

                Logger.logOut("Indexing complete. Indexed %d/%d documents. Took %d miliseconds.", stats.completed, stats.total, stats.runtime);
            }

            Benchmark.BenchmarkResult result = Benchmark.doBenchmark(true, print_results_immediately);

            // TODO print results

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }
}

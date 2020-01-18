package IR_project;

import org.apache.lucene.search.Query;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * IR_project.Main class.
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

                            String doc_id = Utils.getDocumentID(file);

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
        // format: <exec> mode=normal mode=rocchio index=true index=false print_imm=true print_imm=false top=<int>
        try {

            boolean normal_search = true;
            boolean index = false;
            boolean prompt_for_index = true;
            boolean print_imm = true;
            int top = 10;

            for (int i = 0; i < argv.length; i++) {
                String arg = argv[i];

                // split on "="

                if (!arg.contains("=")) {
                    Logger.logError("Invalid formatted argument '%s'", arg);
                    continue;
                }

                String[] splitted = arg.split("=");
                String value = splitted[1].toLowerCase();

                switch (splitted[0].toLowerCase()) {
                    case "mode":
                        if(value.equals("normal"))
                        {
                            normal_search=true;
                        }
                        else if(value.equals("rocchio"))
                        {
                            normal_search=false;
                        }
                        else
                        {
                            Logger.logError("Invalid mode '%s'", value);
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

            if(normal_search) {
                M_normalSearch(index, prompt_for_index, top);
            } else {
                M_rocchioSearch(index, prompt_for_index, top);
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run the normal retrieval mode of the program.
     *
     * @param do_index True if the index needs to be created.
     * @param prompt_user_for_index True if the user needs to be asked whether the index is to be created.
     * @param result_count Maximum number of results that are displayed.
     */
    private static void M_normalSearch(boolean do_index, boolean prompt_user_for_index, int result_count)
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
            Searcher searcher = new Searcher();
            Searcher.SearchResult result = searcher.search(query, result_count);
            Date search_end = new Date();

            Logger.logOut(
                    "Found results. Took %d miliseconds. %d results total, %d retrieved.",
                    search_end.getTime() - search_start.getTime(), result.totalResultCount, result.topResultIDs.size()
            );

            for (String identifier : result.topResultIDs)
            {
                Logger.logOut("");
                Logger.logOut(identifier);
//                DocumentPrinter.printDocument(Paths.get(Constants.PATH_DOCUMENTS.toString(), identifier + ".xml"));
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void M_rocchioSearch(boolean do_index, boolean prompt_user_for_index, int result_count) throws IOException
    {
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

        // de parameters komen uit de cursus
        Searcher searcher = new Searcher();
        Rocchio algorithm_utils = new Rocchio(0.30,0.75,0.25,1.2, 1.2, 0.75);
        RocchioQuery current_rocchio_query = algorithm_utils.parseQuery(query, Utils.getAnalyzer());

        // keep adjusting results and executing rocchio.
        while(true) {
            Date search_start = new Date();
            Searcher.SearchResult result = searcher.search(current_rocchio_query.toLuceneQuery(), result_count);
            Date search_end = new Date();

            Logger.logOut(
                    "Found results. Took %d miliseconds. %d results total, %d retrieved.",
                    search_end.getTime() - search_start.getTime(), result.totalResultCount, result.topResultIDs.size()
            );

            int doc_counter = 1;
            // documents in the output will be labeled 1,2,3, ..., k
            // this map converts such a number to the lucene document id
            Map<Integer, Integer> doc_counter_to_lucene_id = new HashMap<>();
            for (String identifier : result.topResultIDs) {
                Logger.logOut("");
                Logger.logOut("#%d:", doc_counter);

                DocumentPrinter.printDocument(Paths.get(Constants.PATH_DOCUMENTS.toString(), identifier + ".xml"));

                // SO identifier -> (counter, lucene_id)
                doc_counter_to_lucene_id.put(doc_counter, result.docIdentifierToInternalId.get(identifier));
                doc_counter++;
            }

            Logger.logOut("Refine using feedback?");
            String feedback_answer = input_scanner.nextLine();
            boolean do_feedback = (feedback_answer.toLowerCase().equals("y") || feedback_answer.toLowerCase().equals("yes"));

            // ask user for positive feedback and negative feedback
            if(!do_feedback)
                break;

            List<Integer> relevant_set = new ArrayList<>();
            List<Integer> non_relevant_set = new ArrayList<>();

            Logger.logOut("Enter relevant documents (comma separated):");

            // ask user for relevant and non-relevant documents
            String relevant_string = input_scanner.nextLine();
            for(String rel_id : relevant_string.split(","))
            {
                try {
                    int value = Integer.parseInt(rel_id);
                    relevant_set.add(doc_counter_to_lucene_id.get(value));
                } catch(NumberFormatException ignored) {
                }
            }

            Logger.logOut("Enter non-relevant documents (comma separated):");

            String non_relevant_string = input_scanner.nextLine();
            for(String non_rel_id : non_relevant_string.split(","))
            {
                try {
                    int value = Integer.parseInt(non_rel_id);
                    non_relevant_set.add(doc_counter_to_lucene_id.get(value));
                } catch(NumberFormatException ignored) {
                }
            }

            current_rocchio_query = algorithm_utils.adjustQuery(current_rocchio_query, relevant_set, non_relevant_set, searcher.getIndexReader());
        }
    }
}

package IR_project;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * IR_project.Main class.
 */
public class Main
{
    /**
     * Main function.
     *
     * Argument format: "x=y"
     *
     * Arguments:
     *  mode: can by "normal" for normal search and "rocchio" for relevance feedback.
     *  index: can be "true" to perform index, can be "false" to not perform index. The program will prompt the user if omitted.
     *  progress: can be "true" to print indexing progress, can be "false" not to print indexing progress.
     *  top: pass integer value. The value will be the amount of documents returned during search.
     *
     * @param argv Program arguments.
     */
    public static void main(String[] argv)
    {
        // format: <exec> mode=normal mode=rocchio index=true index=false progress=true progress=false top=<int>
        try {

            boolean normal_search = true; // by default, do normal search
            boolean index = false; // by default, don't do index, but ask
            boolean prompt_for_index = true; // by default, ask for indexing
            boolean print_progress = true; // by default, print progress while indexing.
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
                    case "progress": {
                        if(value.equals("true")) {
                            print_progress = true;
                        } else if(value.equals("false")) {
                            print_progress = false;
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

            // create index of needed
            {
                Scanner input_scanner = new Scanner(System.in);

                if (!index && prompt_for_index) {
                    Logger.logOut("Construct index? (y)es or (n)o");

                    String answer = input_scanner.nextLine();

                    index = (answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes"));
                }

                if (index) {
                    Indexer.IndexationStats stats = Indexer.createIndex(Constants.PATH_DOCUMENTS, Constants.PATH_INDEX, false, print_progress);

                    Logger.logOut("Indexing complete. Indexed %d/%d documents. Took %d miliseconds.", stats.completed, stats.total, stats.runtime);

                }
            }

            if(normal_search) {
                M_normalSearch(top);
            } else {
                M_rocchioSearch(top);
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run the normal retrieval mode of the program.
     *
     * @param result_count Maximum number of results that are displayed.
     */
    private static void M_normalSearch(int result_count)
    {
        try {
            Scanner input_scanner = new Scanner(System.in);

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
                DocumentPrinter.printDocument(Paths.get(Constants.PATH_DOCUMENTS.toString(), identifier + ".xml"));
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Run the program with Rocchio relevance feedback.
     *
     * @param result_count Maximum number of results that are displayed.
     */
    private static void M_rocchioSearch(int result_count) throws IOException
    {
        Scanner input_scanner = new Scanner(System.in);

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

            Logger.logOut("Refine using feedback? (y)es or (n)o");
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

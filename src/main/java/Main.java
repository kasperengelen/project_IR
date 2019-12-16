import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;

// TODO exceptions

/**
 * Main class.
 */
public class Main
{
    public static void main(String[] argv)
    {

        // TODO performance-conscious programming
        // TODO allocate more ram (4GB?)

        try {
            Logger.logDebug("test");

            Path sample_dir = Paths.get("../big_sample/");
            Path index_dir = Paths.get("../index/");

            Logger.logOut("Construct index? (y)es or (n)o");
            Scanner input_scanner = new Scanner(System.in);
            String answer = input_scanner.nextLine();

            if(answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes"))
            {
                DocumentIndexer.IndexationStats stats = DocumentIndexer.createIndex(sample_dir, index_dir, false, true);

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
                DocumentPrinter.printDocument("../big_sample/" + filename);
            }

        } catch(Exception e) {
            Logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }
}

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

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
            System.out.println("test");

            Path sample_dir = Paths.get("../posts_sample/");
            Path index_dir = Paths.get("../index/");

            if(false) {

                DocumentIndexer.IndexationStats stats = DocumentIndexer.createIndex(sample_dir, index_dir, true, true);

                System.out.println(String.format(
                        "Indexing complete. Indexed %d/%d documents. Took %d miliseconds.",
                        stats.completed, stats.total, stats.runtime
                ));
            }


            String query = "sorting";
            Date search_start = new Date();
            Searcher.SearchResult result = Searcher.search(query, 50, index_dir);
            Date search_end = new Date();

            System.out.println(String.format(
                    "Found results. Took %d miliseconds. %d results total, %d retrieved.",
                    search_end.getTime() - search_start.getTime(), result.totalResultCount, result.topResultFilenames.size()
            ));

            System.out.println("RESULT:");

            for (String filename : result.topResultFilenames)
            {
                System.out.println(filename);
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

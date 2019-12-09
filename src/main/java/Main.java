import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

            DocumentIndexer.createIndex(sample_dir, index_dir, true);


            // TODO args
            // TODO call DocumentIndexer
            // TODO querier
        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

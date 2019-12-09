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
        try {
            System.out.println("test");

            Path sample_dir = Paths.get("../small_sample/");
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

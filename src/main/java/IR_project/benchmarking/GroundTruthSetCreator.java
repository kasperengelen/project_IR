package IR_project.benchmarking;

import IR_project.DocumentXMLHandler;
import IR_project.Logger;
import IR_project.Utils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that creates ground truth sets from a directory of documents.
 */
public class GroundTruthSetCreator
{

    public static void createTitleQueryGroundTruth(Path doc_directory, PrintWriter out_sets) throws IOException
    {

        Logger.logDebug("Started walking file tree");

        final int[] count = {0};

        Files.walkFileTree(doc_directory, new SimpleFileVisitor<Path>() {
            @SuppressWarnings("DuplicatedCode")
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
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

                    String query = handler.getTitle().toLowerCase();

                    out_sets.println(query + " | " + file.getFileName().toString()); // add to output

                    count[0]++;

                    if (count[0] % 1000 == 0) {
                        Logger.logDebug("Processed %d documents.", count[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return FileVisitResult.CONTINUE;
            }
        });

    }

    public static void createTitleTermQueryGroundTruth(Path doc_directory, PrintWriter out_sets) throws IOException
    {

        Logger.logDebug("Started walking file tree");

        final int[] count = {0};

        Map<String, ArrayList<String>> termToDocs = new HashMap<>();

        Files.walkFileTree(doc_directory, new SimpleFileVisitor<Path>() {
            @SuppressWarnings("DuplicatedCode")
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
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

                    Analyzer analyzer = Utils.getAnalyzer();

                    TokenStream stream = analyzer.tokenStream("??", handler.getTitle());
                    CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);
                    stream.reset();

                    while(stream.incrementToken()) {
                        String token_value = attr.toString();

                        if(token_value.length() > 2 && !EnglishAnalyzer.ENGLISH_STOP_WORDS_SET.contains(token_value)) {
                            ArrayList<String> l = termToDocs.getOrDefault(token_value, new ArrayList<>());
                            if (l.isEmpty()) {
                                l.add(file.getFileName().toString());
                                termToDocs.put(token_value, l);
                            } else l.add(file.getFileName().toString());
                        }

//                        if(token_value.length() > 2) {
//                            unique_term_set.add(token_value); // add to set of unique terms
//                            if(unique_term_set.contains(token_value)) {
//                            unique_term_set.add(token_value); // add to set of unique terms
//                                out_sets.println(token_value + " " + file.getFileName().toString()); // add to output
//                            }
//                        }
                    }

                    count[0]++;

                    if (count[0] % 1000 == 0) {
                        Logger.logDebug("Processed %d documents.", count[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return FileVisitResult.CONTINUE;
            }
        });

        for(Map.Entry<String, ArrayList<String>> entry : termToDocs.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            if (value.size() > 1) {
                StringBuilder line = new StringBuilder(key + " |");
                for (String f : value) {
                    line.append(" ").append(f.toLowerCase().replace(".xml", "").toUpperCase());
                }
                out_sets.println(line.toString());
            }
        }

    }

    public static void main(String[] argv)
    {
        try {

            PrintWriter set_file = new PrintWriter(new File("./titleTermQueryDoc.txt"));
            IR_project.benchmarking.GroundTruthSetCreator.createTitleTermQueryGroundTruth(IR_project.Constants.PATH_DOCUMENTS, set_file);
            set_file.close();

//            PrintWriter set_file = new PrintWriter(new File("./titleQueryDoc.txt"));
//            IR_project.benchmarking.GroundTruthSetCreator.createTitleQueryGroundTruth(IR_project.Constants.PATH_DOCUMENTS, set_file);
//            set_file.close();

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}

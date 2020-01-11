package IR_project.benchmarking;

import IR_project.DocumentXMLHandler;
import IR_project.Logger;
import IR_project.Utils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that creates ground truth sets from a directory of documents.
 */
public class GroundTruthSetCreator
{

    public static void createSets(Path doc_directory, PrintWriter out_sets, PrintWriter out_terms) throws IOException
    {
        // foreach document:
        //  tokens = document.title.tokenize()
        //  foreach token in tokens:
        //      write(token, document.docid)

        Set<String> unique_term_set = new HashSet<>();
        {
            unique_term_set.add("python");
            unique_term_set.add("how");
            unique_term_set.add("us");
            unique_term_set.add("c");
            unique_term_set.add("from");
            unique_term_set.add("file");
            unique_term_set.add("function");
            unique_term_set.add("i");
            unique_term_set.add("error");
            unique_term_set.add("list");
            unique_term_set.add("valu");
            unique_term_set.add("get");
            unique_term_set.add("django");
            unique_term_set.add("string");
            unique_term_set.add("when");
            unique_term_set.add("class");
            unique_term_set.add("arrai");
            unique_term_set.add("object");
            unique_term_set.add("data");
            unique_term_set.add("can");
            unique_term_set.add("do");
            unique_term_set.add("panda");
            unique_term_set.add("why");
            unique_term_set.add("creat");
            unique_term_set.add("variabl");
            unique_term_set.add("work");
            unique_term_set.add("code");
            unique_term_set.add("doe");
            unique_term_set.add("return");
            unique_term_set.add("what");
            unique_term_set.add("multipl");
            unique_term_set.add("call");
            unique_term_set.add("differ");
            unique_term_set.add("column");
            unique_term_set.add("find");
            unique_term_set.add("type");
            unique_term_set.add("loop");
            unique_term_set.add("run");
            unique_term_set.add("number");
            unique_term_set.add("read");
            unique_term_set.add("convert");
            unique_term_set.add("window");
            unique_term_set.add("templat");
            unique_term_set.add("datafram");
            unique_term_set.add("name");
            unique_term_set.add("wai");
            unique_term_set.add("my");
            unique_term_set.add("set");
            unique_term_set.add("text");
            unique_term_set.add("on");
        }

        Logger.logDebug("Start walking...");
        Files.walkFileTree(doc_directory, new SimpleFileVisitor<Path>() {
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
                        if(unique_term_set.contains(token_value)) {
//                            unique_term_set.add(token_value); // add to set of unique terms
                            out_sets.println(token_value + " " + file.getFileName().toString()); // add to output
                        }
                        //IR_project.Logger.logDebug("%s %s", token_value, IR_project.Utils.getDocumentID(file));
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                return FileVisitResult.CONTINUE;
            }
        });

        for(String unique_token : unique_term_set)
        {
            out_terms.println(unique_token);
        }
    }
}

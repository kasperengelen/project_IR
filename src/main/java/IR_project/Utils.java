package IR_project;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;

public class Utils
{
    /**
     * Retrieve the document ID of the document located at the specified path.
     *
     * @param document_file The path of the document.
     */
    public static String getDocumentID(Path document_file)
    {
        return document_file.getFileName().toString().toLowerCase().replace(".xml", "").toUpperCase();
    }

    /**
     * Retrieve an instance of the analyzer used by the project.
     */
    public static Analyzer getAnalyzer() {
        return new EnglishAnalyzer();
    }

    public static void logHighFrequencyTerms(Path index_path) {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(index_path));
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(Constants.DEFAULT_SIM_SEARCHER);

            HighFreqTerms.DocFreqComparator cmp = new HighFreqTerms.DocFreqComparator();
            TermStats[] highFreqTitleTerms = HighFreqTerms.getHighFreqTerms(reader, 50, "title", cmp);
            TermStats[] highFreqQuestionTerms = HighFreqTerms.getHighFreqTerms(reader, 50, "question", cmp);
            TermStats[] highFreqAnswerTerms = HighFreqTerms.getHighFreqTerms(reader, 50, "answer", cmp);
            TermStats[] highFreqTagTerms = HighFreqTerms.getHighFreqTerms(reader, 50, "tags", cmp);

            File stat_dir = new File("./stats");

            if (!stat_dir.exists()) {stat_dir.mkdir();};

            PrintWriter hf_title_out = new PrintWriter(new File("./stats/hf50_title_terms .txt"));
            PrintWriter hf_question_out = new PrintWriter(new File("./stats/hf50_question_terms .txt"));
            PrintWriter hf_answer_out = new PrintWriter(new File("./stats/hf50_answer_terms .txt"));
            PrintWriter hf_tags_out = new PrintWriter(new File("./stats/hf50_tag_terms .txt"));

            for (TermStats ts : highFreqTitleTerms) {
                hf_title_out.printf("%s %d\n", ts.termtext.utf8ToString(), ts.totalTermFreq);
            }

            for (TermStats ts : highFreqQuestionTerms) {
                hf_question_out.printf("%s %d\n", ts.termtext.utf8ToString(), ts.totalTermFreq);
            }

            for (TermStats ts : highFreqAnswerTerms) {
                hf_answer_out.printf("%s %d\n", ts.termtext.utf8ToString(), ts.totalTermFreq);
            }

            for (TermStats ts : highFreqTagTerms) {
                hf_tags_out.printf("%s %d\n", ts.termtext.utf8ToString(), ts.totalTermFreq);
            }
            hf_title_out.close();
            hf_question_out.close();
            hf_answer_out.close();
            hf_tags_out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

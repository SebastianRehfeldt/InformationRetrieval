package de.hpi.ir.bingo;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.google.common.collect.Lists;

public class SearchEngineTokenizer {

    public List<String> tokenizeStopStem(Reader reader) {
        Analyzer analyzer = new StandardAnalyzer();
        try {
            TokenStream stream = analyzer.tokenStream("", reader);
            stream = new EnglishMinimalStemFilter(stream);
            stream.reset();
            //OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
            List<String> result = Lists.newArrayList();
            while (stream.incrementToken()) {
                String token = stream.getAttribute(CharTermAttribute.class).toString();
                //OffsetAttribute attribute = stream.getAttribute(OffsetAttribute.class);
                result.add(token);
                // TODO get position, return objects
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

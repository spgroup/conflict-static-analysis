package br.unb.cic.analysis.samples;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DFPBaseSample {
    private String text;

    public DFPBaseSample(String text){
        this.text = text;
    }

    public void cleanText(){
        this.normalizeWhiteSpace(); //Left
        this.removeComments();
        this.removeDuplicateWords(); //Right
    }

    private void normalizeWhiteSpace(){
        text = text.replace("  ", " ");
    }

    private void removeComments(){
        String pattern = "(\".*?\"|'.*?')|(/\\*.*?\\*/|//.*?$)";
        Pattern regex = Pattern.compile(pattern, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = regex.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                matcher.appendReplacement(buffer, matcher.group(1));
            } else {
                matcher.appendReplacement(buffer, "");
            }
        }
        matcher.appendTail(buffer);
        text = buffer.toString();
    }

    private void removeDuplicateWords(){
        String[] words = text.split(" ", -1);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i == 0 || !words[i].equals(words[i - 1]))
                result.append(words[i]);
            if (i < words.length - 1)
                result.append(" ");
        }

        text = result.toString();
    }

    public String getText(){
        return text;
    }

    public void setText(String text){
        this.text = text;
    }

}

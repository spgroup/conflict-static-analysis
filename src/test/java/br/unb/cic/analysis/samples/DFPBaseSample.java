package br.unb.cic.analysis.samples;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DFPBaseSample {
    private String text;

    public void cleanText(){
        DFPBaseSample inst = new DFPBaseSample();
        inst.normalizeWhiteSpace(); //Left
        inst.removeComments();
        inst.removeDuplicateWords(); //Right
    }

    private void normalizeWhiteSpace(){
        text.replace("  ", "");
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
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            if (!words[i].equals(words[i - 1])) {
                result.append(" ");
                result.append(words[i]);
            }
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

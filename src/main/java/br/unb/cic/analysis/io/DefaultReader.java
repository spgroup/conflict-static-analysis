package br.unb.cic.analysis.io;

import br.unb.cic.analysis.ClassChangeDefinition;
import br.unb.cic.analysis.model.Statement;
import com.sun.scenario.effect.Merge;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A merge conflict reader from a CSV file.
 * The CSV must have the following columns:
 *
 *    * the name of a class
 *    * the type of a change (source or sink)
 *    * the source code line number
 *
 * The columns should be separated by a comma.
 */
public class DefaultReader implements MergeConflictReader {

    private String fileName;

    /**
     * Constructor.
     * @param fileName the path to the CSV file
     */
    public DefaultReader(String fileName) {
        this.fileName = fileName;
    }


    @Override
    public List<ClassChangeDefinition> read() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        List<ClassChangeDefinition> res = new ArrayList<>();
        for(String s: lines) {
            StringTokenizer st = new StringTokenizer(s, ",");
            assert st.countTokens() == 3;
            String className = st.nextToken();
            Statement.Type type = st.nextElement().equals("source") ? Statement.Type.SOURCE : Statement.Type.SINK;
            Integer lineNumber = Integer.parseInt(st.nextToken());
            res.add(new ClassChangeDefinition(className, type, lineNumber));
        }
        return res;
    }
}

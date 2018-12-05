package br.unb.cic.df.analysis;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class Main {
	
	public static void main(String args[]) {
		PackManager.v().getPack("jtp").add(
			    new Transform("jtp.myTransform", new BodyTransformer() {
					@Override
					protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
						new NullPointAnalysis(new ExceptionalUnitGraph(body));
						
					}
			    }));
		
		soot.Main.main(new String[] {"-w", "-allow-phantom-refs", "-f", "J", "-keep-line-number", "-process-dir", "/Users/rbonifacio/Documents/workspace-java/tc.jar"});
	}
}

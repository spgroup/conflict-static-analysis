package br.unb.cic.analysis.samples;


public class InterproceduralTestCaseDifferentClasses {

	public void foo() {
		int x = 0;

		x = random();

		NotRelevant.blah(x);
	}

	private int random() {
		return 10;
	}



}

class NotRelevant {
	public static void blah(int val) {
		int y = val + 1;

		y = y + 1;

		System.out.println(y);
	}
}
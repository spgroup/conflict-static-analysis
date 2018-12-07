package br.unb.cic.analysis.samples;


public class InterproceduralTestCaseSameClass {

	public void foo() {
		int x = 0;

		x = random();

		bar(x);
	}

	private int random() {
		return 10;
	}

	private void bar(int val) {
		int y = val + 1;

		y = y + 1;

		System.out.println(y);
	}

}
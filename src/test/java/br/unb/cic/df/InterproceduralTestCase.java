package br.unb.cic.df;

import java.util.Random;

public class InterproceduralTestCase {
	
	public void foo() {
		int x = 0; 
		
		x = random();   				// source 
		
		bar(x); 
	}
	
	private int random() {
		return 10;
	}
	
	private void bar(int val) {
		int y = val + 1;  				// sink 
		
		System.out.println(y);
	}

}

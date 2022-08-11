package br.unb.cic.analysis.samples;

import java.io.IOException;

public class DFPSample {
    public static int main(){
        int sum, x;
        sum = 0;
        x = 1;
        while (x < 11) {
            if (x == 10){
                sum = sum + 1;
                x = x + 1;
            }
        }
        return x + sum;
    }
}

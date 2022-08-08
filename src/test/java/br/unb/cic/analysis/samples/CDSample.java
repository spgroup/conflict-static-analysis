package br.unb.cic.analysis.samples;

import java.io.IOException;

public class CDSample {
    public static void main() throws IOException {
        int x = 0;
        try {

            x = x * 3; //left

            if (x==0){
                x = x + 1;
            }
        } finally {
            x = x + 2;//right
        }
    }

}

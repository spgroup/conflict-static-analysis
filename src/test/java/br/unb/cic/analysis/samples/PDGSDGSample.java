package br.unb.cic.analysis.samples;

public class PDGSDGSample {
    public static void main(){
        int x = 0; //sink
        while (x != 0){
            x = 1;
            while (x == 1){ //sink
                if (x != 2){
                    x = 2;
                }
            }
            if (x == 3){ //source
                x = 3;
                while(x == 4){ //source
                    x = 4;
                    if (x == 5){
                        x = 5; //sink
                    }
                    x = 6;
                }
            }else{
                x = 7;
            }
            x = 8;
        }
        x = 9;
    }
}

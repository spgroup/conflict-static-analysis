package br.unb.cic.analysis.samples.ioa.retrofit;

interface GitHub {
}

public class Main {
    public static void main(String[] ars) {
        RestAdapter r = new RestAdapter.Builder().build();
        r.create(GitHub.class);
        ;
    }
}

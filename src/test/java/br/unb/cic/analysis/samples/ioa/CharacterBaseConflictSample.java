package br.unb.cic.analysis.samples.ioa;

// Conflict: [left, countDupWords():22] --> [right, countDupWhitespace():30]
public class CharacterBaseConflictSample {

    public void countFixes() {
        Character r = new Character('a'); //RIGHT
        int i = 0;
        Character l = new Character('a'); // LEFT
    }
}
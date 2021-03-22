package br.unb.cic.exceptions;

public class ValueNotHandledException extends Exception {
    private String msg;

    public ValueNotHandledException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public String getMessage() {
        return msg;
    }
}


package com.scratch;

/**
 * General Exception for Scratch Application
 */
public class ScratchAppException extends Exception {
    public ScratchAppException() {
    }

    public ScratchAppException(String message) {
        super(message);
    }

    public ScratchAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScratchAppException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return "ScratchAppException{} + \n" + super.getMessage();
    }
}

package com.scratch.scheduler;

import com.scratch.ScratchAppException;

public class SchedulingException extends ScratchAppException {
    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulingException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return "SchedulingException{} + \n" + super.getMessage();
    }
}

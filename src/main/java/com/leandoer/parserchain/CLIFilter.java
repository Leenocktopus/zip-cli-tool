package com.leandoer.parserchain;

import com.leandoer.CLIContext;

import java.util.Queue;
import java.util.logging.Logger;

public abstract class CLIFilter {
    CLIFilter next;

    public CLIFilter(CLIFilter next) {
        this.next = next;
    }

    public void doSetup(Queue<String> args, CLIContext cliContext) {
        try {
            doFilterSpecific(args, cliContext);
            if (next != null) {
                next.doSetup(args, cliContext);
            }
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("Error1: " + ex.getMessage());
            }
        }
    }

    protected abstract void doFilterSpecific(Queue<String> args, CLIContext cliContext);

}

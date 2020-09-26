package com.leandoer.parserchain;

import com.leandoer.CLIContext;

import java.util.Arrays;
import java.util.Queue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingFilter extends CLIFilter {

    public LoggingFilter(CLIFilter next) {
        super(next);
    }

    @Override
    protected void doFilterSpecific(Queue<String> args, CLIContext cliContext) {
        String argument = args.peek();
        if ("-verbose".equals(argument) || "-debug".equals(argument)) {
            args.poll();
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            Arrays.stream(logger.getHandlers()).forEach(logger::removeHandler);
            Level level = "-verbose".equals(argument) ? Level.FINE : Level.FINER;
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(level);
            logger.setLevel(level);
            logger.addHandler(consoleHandler);
        }
    }
}

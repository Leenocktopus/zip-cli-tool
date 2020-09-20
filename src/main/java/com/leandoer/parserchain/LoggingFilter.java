package com.leandoer.parserchain;

import com.leandoer.CLIContext;

import java.util.Queue;
import java.util.logging.*;

public class LoggingFilter extends CLIFilter {

    public LoggingFilter(CLIFilter next) {
        super(next);
    }

    @Override
    protected void doFilterSpecific(Queue<String> args, CLIContext cliContext) {
        String argument = args.peek();
        if ("-verbose".equals(argument) || "-debug".equals(argument)){
            args.poll();
            Handler consoleHandler = new ConsoleHandler();
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            Level level = "-verbose".equals(argument) ? Level.FINER : Level.FINEST;
            consoleHandler.setLevel(level);
            logger.setLevel(level);
            logger.addHandler(consoleHandler);

        }

    }
}

package com.leandoer.parserchain;


import com.leandoer.CLIContext;

import java.util.Queue;

public class OptionalArgumentsWrapper extends CLIFilter{


    public OptionalArgumentsWrapper(CLIFilter next) {
        super(next);
    }

    @Override
    protected void doFilterSpecific(Queue<String> args, CLIContext cliContext) {
        String argument = args.peek();
        if (argument!= null && argument.matches("^-.+")){
            throw new RuntimeException("Argument is not recognized:" + argument);
        }
    }
}

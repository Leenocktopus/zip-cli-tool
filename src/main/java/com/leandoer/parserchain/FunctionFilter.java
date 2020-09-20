package com.leandoer.parserchain;

import com.leandoer.CLIContext;
import com.leandoer.ZipService;

import java.nio.file.Path;
import java.util.Queue;
import java.util.function.BiConsumer;

public class FunctionFilter extends CLIFilter {
    ZipService zipService;

    public FunctionFilter(CLIFilter next, ZipService zipService) {
        super(next);
        this.zipService = zipService;
    }


    @Override
    protected void doFilterSpecific(Queue<String> args, CLIContext cliContext) {
        BiConsumer<Path, Path[]> consumer = null;
        String argument = args.poll();
        switch (argument) { //We can be sure that argument is not null because of previous filter
            case "-a":
                consumer = (name, paths) -> zipService.pack(name, paths);
                break;
            case "-n":
                consumer = (name, paths) -> zipService.unpack(name, paths);
                break;
            case "-u":
                consumer = (name, paths) -> zipService.createOrUpdate(name, paths);
                break;
            case "-d":
                consumer = (name, paths) -> zipService.remove(name, paths);
                break;
            default:
                throw new RuntimeException("Function argument should be one of [-anud], instead got: " + argument);
        }
        cliContext.setFunction(consumer);
    }
}

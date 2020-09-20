package com.leandoer.parserchain;

import com.leandoer.CLIContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

public class FileFilter extends CLIFilter {
    static final Path CURRENT_RELATIVE_PATH = Paths.get("");

    public FileFilter(CLIFilter next) {
        super(next);
    }

    @Override
    protected void doFilterSpecific(Queue<String> args, CLIContext cliContext) {
        Queue<Path> paths = normalizePaths(args);
        Path zipName = paths.poll();
        Path[] files = paths.toArray(new Path[]{});

        cliContext.setArchiveName(zipName);
        cliContext.setFilenames(files);
    }

    public Queue<Path> normalizePaths(Queue<String> args){
        return args.stream()
                .map(path -> CURRENT_RELATIVE_PATH.toAbsolutePath().resolve(path).normalize())
                .collect(Collectors.toCollection(LinkedList::new));

    }
}

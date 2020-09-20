package com.leandoer;


import com.leandoer.parserchain.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Application {

	/**
	 * Flags:
	 * -d deletion
	 *
	 * @param args - command line arguments
	 * @see ZipService#remove(Path, Path...)
	 * -a archivation
	 * @see ZipService#pack(Path, Path...)
	 * -n unarchivation
	 * @see ZipService#unpack(Path, Path...)
	 * -u update
	 * @see ZipService#createOrUpdate(Path, Path...)
	 * --verbose
	 * --debug
	 * --help
	 * --version
	 */
	public static void main(String[] args) {
		CLIContext applicationContext = new CLIContext();
		CLIFilter executionFilter = new ExecutionFilter(null);
		CLIFilter fileFilter = new FileFilter(executionFilter);
		CLIFilter optionalArgumentsWrapper = new OptionalArgumentsWrapper(fileFilter);
		CLIFilter loggingFilter = new LoggingFilter(optionalArgumentsWrapper);
		CLIFilter functionFilter = new FunctionFilter(loggingFilter, new ZipService());
		CLIFilter primaryFilter = new PrimaryFilter(functionFilter);
		primaryFilter.doSetup(Arrays.stream(args).collect(Collectors.toCollection(LinkedList::new)), applicationContext);
		System.out.println(Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getLevel());
	}

}
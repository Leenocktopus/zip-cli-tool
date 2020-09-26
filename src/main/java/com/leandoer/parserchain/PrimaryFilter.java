package com.leandoer.parserchain;

import com.leandoer.CLIContext;

import java.util.Queue;
import java.util.logging.Logger;

public class PrimaryFilter extends CLIFilter {
	Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public PrimaryFilter(CLIFilter next) {
		super(next);
	}

	@Override
	protected void doFilterSpecific(Queue<String> args, CLIContext cliContext) {
		String argument = args.peek();
		if (argument == null || argument.equals("-help")) {
			logger.info(String.format("%6s zip [options] \n \t\t(to execute one of commands)", "Usage:"));
			logger.info(String.format("%6s zip [options] [zip archive] [filename1, filename2, ...]\n \t\t(to execute archiver commands)", "or"));
			logger.info("\nwhere commands include:\n");
			logger.info(String.format("\t %s \t print help message to the output stream", "-info "));
			logger.info(String.format("\t %s \t print product version and developer to the output and exit", "-version"));
			logger.info("\nwhere options include:\n");
			logger.info(String.format("\t %s \t enable verbose output", "-verbose"));
			logger.info(String.format("\t %s \t enable debug output\n", "-debug"));
			logger.info(String.format("\t %s \t create archive with the name provided as [zip-archive]\n\t\t and contents provided as [filename1, filename2, ...]\n", "-a"));
			logger.info(String.format("\t %s \t unpack archive with the name provided as [zip-archive]\n\t\t and destination provided as [filename]. Using this option\n\t\t without destination will unpack files to the current directory.\n\t\t Multiple destinations produce exception.\n", "-n"));
			logger.info(String.format("\t %s \t update archive with the name provided as [zip-archive]\n\t\t and contents provided as [filename1, filename2, ...]\n" ,"-u"));
			logger.info(String.format("\t %s \t remove files provided as [filename1, filename2, ...]\n\t\t from archive provided [zip-archive]. Each filename \n\t\t should be an absolute path in the archive.\n","-d"));
			this.next = null;
		} else if (argument.equals("-version")) {
			logger.info("\tZIP CLI Archiver - Version 0.0.3");
			logger.info("\tDeveloped by Alexey Raichev in 2020");
			logger.info("\tmileenocktopus@gmail.com");
			args.poll();
			this.next = null;
		}
	}
}

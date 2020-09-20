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
			logger.info("-info");
			logger.info("-version");
			logger.info("-verbose");
			logger.info("-a");
			logger.info("-n");
			logger.info("-u");
			logger.info("-d");
			logger.info("zip [-anud] [ZIP_NAME] [FILE1, FILE2, ..]");
			this.next = null;
		} else if (argument.equals("-version")) {
			logger.info("Zip CLI Archiver - Version 0.0.1");
			logger.info("Developed by Alexey Raichev in 2020");
			args.poll();
			this.next = null;
		}
	}
}

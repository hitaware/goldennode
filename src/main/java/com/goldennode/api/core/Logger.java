package com.goldennode.api.core;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	public static void log(String log, Severity severity) {
		if (log.contains("ping") || log.contains("pong")) {
			return;
		}
		PrintStream ps;
		if (severity == Severity.ERROR) {
			ps = System.err;
		} else {
			ps = System.out;
		}

		ps.println("\n"
				+ new SimpleDateFormat("yyyyMMdd HH:mm:ss SS")
						.format(new Date()) + "\n" + log);
	}

	public static void debug(String log) {
		log(log, Severity.DEBUG);
	}

	public static void info(String log) {
		log(log, Severity.INFO);
	}

	public static void warning(String log) {
		log(log, Severity.WARNING);
	}

	public static void error(String log) {
		log(log, Severity.DEBUG);
	}

	public static void error(Exception e) {

		PrintStream ps;

		ps = System.err;

		ps.println("\n"
				+ new SimpleDateFormat("yyyyMMdd HH:mm:ss SS")
						.format(new Date()) + "\n"/* + e.toString() */);
		e.printStackTrace(ps);
	}

	public static enum Severity {
		DEBUG, ERROR, INFO, WARNING
	}
}

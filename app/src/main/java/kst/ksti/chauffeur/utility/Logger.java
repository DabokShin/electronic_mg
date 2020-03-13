package kst.ksti.chauffeur.utility;

import android.util.Log;

import kst.ksti.chauffeur.BuildConfig;


public class Logger {

	public static void v(String msg) {
		logger(Log.VERBOSE, msg);
	}

	public static void d(String msg) {
		logger(Log.DEBUG, msg);
	}

	public static void i(String msg) {
		logger(Log.INFO, msg);
	}

	public static void w(String msg) {
		logger(Log.WARN, msg);
	}

	public static void e(String msg) {
		logger(Log.ERROR, msg);
	}

	private static void logger(int priority, String msg) {
		if (BuildConfig.DEBUG) {
			try {
				StringBuilder msgBuilder = new StringBuilder();
				msgBuilder.append("[").append(Thread.currentThread().getStackTrace()[4].getMethodName())
						.append("()").append("]").append(" :: ").append(msg)
						.append(" (").append(Thread.currentThread().getStackTrace()[4].getFileName()).append(":")
						.append(Thread.currentThread().getStackTrace()[4].getLineNumber()).append(")");

				Log.println(priority, "<PHD> // "+Thread.currentThread().getStackTrace()[4].getFileName().replace(".java", ""), msgBuilder.toString());

			} catch (Exception e) {
				Log.e("<PHD>", msg);
			}
		}
	}
}

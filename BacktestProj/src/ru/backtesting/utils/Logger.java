package ru.backtesting.utils;

import java.text.DecimalFormat;

public class Logger {
    private final DecimalFormat df = new DecimalFormat("0.00");;

	private static Logger instance;
	
	public static synchronized Logger log() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}
	
	public String doubleLog(double number) {
		return df.format(number);
	}
}

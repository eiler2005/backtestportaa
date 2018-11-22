package ru.backtesting.utils;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

public class Logger {
    private final DecimalFormat df = new DecimalFormat("0.00");;

	private static Logger instance;

	private PrintStream out;

    private Logger() {
    	try {
			out = new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
    }
    
	public static synchronized Logger log() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}
	
	public String doubleLog(double number) {
		return df.format(number);
	}
	
	public void info(String info) {
		out.println(info);
	}
}

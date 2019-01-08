package ru.backtesting.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataСonverterForHTMLUse {
    public static final DateTimeFormatter DATE_FORMAT_SIMPLE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	public static String printDate(LocalDate date) {
		return date.format(DATE_FORMAT_SIMPLE);
	}

	public static String printDates(LocalDate[] dates) {
		String str = "[";
		
		for (int i = 0; i < dates.length; i++) {
			if ( i != dates.length - 1 )
				str += "'" + printDate(dates[i]) + "', ";
			else
				str += "'" + printDate(dates[i]) + "']";
		}
		
		return str;
	}

	public static String printTextForHtmlUsed(double[] text) {
		String str = "[";
		
		for (int i = 0; i < text.length; i++) {
			if ( i != text.length - 1 )
				str += "'" + text[i] + "', ";
			else
				str += "'" + text[i] + "']";
		}
		
		return str;
	}

}

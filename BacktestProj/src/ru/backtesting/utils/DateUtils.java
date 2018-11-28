package ru.backtesting.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
	public static boolean compareDatesByDay(LocalDateTime date1, LocalDateTime date2) {
		return date1.getYear() == date2.getYear() && date1.getMonth().equals(date2.getMonth()) && 
				date1.getDayOfMonth() == date2.getDayOfMonth();
	}
	
	public static LocalDateTime dateFromString(String str) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return LocalDateTime.parse(str, formatter);
	}
}

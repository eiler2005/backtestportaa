package ru.backtesting.utils;

import java.time.LocalDateTime;

public class DateUtils {
	public static boolean compareDatesByDay(LocalDateTime date1, LocalDateTime date2) {
		return date1.getYear() == date2.getYear() && date1.getMonth().equals(date2.getMonth()) && 
				date1.getDayOfMonth() == date2.getDayOfMonth();
	}
}

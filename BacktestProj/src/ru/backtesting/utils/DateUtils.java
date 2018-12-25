package ru.backtesting.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DateUtils {
	public static final String TIME_ZONE = "America/Los_Angeles";
	
	public static boolean compareDatesByDay(LocalDateTime date1, LocalDateTime date2) {
		return date1.getYear() == date2.getYear() && date1.getMonth().equals(date2.getMonth()) && 
				date1.getDayOfMonth() == date2.getDayOfMonth();
	}
	
	public static LocalDateTime dateTimeFromString(String str) {
		return LocalDateTime.parse(str, Logger.DATE_FORMAT);
	}
	
	public static LocalDate dateFromString(String str) {
		return LocalDate.parse(str, Logger.DATE_FORMAT);
	}
	
	@Deprecated
	public LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.of(TIME_ZONE)).toLocalDateTime();
	}
	
	public static Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.of(TIME_ZONE)).toInstant());
	}

	public static Date asDate(LocalDateTime localDateTime) {
	    return Date.from(localDateTime.atZone(ZoneId.of(TIME_ZONE)).toInstant());
	}

	public static LocalDate asLocalDate(Date date) {
	    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.of(TIME_ZONE)).toLocalDate();
	}

	public static LocalDateTime asLocalDateTime(Date date) {
	    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.of(TIME_ZONE)).toLocalDateTime();
	}
	
	public static List<LocalDate> getDatesBetweenUsingJava8(LocalDate startDate, LocalDate endDate) { 
		long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate); 
		return IntStream.iterate(0, i -> i + 1).limit(numOfDaysBetween).mapToObj(i -> startDate.plusDays(i)).collect(Collectors.toList()); 
	}
	
	public static Duration duration(LocalDate startDate, LocalDate endDate) {
		return Duration.between(startDate.atStartOfDay(ZoneId.of(TIME_ZONE)), endDate.atStartOfDay(ZoneId.of(TIME_ZONE)));
	}
}

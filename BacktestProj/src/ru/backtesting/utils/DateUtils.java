package ru.backtesting.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.patriques.output.AlphaVantageException;

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
	
	public static List<LocalDate> asLocalDate(List<LocalDateTime> localDateTime) {
		List<LocalDate> localDate = new ArrayList<LocalDate>();
		
		for (LocalDateTime date : localDateTime)
			localDate.add(date.toLocalDate());
		
		return localDate;
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
	
	@Deprecated // нужны трейдерские дни
	public static Duration duration(LocalDate startDate, LocalDate endDate) {
		return Duration.between(startDate.atStartOfDay(ZoneId.of(TIME_ZONE)), endDate.atStartOfDay(ZoneId.of(TIME_ZONE)));
	}
	
	public static List<LocalDateTime> filterDateListByYear(List<LocalDateTime> dates, int startYear, int endYear) {
		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();
		
		for (LocalDateTime curDate : dates  ) 
		    if (curDate.getYear() >= startYear && curDate.getYear() <= endYear) 
		    	filteredDates.add(curDate);
	
		return filteredDates;
	}
	
	public static List<LocalDateTime> filteredDatesByAnnyally(List<LocalDateTime> dates, int startYear, int endYear) {
		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();
		
		// первый торговый день года
		filteredDates.add(getFirsDayByParams(dates, Month.JANUARY, startYear));	
		
		for (int curYear = startYear; curYear <= endYear - 1; curYear++) {
			// последний торговый день года
			filteredDates.add(getLastDayOfYear(dates, curYear));
		}
		
		// последний торговый день года
		filteredDates.add(getLastDayOfYear(dates, endYear));

		return filteredDates;
	}
	
	public static List<LocalDateTime> filteredDatesByMonth(List<LocalDateTime> dates, int startYear, int endYear) {
		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();

		// первый торговый день года
		LocalDateTime firsJunuaryDate = getFirsDayByParams(dates, Month.JANUARY, startYear);

		filteredDates.add(firsJunuaryDate);
		
		for (int curYear = startYear; curYear <= endYear; curYear++) {
			LocalDateTime lasMonthOfYear =  getLastDayOfYear(dates, curYear);
	    	
			int numberLastMonthOfYear = monthToInt(lasMonthOfYear.getMonth());
			
			if ( numberLastMonthOfYear >= 1 )
				filteredDates.add(getLastDayByParams(dates, Month.JANUARY, curYear));
			if ( numberLastMonthOfYear >= 2 )
				filteredDates.add(getLastDayByParams(dates, Month.FEBRUARY, curYear));
			if ( numberLastMonthOfYear >= 3 )
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
	    	if ( numberLastMonthOfYear >= 4 )
				filteredDates.add(getLastDayByParams(dates, Month.APRIL, curYear));
	    	if ( numberLastMonthOfYear >= 5 )
				filteredDates.add(getLastDayByParams(dates, Month.MAY, curYear));
	    	if ( numberLastMonthOfYear >= 6 ) 
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
	    	if ( numberLastMonthOfYear >= 7 )
				filteredDates.add(getLastDayByParams(dates, Month.JULY, curYear));
	    	if ( numberLastMonthOfYear >= 8 )
				filteredDates.add(getLastDayByParams(dates, Month.AUGUST, curYear));
	    	if ( numberLastMonthOfYear >= 9 )
				filteredDates.add(getLastDayByParams(dates, Month.SEPTEMBER, curYear));
	    	if ( numberLastMonthOfYear >= 10 )
				filteredDates.add(getLastDayByParams(dates, Month.OCTOBER, curYear));
	    	if ( numberLastMonthOfYear >= 11 )
				filteredDates.add(getLastDayByParams(dates, Month.NOVEMBER, curYear));
	    	if ( numberLastMonthOfYear >= 12 )
				filteredDates.add(getLastDayByParams(dates, Month.DECEMBER, curYear));
		}
		
		return filteredDates;
	}

	@Deprecated
	public static List<LocalDateTime> filteredDatesByWeek(List<LocalDateTime> dates, int startYear, int endYear) {
		
		throw new AlphaVantageException("Метод еще не реализован");
	}
		
	
	public static List<LocalDateTime> filteredDatesByQuarter(List<LocalDateTime> dates, int startYear, int endYear) {
		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();

		// первый торговый день года
		LocalDateTime firsJanuaryDate = getFirsDayByParams(dates, Month.JANUARY, startYear);

		filteredDates.add(firsJanuaryDate);
		
		for (int curYear = startYear; curYear <= endYear; curYear++) {
			LocalDateTime lasMonthOfYear =  getLastDayOfYear(dates, curYear);
	    	
			int numberLastMonthOfYear = monthToInt(lasMonthOfYear.getMonth());
			
			if ( numberLastMonthOfYear >= 0 && numberLastMonthOfYear <= 2) {
				filteredDates.add(getLastDayByParams(dates, lasMonthOfYear.getMonth(), curYear));
	    	} else if ( numberLastMonthOfYear == 3) {
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
	    	} else if ( numberLastMonthOfYear >= 4 && numberLastMonthOfYear <= 5) {
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
				filteredDates.add(getLastDayByParams(dates, lasMonthOfYear.getMonth(), curYear));
	    	} else if ( numberLastMonthOfYear == 6) {
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
	    	} else if ( numberLastMonthOfYear >= 7 && numberLastMonthOfYear <= 8) {
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));

				filteredDates.add(getLastDayByParams(dates, lasMonthOfYear.getMonth(), curYear));
	    	} else if ( numberLastMonthOfYear == 9) {
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.SEPTEMBER, curYear));
	    	}
	    	else if ( numberLastMonthOfYear >= 10 && numberLastMonthOfYear <= 11) {
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.SEPTEMBER, curYear));
	
				filteredDates.add(getLastDayByParams(dates, lasMonthOfYear.getMonth(), curYear));
	    	}
	    	else if ( numberLastMonthOfYear == 12) {
				filteredDates.add(getLastDayByParams(dates, Month.MARCH, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.SEPTEMBER, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.DECEMBER, curYear));
	    	}
		}

		return filteredDates;
	}
	
	public static List<LocalDateTime> filteredDatesBySemiAnnually(List<LocalDateTime> dates, int startYear, int endYear) {
		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();

		// первый торговый день года
		LocalDateTime firsJunuaryDate = getFirsDayByParams(dates, Month.JANUARY, startYear);

		filteredDates.add(firsJunuaryDate);
		
		for (int curYear = startYear; curYear <= endYear; curYear++) {
			LocalDateTime lasMonthOfYear =  getLastDayOfYear(dates, curYear);
	    	
			int numberLasMonthOfYear = monthToInt(lasMonthOfYear.getMonth());
			
			if ( numberLasMonthOfYear >= 0 && numberLasMonthOfYear <= 5) {
				filteredDates.add(getLastDayByParams(dates, lasMonthOfYear.getMonth(), curYear));
			}
	    	else if ( numberLasMonthOfYear == 6 ) {
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
	    	} else if ( numberLasMonthOfYear >= 7 && numberLasMonthOfYear <= 11) {
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
				filteredDates.add(getLastDayByParams(dates, lasMonthOfYear.getMonth(), curYear));
	    	} if ( numberLasMonthOfYear == 12 ) {
				filteredDates.add(getLastDayByParams(dates, Month.JUNE, curYear));
				filteredDates.add(getLastDayByParams(dates, Month.DECEMBER, curYear));
	    	}
		}

		return filteredDates;
	}
	
	public static Month monthIntegerToMonth(int monthNumber) {
		if ( monthNumber == 1 )
			return Month.JANUARY;
		if ( monthNumber == 2 )
			return Month.FEBRUARY;
		if ( monthNumber == 3 )
			return Month.MARCH;
		if ( monthNumber == 4 )
			return Month.APRIL;
		if ( monthNumber == 5 )
			return Month.MAY;
		if ( monthNumber == 6 )
			return Month.JUNE;
		if ( monthNumber == 7 )
			return Month.JULY;
		if ( monthNumber == 8 )
			return Month.AUGUST;
		if ( monthNumber == 9 )
			return Month.SEPTEMBER;
		if ( monthNumber == 10 )
			return Month.OCTOBER;
		if ( monthNumber == 11 )
			return Month.NOVEMBER;
		if ( monthNumber == 12 )
			return Month.DECEMBER;
		
		throw new RuntimeException("Нет месяца с номером " + monthNumber);
	}

	public static int monthToInt(Month month) {
		switch( month.toString() ) {
	    	case "JANUARY" : 
	    		return 1;
	    	case "FEBRUARY" : 
	    		return 2;
	    	case "MARCH" : 
	    		return 3;
	    	case "APRIL" : 
	    		return 4;
	    	case "MAY" : 
	    		return 5;
	    	case "JUNE" : 
	    		return 6;
	    	case "JULY" : 
	    		return 7;
	    	case "AUGUST" : 
	    		return 8;
	    	case "SEPTEMBER" : 
	    		return 9;
	    	case "OCTOBER" : 
	    		return 10;
	    	case "NOVEMBER" : 
	    		return 11;
	    	case "DECEMBER" : 
	    		return 12;
		}
		
		return -1;
	}
	
	public static LocalDateTime getLastDayByParams(List<LocalDateTime> dates, Month month, int startYear) {
		List<LocalDateTime> filteredDates = filteredDaysByParams(dates, month, startYear);
		
		if ( filteredDates.size() == 0 )
			throw new AlphaVantageException("Для года " + startYear + " в " + month + " не загружены котировки для ребалансировки");
		
		return filteredDates.get(filteredDates.size() - 1);
	}
	
	
	private static  LocalDateTime getLastDayOfYear(List<LocalDateTime> dates, int endYear) {
		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();
		
		for (LocalDateTime date : dates)
			if (date.getYear() == endYear)
				filteredDates.add(date);
		
		if ( filteredDates.size() == 0 )
			throw new AlphaVantageException("Для года " + endYear + " не загружены котировки для ребалансировки");
		
		return filteredDates.get(filteredDates.size() - 1);
	}
	
	public static  LocalDateTime getFirsDayByParams(List<LocalDateTime> dates, Month month, int startYear) {
		List<LocalDateTime> filteredDates = filteredDaysByParams(dates, month, startYear);
		
		if ( filteredDates.size() == 0 )
			throw new AlphaVantageException("Для года " + startYear + " в " + month + " не загружены котировки для ребалансировки");
		
		return filteredDates.get(0);		
	}
	
	private static  List<LocalDateTime> filteredDaysByParams(List<LocalDateTime> dates, Month month, int startYear) {
		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();
		
		for (LocalDateTime date : dates)
			if (date.getYear() == startYear && date.getMonth() == month)
				filteredDates.add(date);
		
		return filteredDates;		
	}
}

package ru.backtesting.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TypeUtils {
	public static double[] toPrimitive(List<Double> list) {
		if (list == null) {
			return null;
		} else if (list.size() == 0) {
			double[] empty = {};
			return empty;
		}
		
		final double[] result = new double[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i).doubleValue();
		}
		
		return result;
	}
	
	public static String[] toStringArray(Object[] arr) {
		return Arrays.copyOf(arr, arr.length, String[].class);
	}
	
	public static Double[] toDoubleArray(Object[] arr) {
		return Arrays.copyOf(arr, arr.length, Double[].class);
	}
	
	public static LocalDate[] toLocalDateArray(Object[] arr) {
		return Arrays.copyOf(arr, arr.length, LocalDate[].class);
	}
	
	public static Object[] toObjectArray(Collection<?> list) {
		if (list == null) {
			return null;
		} else if (list.size() == 0) {
			LocalDate[] empty = {};
			return empty;
		}
		
		final Object[] result = new Object[list.size()];
		
		int i = 0;
		
		for(Object obj : list ) {
			result[i] = obj;
			
			i++;
		}
		
		return result;
	}
	
	public static boolean isStringCollection(Collection<?> col) {
		if ( col.size() > 0 && (col.iterator().next() instanceof String) )
			return true;
		
		return false;
	}
	
	public static boolean isDateCollection(Collection<?> col) {
		if ( col.size() > 0 && (col.iterator().next() instanceof LocalDateTime) )
			return true;
		
		return false;
	}
	
	public static boolean isDoubleCollection(Collection<?> col) {
		if ( col.size() > 0 && (col.iterator().next() instanceof Double) )
			return true;
		
		return false;
	}
}

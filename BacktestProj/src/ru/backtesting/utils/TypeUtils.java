package ru.backtesting.utils;

import java.time.LocalDate;
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
	
	public static Object[] toObjectArray(List<Object> list) {
		if (list == null) {
			return null;
		} else if (list.size() == 0) {
			LocalDate[] empty = {};
			return empty;
		}
		
		final Object[] result = new Object[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		
		return result;
	}
}

package ru.backtesting.utils.doubles;

import java.util.ArrayList;
import java.util.Collection;

public class DeduplicateValues {
	private Collection<String> addedValues;
	
	public DeduplicateValues() {
		this.addedValues = new ArrayList<String>();
	}
	
	public String check(String value) {
		if ( containsIgnoreCase(addedValues, value) )
			return value + "_";
		else {
			addedValues.add(value);
			
			return value;
		}
	}
	
	private boolean containsIgnoreCase(Collection<String> collection, String value) {
		for(String str : collection) {
			if ( str.equalsIgnoreCase(value) )
				return true;
		}
		
		return false;
	}
}

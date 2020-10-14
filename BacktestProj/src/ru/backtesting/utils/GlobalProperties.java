package ru.backtesting.utils;

public class GlobalProperties {
	private static GlobalProperties prop;
	
	private GlobalProperties() {
	}
	
	public static synchronized GlobalProperties instance() {
		if ( prop == null )
			prop = new GlobalProperties();
		
		return prop;
	}
	
	// считать портфель если нет котировок и обходиться имеющимися
	public boolean isSoftQuotesInPort() {
		return true;
	}
	
	// trace method invoke tree
	public boolean traceWithMethodInvoke() {
		return true;
	}
	
	// trace method invoke tree
	public boolean logExcelAdded() {
		return false;
	}
}

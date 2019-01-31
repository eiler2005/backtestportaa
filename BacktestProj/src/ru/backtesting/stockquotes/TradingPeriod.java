package ru.backtesting.stockquotes;

public enum TradingPeriod {
	Monthly("Monthly"), 
	Weekly("Weekly"), 
	Daily("Daily");
	
	final String value;

	TradingPeriod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

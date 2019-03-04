package ru.backtesting.stockquotes;

public enum TradingTimeFrame {
	Monthly("Monthly"), 
	Weekly("Weekly"), 
	Daily("Daily");
	
	final String value;

	TradingTimeFrame(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

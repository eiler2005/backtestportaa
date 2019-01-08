package ru.backtesting.mktindicators.base;

public enum MarketIndicatorInterval {
	
	Daily("Daily"),
	Weekly("Weekly"),
	Monthly("Monthly");

    final String value;

    MarketIndicatorInterval(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

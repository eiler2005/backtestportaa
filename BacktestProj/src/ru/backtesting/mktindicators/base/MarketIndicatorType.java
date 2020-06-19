package ru.backtesting.mktindicators.base;

public enum MarketIndicatorType {
	SMA_IND("SMA"),
	EMA_IND("EMA"),
	WMA_IND("WMA"),
	KaufmanAdaptiveMA_IND("KAMA"),


	RSI_OSC("RSI"),
	CHANDE_MOMENTUM_OSC("CMO"),
	BOLLINGER_BANDS("bbands"),
	
	HILBER_TRANSFORM_TRENDLINE("ht_trendline"),
	
	OBV("On balance volume");
	
    final String value;

    MarketIndicatorType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

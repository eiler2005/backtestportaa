package ru.backtesting.mktindicators.base;

import java.time.LocalDateTime;

@Deprecated
public class SignalActionContext  {
	private MarketIndicatorInterface action;
	
	public MarketIndicatorInterface getSignal() {
		return action;
	}
	
	public void setSignalAction(MarketIndicatorInterface action) {
		this.action = action;
	}
	
	public int testSignal (LocalDateTime date, String ticker) {
		return action.testSignal(date, ticker);
	}
}

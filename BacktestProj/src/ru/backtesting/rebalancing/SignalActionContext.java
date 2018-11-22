package ru.backtesting.rebalancing;

import java.time.LocalDateTime;

public class SignalActionContext {
	private SignalTestingAction action;
	
	public SignalTestingAction getSignal() {
		return action;
	}
	public void setSignal(SignalTestingAction action) {
		this.action = action;
	}
	
	public int testSignal (LocalDateTime date, String ticker) {
		return action.testSignal(date, ticker);
	}
}

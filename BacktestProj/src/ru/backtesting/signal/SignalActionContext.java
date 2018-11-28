package ru.backtesting.signal;

import java.time.LocalDateTime;

@Deprecated
public class SignalActionContext  {
	private SignalTestingAction action;
	
	public SignalTestingAction getSignal() {
		return action;
	}
	
	public void setSignalAction(SignalTestingAction action) {
		this.action = action;
	}
	
	public int testSignal (LocalDateTime date, String ticker) {
		return action.testSignal(date, ticker);
	}
}

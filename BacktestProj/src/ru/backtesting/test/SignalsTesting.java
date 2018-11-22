package ru.backtesting.test;

import ru.backtesting.signal.RSITechnicalSignal;
import ru.backtesting.signal.SignalActionContext;

public class SignalsTesting {

	public static void main(String[] args) {
		SignalActionContext portAction = new SignalActionContext();
		
		portAction.setSignal(new RSITechnicalSignal());
		
		int result = portAction.testSignal(null, null);
	}
}

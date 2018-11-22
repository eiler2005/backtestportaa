package ru.backtesting.rebalancing.test;

import ru.backtesting.rebalancing.RSITechnicalSignal;
import ru.backtesting.rebalancing.SignalActionContext;

public class SignalsTesting {

	public static void main(String[] args) {
		SignalActionContext portAction = new SignalActionContext();
		
		portAction.setSignal(new RSITechnicalSignal());
		
		int result = portAction.testSignal();
	}
}

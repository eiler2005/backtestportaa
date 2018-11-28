package ru.backtesting.test;

import ru.backtesting.signal.RSIOscillatorSignal;
import ru.backtesting.signal.SMATechIndicatorSignal;
import ru.backtesting.utils.DateUtils;

public class SignalsTesting {

	public static void main(String[] args) {
		// SignalActionContext smaContext = new SignalActionContext();
		
		SMATechIndicatorSignal smaSignalAction = new SMATechIndicatorSignal(50, 200);
				
		// smaContext.setSignalAction(smaSignalAction);
						
		System.out.println(smaSignalAction.testSignal(DateUtils.dateFromString("2015-07-31 00:00"), "SPY"));
		System.out.println(smaSignalAction.testSignal(DateUtils.dateFromString("2018-11-23 00:00"), "SPY"));
		
		RSIOscillatorSignal rsiOsc = new RSIOscillatorSignal(14);
		
		rsiOsc.testSignal(DateUtils.dateFromString("2018-11-23 00:00"), "SPY");
		rsiOsc.testSignal(DateUtils.dateFromString("2018-11-23 00:00"), "MTUM");

	}
}

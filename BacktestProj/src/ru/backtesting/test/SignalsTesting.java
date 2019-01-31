package ru.backtesting.test;

import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.stockquotes.TradingPeriod;
import ru.backtesting.utils.DateUtils;

public class SignalsTesting {

	public static void main(String[] args) {
		// SignalActionContext smaContext = new SignalActionContext();
		
		MovingAverageIndicatorSignal smaSignalActionDaily = new MovingAverageIndicatorSignal(50, 200, MarketIndicatorType.SMA_IND, TradingPeriod.Daily);
				
		// smaContext.setSignalAction(smaSignalAction);
						
		System.out.println(smaSignalActionDaily.testSignal(DateUtils.dateTimeFromString("2018-07-27 00:00"), "SPY"));
		System.out.println(smaSignalActionDaily.testSignal(DateUtils.dateTimeFromString("2018-12-28 00:00"), "SPY"));
		
		MovingAverageIndicatorSignal smaSignalActionWeekly = new MovingAverageIndicatorSignal(50, 200, MarketIndicatorType.SMA_IND, TradingPeriod.Weekly);

		System.out.println(smaSignalActionWeekly.testSignal(DateUtils.dateTimeFromString("2018-07-27 00:00"), "SPY"));
		System.out.println(smaSignalActionWeekly.testSignal(DateUtils.dateTimeFromString("2018-12-28 00:00"), "SPY"));
		
		RSIOscillatorSignal rsiOsc = new RSIOscillatorSignal(14, TradingPeriod.Daily);
		
		rsiOsc.testSignal(DateUtils.dateTimeFromString("2018-07-27 00:00"), "SPY");
		rsiOsc.testSignal(DateUtils.dateTimeFromString("2018-12-28 00:00"), "MTUM");

	}
}

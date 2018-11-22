package ru.backtesting.rebalancing;

import java.time.LocalDateTime;

public interface SignalTestingAction {
	public int testSignal(LocalDateTime date, String ticker);
}

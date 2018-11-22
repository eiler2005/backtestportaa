package ru.backtesting.signal;

import java.time.LocalDateTime;

public interface SignalTestingAction {
	public int testSignal(LocalDateTime date, String ticker);
}

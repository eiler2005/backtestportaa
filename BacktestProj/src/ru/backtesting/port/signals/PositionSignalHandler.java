package ru.backtesting.port.signals;

import java.time.LocalDateTime;
import java.util.List;

import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;

public interface PositionSignalHandler {
	public boolean handleOnDate(LocalDateTime prevDate, LocalDateTime date, Portfolio port, boolean logging);
	public List<PositionInformation> getPositions();
	public double getBalance();
	public void handleAllocations(List<AssetAllocPerfInf> allocList);

}

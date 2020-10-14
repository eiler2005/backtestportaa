package ru.backtesting.port.base.aa;

import java.time.LocalDateTime;

import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AssetAllocation;

public interface AssetAllocPerfInf extends AssetAllocation {
	public LocalDateTime getStartDate();

	public double getStockQuoteStart();
	
	public AllocChoiceModelType getAllocModelType();
}

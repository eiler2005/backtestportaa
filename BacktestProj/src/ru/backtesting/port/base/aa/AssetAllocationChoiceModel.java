package ru.backtesting.port.base.aa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.ticker.TickerInf;

public interface AssetAllocationChoiceModel {
	public List<AssetAllocPerfInf> calculateAllocation(LocalDateTime date, List<? extends AssetAllocation> assetsAllocEtalon, List<TickerInf> tickers, TickerInf outOfMarketPos, 
			LocalDateTime launchDate);
	public AllocChoiceModelType getType();
	public List<String> getRiskOnOffTickers();
	public Map<String, Object> getExportModelParams();
}

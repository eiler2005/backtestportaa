package ru.backtesting.port.base.aa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import ru.backtesting.port.base.AllocChoiceModelType;

public interface AssetAllocationChoiceModel {
	public List<AssetAllocPerfInf> calculateAllocation(LocalDateTime date, List<String> tickers, String outOfMarketPosTicker, 
			LocalDateTime launchDate);
	public AllocChoiceModelType getType();
	public List<String> getRiskOnOffTickers();
	public Map<String, Object> getExportModelParams();
}

package ru.backtesting.port.base.aa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.port.base.AllocChoiceModelType;

public class FixedAssetAllocModel implements AssetAllocationChoiceModel {
	@Override
	public AllocChoiceModelType getType() {
		return AllocChoiceModelType.FixedAssetAllocation;
	}

	@Override
	public List<String> getRiskOnOffTickers() {
		return new ArrayList<String>();
	}

	@Override
	public List<AssetAllocPerfInf> calculateAllocation(LocalDateTime date, List<String> tickers,
			String outOfMarketPosTicker, LocalDateTime launchDate) {
		throw new NotImplementedException("Для типа аллокации " + getType() + 
				" распределение активов остается постоянным. Данный метод вызывать нет необходимости");	}

	@Override
	public Map<String, Object> getExportModelParams() {
		throw new NotImplementedException("Метод еще не реализован");
	}
}

package ru.backtesting.port.base.aa.sma;

import java.time.LocalDateTime;

import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;

public class MovingAverageAssetAllocInf extends MomAssetAllocPerfInf {
	private MovingAverageIndicatorSignal smaSignal;
	
	public MovingAverageAssetAllocInf(String ticker, LocalDateTime endDate,
			double stockQuoteEnd, MovingAverageIndicatorSignal smaSignal) {
		super(ticker, null, endDate, 0, stockQuoteEnd, 0);

		this.smaSignal = smaSignal;
	}

	@Override
	public AllocChoiceModelType getAllocModelType() {
		return AllocChoiceModelType.MovingAveragesForAsset;
	}
	
	@Override
	public String toString() {
		return "MovingAverageAssetAllocInf [ticker=" + getTickerInf() + ", allocPercent=" + allocPercent + ", smaSignal = " + smaSignal + "]";
	}

	public MovingAverageIndicatorSignal getSmaSignal() {
		return smaSignal;
	}
	
	@Override
	public double getStockQuoteStart() {
		throw new NotImplementedException("Метод не поддерживается для типа " + getAllocModelType());
	}
	
	@Override
	public LocalDateTime getStartDate() {
		throw new NotImplementedException("Метод не поддерживается для типа " + getAllocModelType());
	}

	@Override
	public double getPercGrowth() {
		throw new NotImplementedException("Метод не поддерживается для типа " + getAllocModelType());
	}
}

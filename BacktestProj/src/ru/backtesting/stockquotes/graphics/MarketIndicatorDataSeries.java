package ru.backtesting.stockquotes.graphics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.patriques.output.AlphaVantageException;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.stockquotes.graphics.base.BaseFinancialTimeSeriesChartInformation;
import ru.backtesting.stockquotes.graphics.base.FinancialTimeSeriesChartInformation;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;

public class MarketIndicatorDataSeries extends BaseFinancialTimeSeriesChartInformation 
	implements FinancialTimeSeriesChartInformation {
	private MarketIndicatorInterface indicator;
	private String indicatorTitle;
	
	public MarketIndicatorDataSeries(String ticker, MarketIndicatorInterface indicator, String indicatorTitle, List<LocalDateTime> dates,
			List<Double> values, List<String> tooltips) {
		super();
		super.ticker = ticker;
		this.indicator = indicator;
		this.indicatorTitle = indicatorTitle;
		super.dates = dates;
		super.values = values;
		super.tooltips = tooltips;
	}
	
	public MarketIndicatorDataSeries(String ticker, MarketIndicatorInterface indicator, String indicatorTitle, int startYear, int endYear, TradingTimeFrame period) {
		super();
		super.ticker = ticker;
		this.indicator = indicator;
		this.indicatorTitle = indicatorTitle;
		
		fillFromStorage(startYear, endYear, period);
	}

	private void fillFromStorage(int startYear, int endYear, TradingTimeFrame period) {	
		int period1 = indicator.getTimePeriod();
		int period2 = 0;
		
		try {
			period2 = indicator.getAdditionalTimePeriod();
		}
		catch (UnsupportedOperationException e) { }
		finally {
			if ( period2 != 0 )
				Logger.log().info("Обработка периода идет только для основного периода. Для дополнительного периода необходимо сделать еще один класс " + 
						MarketIndicatorDataSeries.class);
		}
		
		super.dates = DateUtils.filterDateListByYear(StockQuoteHistory.storage().getTradingDatesByPeriod(ticker, period), startYear, endYear);
		
		MarketIndicatorsHistory.storage().fillIndicatosData(ticker, period1, indicator.getMarketIndType(), indicator.getInterval());
		
		super.values = MarketIndicatorsHistory.storage().findIndicatorValues(ticker, period1, dates, indicator.getMarketIndType(), indicator.getInterval());
		
		// --
		tooltips = new ArrayList<String>();
		
		for(LocalDateTime date : dates) {
			double quoteValue = findValueByDate(date);
			
			tooltips.add(date.toLocalDate() + ": " + quoteValue);
		}
	}
	
	@Override
	public double findValueByDate(LocalDateTime date) {
		if ( !MarketIndicatorsHistory.storage().containsIndicatorInStorageOnDate(ticker, indicator.getTimePeriod(), 
				date, indicator.getMarketIndType(), indicator.getInterval()) )
			throw new AlphaVantageException("Не рассчитаны индикаторы " + indicator + "[" + indicator.getTimePeriod() + "] для тикера: " + ticker + " на дату: " + date);
		else {
		
			int i = 0;
		
			for(LocalDateTime curDate : dates)
				if ( DateUtils.compareDatesByDay(curDate, date)  ) {
					return values.get(i).doubleValue();
				}
				else
					i++;
			
			throw new AlphaVantageException("Не рассчитаны индикаторы " + indicator + "[" + indicator.getTimePeriod() + "] для тикера: " + ticker + " на дату: " + date);

		}
	}
	
	public boolean containtsValueByDate(LocalDateTime date) {
		if ( !MarketIndicatorsHistory.storage().containsIndicatorInStorageOnDate(ticker, indicator.getTimePeriod(), 
				date, indicator.getMarketIndType(), indicator.getInterval()) )
			return false;
		else {
			for(LocalDateTime curDate : dates)
				if ( DateUtils.compareDatesByDay(curDate, date)  )
					return true;
			
			return false;
		}
	}
	
	public MarketIndicatorInterface getIndicator() {
		return indicator;
	}

	public String getIndicatorTitle() {
		return indicatorTitle;
	}
}

package ru.backtesting.stockquotes.graphics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.patriques.output.AlphaVantageException;

import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.stockquotes.graphics.base.BaseFinancialTimeSeriesChartInformation;
import ru.backtesting.stockquotes.graphics.base.FinancialTimeSeriesChartInformation;
import ru.backtesting.utils.DateUtils;

public class MarketQuoteDataSeries extends BaseFinancialTimeSeriesChartInformation 
		implements FinancialTimeSeriesChartInformation {
	
	public MarketQuoteDataSeries(String ticker, int startYear, int endYear, TradingTimeFrame period, boolean dividens) {
		super();
		this.ticker = ticker;
		this.period = period;
		
		fillFromStorage(startYear, endYear, period, dividens);
	}
	
	public MarketQuoteDataSeries(String ticker, List<LocalDateTime> dates, List<Double> quotes, List<String> tooltips) {
		super();
		this.ticker = ticker;
		super.dates = dates;
		super.values = quotes;
		this.tooltips = tooltips;
	}

	private void fillFromStorage(int startYear, int endYear, TradingTimeFrame period, boolean dividens) {
		StockQuoteHistory.storage().fillQuotesData(ticker, period);
		
		super.dates = DateUtils.filterDateListByYear(StockQuoteHistory.storage().getTradingDatesByPeriod(ticker, period), startYear, endYear);
		
		super.values = StockQuoteHistory.storage().getQuoteValuesByDates(ticker, period, dates, dividens);		
		
		// --
		tooltips = new ArrayList<String>();
		
		for(LocalDateTime date : dates) {
			double quoteValue = findValueByDate(date);
			
			tooltips.add(date.toLocalDate() + ": " + quoteValue);
		}
	}

	@Override
	public double findValueByDate(LocalDateTime date) {
		if ( !StockQuoteHistory.storage().containsDataInStorageOnDate(ticker, getTradingPeriod(), date) )
			throw new AlphaVantageException("По активу [" + ticker + " на дату " + date + " не загружены котировки. "
					+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.co");
		else {
		
			int i = 0;
		
			for(LocalDateTime curDate : dates)
				if ( DateUtils.compareDatesByDay(curDate, date)  ) {
					return values.get(i).doubleValue();
				}
				else
					i++;
			
			throw new AlphaVantageException("По активу [" + ticker + " на дату " + date + " не загружены котировки. "
					+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.co");
		}
	}
}

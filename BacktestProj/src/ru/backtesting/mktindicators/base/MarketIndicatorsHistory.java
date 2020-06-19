package ru.backtesting.mktindicators.base;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.TechnicalIndicatorResponse;
import org.patriques.output.technicalindicators.data.BBANDSData;
import org.patriques.output.technicalindicators.data.IndicatorData;

import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;


public class MarketIndicatorsHistory {	
	private static MarketIndicatorsHistory instance;
	private static HashMap<String, HashMap<String, List<Object>>> indicatorsStorage;

	
	private MarketIndicatorsHistory() {
		indicatorsStorage = new HashMap<String, HashMap<String, List<Object>>>();
	}
	
	public static synchronized MarketIndicatorsHistory storage() {
		if (instance == null) {
			instance = new MarketIndicatorsHistory();
		}
		
		return instance;
	}
	
	private String generateKeyForIndTicker(String ticker, int timePeriod, MarketIndicatorType type, TradingTimeFrame interval) {
		return "[" + ticker + "][" + String.valueOf(timePeriod) + "][" + type + "][" + interval + "]"; 
	}
	
	public List<LocalDateTime> fillIndicatosData(String ticker, int timePeriod, MarketIndicatorType type, TradingTimeFrame interval) {
		org.patriques.input.technicalindicators.Interval invervalAV = Interval.DAILY;
		
		if ( interval.equals(TradingTimeFrame.Monthly) )
			invervalAV = Interval.MONTHLY;
		else if ( interval.equals(TradingTimeFrame.Weekly) )
			invervalAV = Interval.WEEKLY;
		
		String indStorageKey = generateKeyForIndTicker(ticker, timePeriod, type, interval);
		
		if ( !indicatorsStorage.containsKey(indStorageKey) || !indicatorsStorage.get(indStorageKey).containsKey(indStorageKey) )
			try {			 
				 
				List<Object> indList = null;
				TechnicalIndicatorResponse resp = null;
				 
				if (type.equals(MarketIndicatorType.SMA_IND)) {
					resp = StockConnector.sma(ticker, invervalAV, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.EMA_IND)) {
					resp = StockConnector.ema(ticker, invervalAV, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.WMA_IND)) {
					resp = StockConnector.wma(ticker, invervalAV, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.KaufmanAdaptiveMA_IND)) {
					resp = StockConnector.kama(ticker, invervalAV, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.RSI_OSC)) {
					resp = StockConnector.rsi(ticker, invervalAV, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.CHANDE_MOMENTUM_OSC)) {
					resp = StockConnector.cmo(ticker, invervalAV, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.BOLLINGER_BANDS)) {
					resp = StockConnector.bbands(ticker, invervalAV, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.HILBER_TRANSFORM_TRENDLINE)) {
					resp = StockConnector.ht_trendline(ticker, invervalAV, SeriesType.CLOSE);
				} else if (type.equals(MarketIndicatorType.OBV)) {
					resp = StockConnector.obv(ticker, invervalAV);
				}
				else
					throw new RuntimeException("Индикатор " + type + " не найден для тикера " + ticker + "и периода " + timePeriod);
				
				indList = resp.getData();

				HashMap<String, List<Object>> indicatorDates = new HashMap<String, List<Object>>();
				  
				Collections.reverse(indList);
				 
				indicatorDates.put(indStorageKey, indList);
				 			 
				indicatorsStorage.put(indStorageKey,indicatorDates);
				 
				Logger.log().info("Загрузка в хранилище индикатора " + type + ": " + resp.getMetaData());
			 } catch (AlphaVantageException e) {
			      throw e;
			 } 
		
		List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
		
		for (Object indData : indicatorsStorage.get(indStorageKey).get(indStorageKey)) {
			LocalDateTime date = null;
			
			if ( indData instanceof IndicatorData)
				date = ((IndicatorData) indData).getDateTime();
			else if (indData instanceof BBANDSData  ) 
				date = ((BBANDSData) indData).getDateTime();
				
			if ( !dates.contains(date) )
				dates.add(date);
		}
		 
		 return dates;
	}
	
	public List<LocalDateTime> fillHilbertTrendlineData(String ticker, TradingTimeFrame interval) {
		 return fillIndicatosData(ticker, 0, MarketIndicatorType.HILBER_TRANSFORM_TRENDLINE, interval) ;
	}
	
	public List<LocalDateTime> fillRSIData(String ticker, int timePeriod, TradingTimeFrame interval) {
		 return fillIndicatosData(ticker, timePeriod, MarketIndicatorType.RSI_OSC, interval) ;
	}
	
	public List<LocalDateTime> fillCMOData(String ticker, int timePeriod, TradingTimeFrame interval) {
		 return fillIndicatosData(ticker, timePeriod, MarketIndicatorType.CHANDE_MOMENTUM_OSC, interval) ;
	}
	
	public List<LocalDateTime> fillBBandsData(String ticker, int timePeriod, TradingTimeFrame interval) {
		 return fillIndicatosData(ticker, timePeriod, MarketIndicatorType.BOLLINGER_BANDS, interval) ;
	}
	
	public List<LocalDateTime> fillOBVData(String ticker, TradingTimeFrame interval) {
		 return fillIndicatosData(ticker, 0, MarketIndicatorType.OBV, interval) ;
	}
	
	@SuppressWarnings("unused")
	private List<Object> findIndicatorData(String ticker, int timePeriod, MarketIndicatorType type, TradingTimeFrame inverval) {
		String indStorageKey = generateKeyForIndTicker(ticker, timePeriod, type, inverval);

		HashMap<String, List<Object>> indicatorData = indicatorsStorage.get(indStorageKey);
		
		if ( indicatorData == null || !indicatorData.containsKey(indStorageKey) )
			throw new RuntimeException("Не рассчитаны индикаторы " + type + "[" + timePeriod + ", " + inverval + "] для тикера: " + ticker);
		else {
			List<Object> data = indicatorData.get(indStorageKey);
			
			if (data == null)
				throw new RuntimeException("Не рассчитаны индикаторы " + type + "[" + timePeriod + ", " + inverval + "] для тикера: " + ticker);
			else
				return data;
		}
	}
	
	public List<Double> findIndicatorValues(String ticker, int timePeriod, List<LocalDateTime> dates, MarketIndicatorType type, TradingTimeFrame inverval) {
		List<Double> values = new ArrayList<Double>();
		
		for (LocalDateTime date : dates) {
			double indValue = findIndicatorValue(ticker, timePeriod, date, type, inverval);

			values.add(indValue);
		}
		
		return values;
	}

	public boolean containsIndicatorInStorageOnDate(String ticker, int period, LocalDateTime date, MarketIndicatorType indicatorType, TradingTimeFrame inverval) {
		String indStorageKey = generateKeyForIndTicker(ticker, period, indicatorType, inverval);
		
		HashMap<String, List<Object>> indicatorData = indicatorsStorage.get(indStorageKey);
		
		if ( indicatorData == null )
			return false;
		else {
			List<Object> data = indicatorData.get(indStorageKey);
			
			if (data == null)
				return false;
			else {
				for (Object indData : data) {
					LocalDateTime dateObj = null;
					
					if ( indData instanceof IndicatorData)
						dateObj = ((IndicatorData) indData).getDateTime();
					else if (indData instanceof BBANDSData  ) 
						dateObj = ((BBANDSData) indData).getDateTime();
					
					if ( DateUtils.compareDatesByDay(dateObj, date) )
						return true;
				}
			}
					
			return false;
		}
	}
	
	public double findIndicatorValue(String ticker, int timePeriod, LocalDateTime date, MarketIndicatorType type, TradingTimeFrame inverval) {
		if ( containsIndicatorInStorageOnDate(ticker, timePeriod, date, type, inverval) ) {
			String indStorageKey = generateKeyForIndTicker(ticker, timePeriod, type, inverval);

			HashMap<String, List<Object>> indicatorData = indicatorsStorage.get(indStorageKey);
			
			List<Object> data = indicatorData.get(indStorageKey);
			
			for (Object indData : data) {
				LocalDateTime dateObj = null;
				
				if ( indData instanceof IndicatorData)
					dateObj = ((IndicatorData) indData).getDateTime();
				else if (indData instanceof BBANDSData  ) 
					dateObj = ((BBANDSData) indData).getDateTime();
				
				if ( DateUtils.compareDatesByDay(dateObj, date) )
					if ( indData instanceof BBANDSData )
						return ((BBANDSData) indData).getLowerBand();
					else return ((IndicatorData)indData).getData();
			}
		}
		
		LocalDateTime firstDate = getFirstDateFromIndicator(ticker, timePeriod, type, inverval);
		
		throw new AlphaVantageException("Не рассчитаны индикаторы " + type + "[" + timePeriod + ", " + inverval + "] для тикера: " + ticker + " на дату: " + date + 
				". Самая первая дата для данного индикатора: " + firstDate + ". Либо дата " + date + " раньше первой даты расчета индикатора " + firstDate + " или дата не попадает в период " +  inverval);
	}
	
	private LocalDateTime getFirstDateFromIndicator(String ticker, int timePeriod, MarketIndicatorType type, TradingTimeFrame inverval) {
		String indStorageKey = generateKeyForIndTicker(ticker, timePeriod, type, inverval);
		
		HashMap<String, List<Object>> indicatorData = indicatorsStorage.get(indStorageKey);

		if ( indicatorData == null )
			throw new AlphaVantageException("Не рассчитаны индикаторы " + type + "[" + timePeriod + ", " + inverval + "] для тикера: " + ticker + " ни на одну дату");
		
		else {
			List<Object> data = indicatorData.get(indStorageKey);
			
			if (data == null)
				throw new AlphaVantageException("Не рассчитаны индикаторы " + type + "[" + timePeriod + ", " + inverval + "] для тикера: " + ticker + " ни на одну дату");
			
			List<LocalDateTime> dates = new ArrayList<LocalDateTime>(); 
			for (Object indData : data) {								
				if ( indData instanceof IndicatorData)
					dates.add(((IndicatorData) indData).getDateTime());
				else if (indData instanceof BBANDSData  ) 
					dates.add(((BBANDSData) indData).getDateTime());
			}
			
			return dates.get(0);
		}
	}
	
	@Deprecated
	public List<Double> getIndicatorsDataForLastPeriod(String ticker, int indTimePeriod, LocalDateTime date, MarketIndicatorType type, TradingTimeFrame inverval, int lastPeriod) {
		String indStorageKey = generateKeyForIndTicker(ticker, indTimePeriod, type, inverval);

		HashMap<String, List<Object>> indicatorData = indicatorsStorage.get(indStorageKey);
		
		if ( indicatorData == null )
			throw new AlphaVantageException("Не рассчитаны индикаторы " + type + "[" + indTimePeriod + ", " + inverval + "] для тикера: " + ticker + " ни на одну дату");
		else {
			List<Object> data = indicatorData.get(indStorageKey);
			
			if (data == null)
				throw new AlphaVantageException("Не рассчитаны индикаторы " + type + "[" + indTimePeriod + ", " + inverval + "] для тикера: " + ticker + " ни на одну дату");
		
			int i = 0;
			for (Object indData : data) {				
				LocalDateTime dateObj = null;
				
				if ( indData instanceof IndicatorData)
					dateObj = ((IndicatorData) indData).getDateTime();
				else if (indData instanceof BBANDSData  ) 
					dateObj = ((BBANDSData) indData).getDateTime();
				
				if ( DateUtils.compareDatesByDay(dateObj, date) )
					break;
				
				i++;
			}
						
			if ( i == 0 )
				throw new AlphaVantageException("Не рассчитаны индикаторы " + type + "[" + indTimePeriod + ", " + inverval + "] для тикера: " + ticker + " на дату: " + date);
			
			List<Double> dataForSma = new ArrayList<Double>();
			
			int iterator = 0;
			
			if ( i >= lastPeriod)
				iterator = i - lastPeriod;
			
			for(;iterator <= i; iterator++) {
				Object indData = data.get(iterator);
				
				if ( indData instanceof BBANDSData )
					dataForSma.add(Double.valueOf(((BBANDSData) indData).getLowerBand()));
				else
					dataForSma.add(Double.valueOf(((IndicatorData)indData).getData()));
			}
			
			return dataForSma;
		}
	}
}

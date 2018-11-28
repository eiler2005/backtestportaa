package ru.backtesting.stockindicators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.patriques.TechnicalIndicators;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.SMA;
import org.patriques.output.technicalindicators.TechnicalIndicatorResponse;
import org.patriques.output.technicalindicators.data.IndicatorData;

import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;


public class StockIndicatorsHistory {
	public final static String SMA_IND_ID = "SMA";
	public final static String RSI_OSC_ID = "RSI";
	public final static String CHANDE_MOMENTUM_OSC_ID = "CMO";

	
	private static StockIndicatorsHistory instance;
	private static HashMap<String, HashMap<Integer, List<IndicatorData>>> indicatorsStorage;

	
	private StockIndicatorsHistory() {
		indicatorsStorage = new HashMap<String, HashMap<Integer, List<IndicatorData>>>();
	}
	
	public static synchronized StockIndicatorsHistory storage() {
		if (instance == null) {
			instance = new StockIndicatorsHistory();
		}
		
		return instance;
	}
	
	private String generateKeyForIndTicker(String ticker, int timePeriod, String indicator) {
		return ticker + String.valueOf(timePeriod) + indicator; 
	}
	
	private List<LocalDateTime> fillIndicatosData(String ticker, int timePeriod, String indicator) {
		String smaStorageKey = generateKeyForIndTicker(ticker, timePeriod, indicator);
		
		if ( !indicatorsStorage.containsKey(smaStorageKey) || !indicatorsStorage.get(smaStorageKey).containsKey(new Integer(timePeriod)) )
			try {			 
				 
				List<IndicatorData> indList = null;
				TechnicalIndicatorResponse resp = null;
				 
				if (indicator.equals(SMA_IND_ID)) {
					resp = StockConnector.sma(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);;
				} else if (indicator.equals(RSI_OSC_ID)) {
					resp = StockConnector.rsi(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (indicator.equals(CHANDE_MOMENTUM_OSC_ID)) {
					resp = StockConnector.cmo(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				}
				else
					throw new RuntimeException("Индикатор " + indicator + " не найден для тикера " + ticker + "и периода " + timePeriod);
				
				indList = (List<IndicatorData>) resp.getData();

				HashMap<Integer, List<IndicatorData>> indicatorDates = new HashMap<Integer, List<IndicatorData>>();
				  
				Collections.reverse(indList);
				 
				indicatorDates.put(new Integer(timePeriod), indList);
				 			 
				indicatorsStorage.put(smaStorageKey,indicatorDates);
				 
				Logger.log().info("Загрузка в хранилище индикатора " + indicator + ": " + resp.getMetaData());
			 } catch (AlphaVantageException e) {
			      throw e;
			 } 
		
		List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
		
		for (IndicatorData smaData : indicatorsStorage.get(smaStorageKey).get(new Integer(timePeriod))) 				  
			if ( !dates.contains(smaData.getDateTime()))
				dates.add(smaData.getDateTime());
		 
		 return dates;
	}
	
	public List<LocalDateTime> fillSMAData(String ticker, int timePeriod) {
		 return fillIndicatosData(ticker, timePeriod, SMA_IND_ID) ;
	}
	
	public List<LocalDateTime> fillRSIData(String ticker, int timePeriod) {
		 return fillIndicatosData(ticker, timePeriod, RSI_OSC_ID) ;
	}
	
	public List<LocalDateTime> fillCMOData(String ticker, int timePeriod) {
		 return fillIndicatosData(ticker, timePeriod, CHANDE_MOMENTUM_OSC_ID) ;
	}
	
	public List<IndicatorData> findIndicatorData(String ticker, int timePeriod, String indID) {
		String smaStorageKey = generateKeyForIndTicker(ticker, timePeriod, indID);

		HashMap<Integer, List<IndicatorData>> indicatorData = indicatorsStorage.get(smaStorageKey);
		
		if ( indicatorData == null || !indicatorData.containsKey(new Integer(timePeriod)) )
			throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker);
		else {
			List<IndicatorData> data = indicatorData.get(timePeriod);
			
			if (data == null)
				throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker);
			else
				return data;
		}
	}
	
	public double findIndicatorValue(String ticker, int timePeriod, LocalDateTime date, String indID) {
		String indStorageKey = generateKeyForIndTicker(ticker, timePeriod, indID);

		
		HashMap<Integer, List<IndicatorData>> indicatorData = indicatorsStorage.get(indStorageKey);
		
		if ( indicatorData == null )
			throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker + " на дату: " + date);
		else {
			List<IndicatorData> data = indicatorData.get(new Integer(timePeriod));
			
			if (data == null)
				throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker + " на дату: " + date);
			else
				for (IndicatorData ind : data)
					if ( DateUtils.compareDatesByDay(ind.getDateTime(), date) )
						return ind.getData();
					
			throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker + " на дату: " + date);
		}
	}
}

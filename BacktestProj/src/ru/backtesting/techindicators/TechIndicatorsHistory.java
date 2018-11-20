package ru.backtesting.techindicators;

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
import org.patriques.output.technicalindicators.SMA;
import org.patriques.output.technicalindicators.data.IndicatorData;

import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.utils.DateUtils;


public class TechIndicatorsHistory {
	private static TechIndicatorsHistory instance;
	private static HashMap<String, HashMap<SMAType, List<IndicatorData>>> smaTechInd;
	
	private TechIndicatorsHistory() {
		smaTechInd = new HashMap<String, HashMap<SMAType, List<IndicatorData>>>();
	}
	
	public static synchronized TechIndicatorsHistory storage() {
		if (instance == null) {
			instance = new TechIndicatorsHistory();
		}
		
		return instance;
	}
	
	public List<LocalDateTime> fillSMAData(String ticker, SMAType smaType) {
		 try {
			 TechnicalIndicators technicalIndicators = new TechnicalIndicators(StockConnector.conn());
			 
			 int timePeriod = 0;
			 if ( smaType.equals(SMAType.TwoHundredDays))
				 timePeriod = 200;
			 if ( smaType.equals(SMAType.FiftyDays))
				 timePeriod = 50;
				 
			 SMA sma = technicalIndicators.sma(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);
			  
			 HashMap<SMAType, List<IndicatorData>> indicatorDates = new HashMap<SMAType, List<IndicatorData>>();
	
			 List<IndicatorData> smaList = sma.getData();
			  
			 Collections.reverse(smaList);
			 
			 indicatorDates.put(smaType, smaList);
			 
			 smaTechInd.put(ticker,indicatorDates);
			 
			 System.out.println("Meta data form sma:" + sma.getMetaData());
			  
			 List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
			 			  
			 for (IndicatorData smaData : smaTechInd.get(ticker).get(smaType)) 				  
				 if ( !dates.contains(smaData.getDateTime()))
					 dates.add(smaData.getDateTime());
			 
			 return dates;
		 } catch (AlphaVantageException e) {
		      throw e;
		 }
	}
	
	public List<IndicatorData> findIndicatorData(String ticker, SMAType smaType) {
		HashMap<SMAType, List<IndicatorData>> indicatorData = smaTechInd.get(ticker);
		
		if ( indicatorData == null )
			throw new RuntimeException("Не рассчитаны индикаторы " + smaType + " для тикера: " + ticker);
		else {
			List<IndicatorData> data = indicatorData.get(smaType);
			
			if (data == null)
				throw new RuntimeException("Не рассчитаны индикаторы " + smaType + " для тикера: " + ticker);
			else
				return data;
		}
	}
	
	public double findIndicatorData(String ticker, SMAType smaType, LocalDateTime date) {
		HashMap<SMAType, List<IndicatorData>> indicatorData = smaTechInd.get(ticker);
		
		if ( indicatorData == null )
			throw new RuntimeException("Не рассчитаны индикаторы " + smaType + " для тикера: " + ticker);
		else {
			List<IndicatorData> data = indicatorData.get(smaType);
			
			if (data == null)
				throw new RuntimeException("Не рассчитаны индикаторы " + smaType + " для тикера: " + ticker);
			else
				for (IndicatorData ind : data)
					if ( DateUtils.compareDatesByDay(ind.getDateTime(), date) )
						return ind.getData();
					
					throw new RuntimeException("Индикаторы " + smaType + " для тикера: " + ticker + " рассчитаны, но не на дату " + date);
		}
	}
}

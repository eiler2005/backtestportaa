package ru.backtesting.port.base.aa.momentum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.RuntimeErrorException;

import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class DualMomUtils {

	public static Map<String, Double> getEquivalentAssetAllocPercent(Collection<String> tickers) {
		int count = tickers.size();
		
		Map<String, Double> allocPersent = new HashMap<String, Double>();
		
		if ( count == 1 )
			allocPersent.put(tickers.iterator().next(), new Double(100));
		else {
			double proportion = /*(double)*/ 100 / tickers.size();
			
			double sum = 0;
			
			String[] tickArray = new String[tickers.size()];
			tickers.toArray(tickArray); 
			
			for ( int i = 0; i < tickers.size() - 1; i++ ) {
				allocPersent.put(tickArray[i], new Double(proportion));
				sum += proportion;
			}
			
			allocPersent.put(tickArray[tickers.size() - 1], new Double(100 - sum));
		}
		
		return allocPersent;
	}

	public static Map<String, MomAssetAllocPerfInf> getAssetsToHoldList(SortedSet<Map.Entry<String, MomAssetAllocPerfInf>> assetsPerfSet, int assetstoHoldCount) {
		Map<String, MomAssetAllocPerfInf> tickers = new HashMap<String, MomAssetAllocPerfInf>();
		
		int count = 0;
		
		if ( assetstoHoldCount == 0 )
			return tickers;
			
		for (Map.Entry<String, MomAssetAllocPerfInf> assetPerf : assetsPerfSet) {
			count++;
			
			tickers.put(assetPerf.getKey(), assetPerf.getValue());
			
			if ( count == assetstoHoldCount )
				return tickers;
		}
				
		return tickers;
	}

	@Deprecated
	public static SortedSet<Map.Entry<String, Double>> calcSortedPerformanceScoreInPercentsToDays(LocalDateTime date, List<String> tickers, int daysPeriod, boolean divinends) {
		SortedSet<Map.Entry<String, Double>> tickersPerfSet = new TreeSet<Map.Entry<String, Double>>(
	            Collections.reverseOrder(new Comparator<Map.Entry<String, Double>>() {
	                @Override
	                public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
	                	 if (e1.getValue() < e2.getValue() )
	                         return -1;
	                     if (e1.getValue() > e2.getValue())
	                         return 1;
	                     return 0;
	                }
	            }));
		
	    SortedMap<String, Double> tickersPerfMap = new TreeMap<String, Double>();
	    
	    for(String ticker : tickers) {
	    	double percentageGrowth = DualMomUtils.calcPerformanceScoreInPercentsToDays(date, ticker, daysPeriod);
	    	
		    tickersPerfMap.put(ticker, new Double(percentageGrowth));
	    }
	
	    tickersPerfSet.addAll(tickersPerfMap.entrySet());
	    	    
		return tickersPerfSet;
	}

	public static SortedSet<Map.Entry<String, MomAssetAllocPerfInf>> calcSortedPerformanceScoreInPercentToMonths(LocalDateTime date, List<String> tickers, int monthPeriod) {
		SortedSet<Map.Entry<String, MomAssetAllocPerfInf>> tickersPerfSet = new TreeSet<Map.Entry<String, MomAssetAllocPerfInf>>(
	            Collections.reverseOrder(new Comparator<Map.Entry<String, MomAssetAllocPerfInf>>() {
	                @Override
	                public int compare(Map.Entry<String, MomAssetAllocPerfInf> e1, Map.Entry<String, MomAssetAllocPerfInf> e2) {
	                	 if (e1.getValue().getPercGrowth() < e2.getValue().getPercGrowth() )
	                         return -1;
	                     if (e1.getValue().getPercGrowth() > e2.getValue().getPercGrowth())
	                         return 1;
	                     return 0;
	                }
	            }));
		
	    SortedMap<String, MomAssetAllocPerfInf> tickersPerfMap = new TreeMap<String, MomAssetAllocPerfInf>();
	    
	    for(String ticker : tickers) {
	    	double percentageGrowth = DualMomUtils.calcPerformanceScoreInPercentsToMonths(date, ticker, monthPeriod);
	    	
	    	LocalDateTime startDate = StockQuoteHistory.storage().getFirstTradinDayAtMonthAgo(date, ticker, monthPeriod);
			
			double startQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, startDate).getClose();
	    	
			double endQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, date).getClose();
			
	    	MomAssetAllocPerfInf allocInf = new MomAssetAllocPerfInf(ticker, startDate, date, 
	    			startQuoteValue, endQuoteValue, percentageGrowth);
	    	
		    tickersPerfMap.put(ticker, allocInf);
	    }
	
	    tickersPerfSet.addAll(tickersPerfMap.entrySet());
	    	    
		return tickersPerfSet;
	}

	@Deprecated
	public static double calcPerformanceScoreInPercentsToDays(LocalDateTime date, String ticker, int daysPeriod ) {
		LocalDateTime startDate = StockQuoteHistory.storage().getPreviousTradingDay(date, ticker, daysPeriod);
		
		double startQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, startDate).getClose();
		
		Logger.log().info("По активу [" + ticker + "] посчитали дату " + startDate + " на " + daysPeriod + 
				" торговых дней ранее, получены следующие котировки: " + startQuoteValue);
		
		double endQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, date).getClose();
	
		Logger.log().info("По активу [" + ticker + "] на дату " + date + " получены котировки: " + endQuoteValue);
	
		double percentageGrowth = (double) (endQuoteValue - startQuoteValue)/startQuoteValue * 100;
				
		return percentageGrowth;
	}

	public static double calcPerformanceScoreInPercentsToMonths(LocalDateTime date, String ticker, int monthsPeriod ) {
		LocalDateTime startDate = StockQuoteHistory.storage().getFirstTradinDayAtMonthAgo(date, ticker, monthsPeriod);
		
		double startQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, startDate).getClose();
		
		Logger.log().info("По активу [" + ticker + "] посчитали дату " + startDate + " на " + monthsPeriod + 
				" торговых месяца ранее, получены следующие котировки: " + startQuoteValue);		
		
		double endQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, date).getClose();
	
		Logger.log().info("По активу [" + ticker + "] на дату " + date + " получены котировки: " + endQuoteValue);
	
		double percentageGrowth = (double) (endQuoteValue - startQuoteValue)/startQuoteValue * 100;
				
		// if ( percentageGrowth == 0 )
		//	throw new RuntimeErrorException(new Error("Для актива " + ticker + " рост за период равен 0. Это некорректно. Проверьте программу на наличие ошибок!"));
			
		return percentageGrowth;
	}

	public static List<AssetAllocPerfInf> getAssetAllocInfListForParams(List<String> tickers, LocalDateTime date, 
			int assetsHoldCount, int monthPerfPeriod, boolean logging) {
		SortedSet<Map.Entry<String, MomAssetAllocPerfInf>> assetsPerfSet = calcSortedPerformanceScoreInPercentToMonths(date, tickers, monthPerfPeriod);
		
		if ( logging )
			Logger.log().info("По активам [" + tickers + "] на дату " + date + " процент роста за период " + monthPerfPeriod + 
				" месяцев составил: \n" + assetsPerfSet);
				
		Map<String, MomAssetAllocPerfInf> assetAllocPerfMap = getAssetsToHoldList(assetsPerfSet, assetsHoldCount);
		    						
		Map<String, Double> assetAllocEqualMap = getEquivalentAssetAllocPercent(assetAllocPerfMap.keySet());
		
		if ( logging )
			Logger.log().info("Собираем портфель со следующими активами и аллокациями: " + assetAllocEqualMap.toString());
	
		List<AssetAllocPerfInf> fullAllocList = new ArrayList<>();
		
		for (Map.Entry<String, MomAssetAllocPerfInf> assetPerf : assetsPerfSet) {
			String ticker = assetPerf.getKey();
			MomAssetAllocPerfInf assetAllocInf = assetPerf.getValue();
			
			Set<String> assetInPortTickers = assetAllocEqualMap.keySet();
			
			if ( assetInPortTickers.contains(ticker) ) {
				assetAllocInf.holdAssetInPort();
				
				double allocPercent = assetAllocEqualMap.get(ticker).doubleValue();
				
				assetAllocInf.setAllocPercent(allocPercent);
			}
			else
				assetAllocInf.sellAsset();
	
			fullAllocList.add(assetAllocInf);
		}
					
		return fullAllocList;
	}

	public static MomAssetAllocPerfInf getTickerAssetAllocInf(String ticker, LocalDateTime date, double allocPercent, int monthPerfPeriod) {
		SortedSet<Map.Entry<String, MomAssetAllocPerfInf>> assetsPerfMap = calcSortedPerformanceScoreInPercentToMonths(
				date, Arrays.asList(new String[] { ticker }), monthPerfPeriod);
	
		MomAssetAllocPerfInf allocInf = assetsPerfMap.first().getValue();
		
		allocInf.setAllocPercent(allocPercent);
		
		return allocInf;
	}

	public static void sellAssetsInPort(List<AssetAllocPerfInf> assets) {
		for(AssetAllocPerfInf asset : assets)
			asset.sellAsset();
	}

}

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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Precision;

import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetAllocationUtils;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.GlobalProperties;
import ru.backtesting.utils.Logger;

public class DualMomUtils {
	public static final int NOT_AVAILABLE_QUOTE_PERF = -100;

	public static List<AssetAllocPerfInf> calcRiskOnAssets(LocalDateTime date, List<? extends AssetAllocation> assetsAllocEtalon, List<TickerInf> tickers, 
			TickerInf outOfMarketPos, int assetsHoldCount, int monthPerfPeriod) {
		List<AssetAllocPerfInf> detailedAssAllocInfList = new ArrayList<AssetAllocPerfInf>();
		
		double portPortion = 100;			
		
		if ( CollectionUtils.isNotEmpty(assetsAllocEtalon) ) {
			List<AssetAllocPerfInf> assetsWithFixedAA = DualMomUtils.getAssetAllocInfListForFixedAA(
					assetsAllocEtalon, date, monthPerfPeriod, true);
			
			detailedAssAllocInfList.addAll(assetsWithFixedAA);
			
			portPortion = AssetAllocationUtils.calcSummarizedAllocPercent(assetsWithFixedAA);
		}
		
		if ( CollectionUtils.isNotEmpty(tickers) ) {
			List<AssetAllocPerfInf> assets = null;
			
			if ( CollectionUtils.isNotEmpty(assetsAllocEtalon) )
				portPortion = 100 - portPortion;
			
			if ( assetsHoldCount == -1 || assetsHoldCount >= tickers.size() )
				assets = DualMomUtils.getAssetAllocInfListForPerfomance(
						tickers, date, tickers.size(), monthPerfPeriod, portPortion, true);
			else
				assets = DualMomUtils.getAssetAllocInfListForPerfomance(
						tickers, date, assetsHoldCount, monthPerfPeriod, portPortion, true);
			
			detailedAssAllocInfList.addAll(assets);
		}	
					
		MomAssetAllocPerfInf outOfMarketTickerAssetAllocInf = DualMomUtils.getTickerAssetAllocInf(outOfMarketPos, date, 0, monthPerfPeriod);
		outOfMarketTickerAssetAllocInf.sellAsset();
		
		detailedAssAllocInfList.add(outOfMarketTickerAssetAllocInf);
		
		return detailedAssAllocInfList;

	}
	
	public static List<AssetAllocPerfInf> calcRiskOffAssets(LocalDateTime date, List<? extends AssetAllocation> assetsAllocEtalon, List<TickerInf> tickers, 
			TickerInf outOfMarketPos, int monthPerfPeriod) {
		double portPortion = 100;			
		
		List<AssetAllocPerfInf> detailedAssAllocInfList = new ArrayList<AssetAllocPerfInf>();
		
		if ( CollectionUtils.isNotEmpty(assetsAllocEtalon) ) {
			List<AssetAllocPerfInf> assetsWithFixedAA = DualMomUtils.getAssetAllocInfListForFixedAA(
					assetsAllocEtalon, date, monthPerfPeriod, true);
			
			detailedAssAllocInfList.addAll(assetsWithFixedAA);
			
			portPortion = AssetAllocationUtils.calcSummarizedAllocPercentWithCore(assetsAllocEtalon);
			
			portPortion = 100 - portPortion;
		}
		
		// данные для таблицы excel
		MomAssetAllocPerfInf outOfMarketTickerAssetAllocInf = DualMomUtils.getTickerAssetAllocInf(outOfMarketPos, date, portPortion, monthPerfPeriod);
		
		outOfMarketTickerAssetAllocInf.holdAssetInPort();
					
		// for excel
		if (  CollectionUtils.isNotEmpty(tickers) )
			detailedAssAllocInfList.addAll(DualMomUtils.getAssetAllocInfListForPerfomance(tickers, date, tickers.size(), 
				monthPerfPeriod, 100, false));
		
		if ( CollectionUtils.isNotEmpty(assetsAllocEtalon) )
			AssetAllocationUtils.sellAssetsInPortWithCore(detailedAssAllocInfList, assetsAllocEtalon);
		else
			AssetAllocationUtils.sellAssetsInPort(detailedAssAllocInfList);
		
		detailedAssAllocInfList.add(outOfMarketTickerAssetAllocInf);
		
		return detailedAssAllocInfList;
	}
	
	public static Map<TickerInf, Double> getEquivalentAssetAllocPercent(Collection<TickerInf> tickers, double portPortion) {
		int count = tickers.size();
		
		Map<TickerInf, Double> allocPersent = new HashMap<TickerInf, Double>();
		
		if ( count == 1 )
			allocPersent.put(tickers.iterator().next(), new Double(portPortion));
		else {
			double proportion = Precision.round( (double )portPortion / tickers.size(), 2);
			
			double sum = 0;
			
			TickerInf[] tickArray = new TickerInf[tickers.size()];
			tickers.toArray(tickArray);
			
			for ( int i = 0; i < tickers.size() - 1; i++ ) {
				allocPersent.put(tickArray[i], new Double(proportion));
				sum += proportion;
			}
						
			allocPersent.put(tickArray[tickers.size() - 1], new Double(portPortion - sum));
		}
		
		return allocPersent;
	}

	public static Map<TickerInf, MomAssetAllocPerfInf> getAssetsToHoldList(
			SortedSet<Map.Entry<TickerInf, MomAssetAllocPerfInf>> assetsPerfSet, int assetstoHoldCount) {
		Map<TickerInf, MomAssetAllocPerfInf> tickers = new HashMap<TickerInf, MomAssetAllocPerfInf>();

		int count = 0;

		if (assetstoHoldCount == 0)
			return tickers;

		for (Map.Entry<TickerInf, MomAssetAllocPerfInf> assetPerf : assetsPerfSet) {
			boolean isHold = assetPerf.getValue().isHoldInPort();

			if (isHold) {
				count++;

				tickers.put(assetPerf.getKey(), assetPerf.getValue());

				if (count == assetstoHoldCount)
					return tickers;
			}
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

	public static SortedSet<Map.Entry<TickerInf, MomAssetAllocPerfInf>> calcSortedPerformanceScoreInPercentToMonths(LocalDateTime date, List<TickerInf> tickersInf, int monthPeriod) {
		SortedSet<Map.Entry<TickerInf, MomAssetAllocPerfInf>> tickersPerfSet = new TreeSet<Map.Entry<TickerInf, MomAssetAllocPerfInf>>(
	            Collections.reverseOrder(new Comparator<Map.Entry<TickerInf, MomAssetAllocPerfInf>>() {
	                @Override
	                public int compare(Map.Entry<TickerInf, MomAssetAllocPerfInf> e1, Map.Entry<TickerInf, MomAssetAllocPerfInf> e2) {
	                	 if (e1.getValue().getPercGrowth() < e2.getValue().getPercGrowth() )
	                         return -1;
	                     if (e1.getValue().getPercGrowth() > e2.getValue().getPercGrowth())
	                         return 1;
	                     return 0;
	                }
	            }));
		
	    SortedMap<TickerInf, MomAssetAllocPerfInf> tickersPerfMap = new TreeMap<TickerInf, MomAssetAllocPerfInf>();
	    
	    for(TickerInf tickerInf : tickersInf) {
	    	String ticker = tickerInf.getTicker();
	    	
			boolean containsInStorage = StockQuoteHistory.storage().containsTradinDayAtMonthAgo(date, ticker, monthPeriod);
			
			if ( GlobalProperties.instance().isSoftQuotesInPort() && !containsInStorage) {
				MomAssetAllocPerfInf allocInf = new MomAssetAllocPerfInf(tickerInf, null, date, 
		    			0, 0, DualMomUtils.NOT_AVAILABLE_QUOTE_PERF);
		    	
				allocInf.sellAsset();
								
			    tickersPerfMap.put(tickerInf, allocInf);
			} else {
		    	double percentageGrowth = DualMomUtils.calcPerformanceScoreInPercentsToMonths(date, ticker, monthPeriod);
		    	
		    	LocalDateTime startDate = StockQuoteHistory.storage().getFirstTradinDayAtMonthAgo(date, ticker, monthPeriod);
				
				double startQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, startDate).getClose();
		    	
				double endQuoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, date).getClose();
				
		    	MomAssetAllocPerfInf allocInf = new MomAssetAllocPerfInf(tickerInf, startDate, date, 
		    			startQuoteValue, endQuoteValue, percentageGrowth);
		    	
				allocInf.holdAssetInPort();
				
			    tickersPerfMap.put(tickerInf, allocInf);
			}
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
		boolean containsInStorage = StockQuoteHistory.storage().containsTradinDayAtMonthAgo(date, ticker, monthsPeriod);
		
		if ( GlobalProperties.instance().isSoftQuotesInPort() && !containsInStorage) {
			return NOT_AVAILABLE_QUOTE_PERF;
		} else {
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
	}
	
	public static List<AssetAllocPerfInf> getAssetAllocInfListForFixedAA(List<? extends AssetAllocation> assetsAllocEtalon, LocalDateTime date, 
			int monthPerfPeriod, boolean logging) {
		List<TickerInf> tickersInf = Ticker.getTickersInfForAssetAlloc(assetsAllocEtalon);
		
		SortedSet<Map.Entry<TickerInf, MomAssetAllocPerfInf>> assetsPerfSet = calcSortedPerformanceScoreInPercentToMonths(date, tickersInf, monthPerfPeriod);
		
		if ( logging )
			Logger.log().info("По активам [" + tickersInf + "] на дату " + date + " процент роста за период " + monthPerfPeriod + 
				" месяцев составил: \n" + assetsPerfSet);
		
		List<AssetAllocPerfInf> fullAllocList = new ArrayList<>();
		
		for (Map.Entry<TickerInf, MomAssetAllocPerfInf> assetPerf : assetsPerfSet) {
			MomAssetAllocPerfInf assetAllocInf = assetPerf.getValue();
			
			TickerInf tickerInf = assetAllocInf.getTickerInf();
			
			AssetAllocation etalonAA = Ticker.getAssetAllocForTickerInf(assetsAllocEtalon, tickerInf);
			
			assetAllocInf.setAllocPercent(etalonAA.getAllocationPercent());
						
			assetAllocInf.holdAssetInPort();
			
			fullAllocList.add(assetAllocInf);
		}
		
		if ( logging )
			Logger.log().info("Собираем портфель со следующими активами и аллокациями: " + fullAllocList.toString());
					
		return fullAllocList;
	}
	
	public static List<AssetAllocPerfInf> getAssetAllocInfListForPerfomance(List<TickerInf> tickersInf, LocalDateTime date, 
			int assetsHoldCount, int monthPerfPeriod, double portPortion, boolean logging) {
		SortedSet<Map.Entry<TickerInf, MomAssetAllocPerfInf>> assetsPerfSet = calcSortedPerformanceScoreInPercentToMonths(date, tickersInf, monthPerfPeriod);
		
		if ( logging )
			Logger.log().info("По активам [" + tickersInf + "] на дату " + date + " процент роста за период " + monthPerfPeriod + 
				" месяцев составил: \n" + assetsPerfSet);
				
		Map<TickerInf, MomAssetAllocPerfInf> assetAllocPerfMap = getAssetsToHoldList(assetsPerfSet, assetsHoldCount);
		    						
		Map<TickerInf, Double> assetAllocEqualMap = getEquivalentAssetAllocPercent(assetAllocPerfMap.keySet(), portPortion);
		
		if ( logging )
			Logger.log().info("Собираем портфель со следующими активами и аллокациями: " + assetAllocEqualMap.toString());
	
		List<AssetAllocPerfInf> fullAllocList = new ArrayList<>();
		
		for (Map.Entry<TickerInf, MomAssetAllocPerfInf> assetPerf : assetsPerfSet) {
			TickerInf ticker = assetPerf.getKey();
			MomAssetAllocPerfInf assetAllocInf = assetPerf.getValue();
			
			Set<TickerInf> assetInPortTickers = assetAllocEqualMap.keySet();
			
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

	public static MomAssetAllocPerfInf getTickerAssetAllocInf(TickerInf tickerInf, LocalDateTime date, double allocPercent, int monthPerfPeriod) {
		SortedSet<Map.Entry<TickerInf, MomAssetAllocPerfInf>> assetsPerfMap = calcSortedPerformanceScoreInPercentToMonths(
				date, Arrays.asList(new TickerInf[] { tickerInf }), monthPerfPeriod);
	
		MomAssetAllocPerfInf allocInf = assetsPerfMap.first().getValue();
		
		allocInf.setAllocPercent(allocPercent);
				
		return allocInf;
	}
}

package ru.backtesting.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.jupiter.api.Test;

import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.momentum.DualMomUtils;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class DualMomentumTest {
	public static void main(String[] args) throws IOException {
		// testAllocTickers();
		
		// daysAssetAllocPeriod();
		
		ioskaPortTest();
	}
	
	@Test
	public static void ioskaPortTest() {
		List<String> tickers = Arrays.asList("smh", "QQQ", "xbi", "ita", "ihi");
			
		for(String ticker : tickers)
			StockQuoteHistory.loadStockData(ticker, TradingTimeFrame.Daily, false);

		
		LocalDateTime date = LocalDateTime.parse("2007-01-03T00:00:00");
		
		int monthPerfPeriod = 1;
		
		List<AssetAllocPerfInf> assetsPerfSet = DualMomUtils.getAssetAllocInfListForParams(
				tickers, date, tickers.size(), monthPerfPeriod, true);

		Logger.log().info("assetsPerfSet: " + assetsPerfSet.toString());
		
		for (AssetAllocPerfInf assetPerf : assetsPerfSet) {
			String tickerCur = assetPerf.getTicker();
			
			MomAssetAllocPerfInf assetPerfNew = null;
			
			if ( assetPerf instanceof MomAssetAllocPerfInf )
				assetPerfNew = (MomAssetAllocPerfInf) assetPerf;
				
			double perf = assetPerfNew.getPercGrowth();
			double startQuote = assetPerfNew.getStockQuoteStart();
			double endQuote = assetPerfNew.getStockQuoteEnd();

			LocalDateTime starDate = assetPerfNew.getStartDate();
			LocalDateTime endEndDate = assetPerfNew.getEndDate();
			
			if ( tickerCur.equalsIgnoreCase("xbi") ) {
				assertEquals(-9.089101034208433, perf);
				
				assertEquals(50.28, startQuote);

				assertEquals(45.71, endQuote);

				assertEquals(LocalDateTime.parse("2006-12-01T00:00:00"), starDate);

				assertEquals(LocalDateTime.parse("2007-01-03T00:00:00"), endEndDate);
			}
			
			if ( tickerCur.equalsIgnoreCase("tlt") ) {
				assertEquals(LocalDateTime.parse("2007-01-03T00:00:00"), endEndDate);

				assertEquals(LocalDateTime.parse("2006-12-01T00:00:00"), starDate);
			}
		}
	}
	
	@Test
	public static void daysAssetAllocPeriod() {
		boolean dividens = true;
		String ticker = "spy";
		String absMomAsset = "TIP";
		
		int perfPeriodDays = 40;
		
		int perfPeriodMonth = 2;
		
		TradingTimeFrame period = TradingTimeFrame.Monthly;
		
		StockQuoteHistory.loadStockData(ticker, period, dividens);
		StockQuoteHistory.loadStockData(absMomAsset, period, dividens);

		// посчитаем перфоманс за период
		
		LocalDateTime curDate = LocalDateTime.parse("2020-05-27T00:00:00");
		
		double percGrowth1 = DualMomUtils.calcPerformanceScoreInPercentsToDays(curDate, ticker, perfPeriodDays);
		
		Logger.log().info("По активу [" + ticker + "] на дату " + curDate + " процент роста за период " + perfPeriodDays + " дней составил: " + 
				Logger.log().doubleAsString(percGrowth1)  + " %");

		assertEquals(Logger.log().doubleAsString(percGrowth1), "16,01");
		
		double percGrowth2 = DualMomUtils.calcPerformanceScoreInPercentsToDays(curDate, absMomAsset, perfPeriodDays);
		
		Logger.log().info("По активу [" + absMomAsset + "] на дату " + curDate + " процент роста за период " + perfPeriodDays + " дней составил: " + 
				Logger.log().doubleAsString(percGrowth2) + " %");

		assertEquals(Logger.log().doubleAsString(percGrowth2), "2,97");
		
		List<String> tickers = Arrays.asList("LQD", "HYG", "SCZ", "VNQ", "QQQ", "DIA", "EFA", "EEM", "IHI", "XLE");
		
		for (String curTicker : tickers)
			StockQuoteHistory.loadStockData(curTicker, period, dividens);
		
		SortedSet<Map.Entry<String, MomAssetAllocPerfInf>> assetsPerfSet = DualMomUtils.calcSortedPerformanceScoreInPercentToMonths(curDate, tickers, perfPeriodMonth);
		
		Logger.log().info("По активам [" + tickers + "] на дату " + curDate + " процент роста за период " + perfPeriodMonth + 
				" месяца составил: \n" + assetsPerfSet);
				
		for (Map.Entry<String, MomAssetAllocPerfInf> assetPerf : assetsPerfSet) {
			String tickerCur = assetPerf.getKey();
			
			double perf = assetPerf.getValue().getPercGrowth();
			double startQuote = assetPerf.getValue().getStockQuoteStart();
			double endQuote = assetPerf.getValue().getStockQuoteEnd();

			LocalDateTime starDate = assetPerf.getValue().getStartDate();
			LocalDateTime endEndDate = assetPerf.getValue().getEndDate();
			
			if ( tickerCur.equalsIgnoreCase("xle") ) {
				assertEquals(45.03982621288921, perf);
				
				assertEquals(27.62, startQuote);

				assertEquals(40.06, endQuote);

				assertEquals(LocalDateTime.parse("2020-04-01T00:00"), starDate);

				assertEquals(LocalDateTime.parse("2020-05-27T00:00:00"), endEndDate);
			}
			
			if ( tickerCur.equalsIgnoreCase("qqq") ) {
				assertEquals(26.317810323076074, perf);
				
				assertEquals(182.31, startQuote);

				assertEquals(230.29, endQuote);

				assertEquals(LocalDateTime.parse("2020-04-01T00:00"), starDate);

				assertEquals(LocalDateTime.parse("2020-05-27T00:00:00"), endEndDate);
			}
			
			if ( tickerCur.equalsIgnoreCase("eem") ) {
				assertEquals(14.324991687089753, perf);
				
				assertEquals(32.4796, startQuote);

				assertEquals(37.1323, endQuote);

				assertEquals(LocalDateTime.parse("2020-04-01T00:00"), starDate);

				assertEquals(LocalDateTime.parse("2020-05-27T00:00:00"), endEndDate);
			}
		}
		
		Logger.log().info(DualMomUtils.getAssetsToHoldList(assetsPerfSet, 4).toString());
	}
	
	@Test
	public static void testAllocTickers() {
		List<String> tickers = Arrays.asList("LQD", "HYG", "SCZ", "VNQ", "QQQ", "DIA", "EFA", "EEM", "IHI");

		assertEquals("{QQQ=11.0, EFA=11.0, HYG=11.0, VNQ=11.0, SCZ=11.0, IHI=12.0, EEM=11.0, DIA=11.0, LQD=11.0}", 
				DualMomUtils.getEquivalentAssetAllocPercent(tickers).toString());

		tickers = Arrays.asList("LQD", "HYG", "SCZ");
		
		assertEquals("{HYG=33.0, SCZ=34.0, LQD=33.0}", 
				DualMomUtils.getEquivalentAssetAllocPercent(tickers).toString());
		
		tickers = Arrays.asList("LQD", "HYG");
		
		assertEquals("{HYG=50.0, LQD=50.0}", 
				DualMomUtils.getEquivalentAssetAllocPercent(tickers).toString());
		
		tickers = Arrays.asList("LQD", "HYG", "SCZ", "VNQ");

		assertEquals("{HYG=25.0, VNQ=25.0, SCZ=25.0, LQD=25.0}", 
				DualMomUtils.getEquivalentAssetAllocPercent(tickers).toString());	
	}
}

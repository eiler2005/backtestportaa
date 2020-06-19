package ru.backtesting.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class StockQuiteHistoryTest {

	public static void main(String[] args) throws IOException {		
		testTickersValues();
		
		testTradingDays();
	}

	@Test
	public static void testTickersValues() {
		boolean dividens = false;
		
		String ticker = "spy";
		
		StockQuoteHistory.loadStockData(ticker, TradingTimeFrame.Daily, dividens);
	
		// посчитаем перфоманс за период
		
		LocalDateTime curDate = LocalDateTime.parse("2010-01-04T00:00:00");
				
		double quoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, curDate).getClose();
		
		Logger.log().info("Для даты [" + curDate + "] получаем значение котировки [" +  quoteValue + "]");

		if ( dividens == false )
			assertEquals(113.33	, quoteValue);
		else
			assertEquals(92.2028, quoteValue);
	}
	
	public static void testTradingDays() {
		boolean dividens = true;
		String ticker = "spy";
		
		StockQuoteHistory.loadStockData(ticker, TradingTimeFrame.Monthly, dividens);
	
		// посчитаем перфоманс за период
		
		LocalDateTime curDate = LocalDateTime.parse("2020-05-27T00:00:00");
				
		LocalDateTime date = StockQuoteHistory.storage().getFirstTradinDayAtMonthAgo(curDate, ticker, 1);
		
		Logger.log().info("curDate [" + curDate + "] получаем первый торговый день месяца [" +  date + "]");
		
		assertEquals(LocalDateTime.parse("2020-05-01T00:00"), date);
		
		date = StockQuoteHistory.storage().getFirstTradinDayAtMonthAgo(curDate, ticker, 2);
		
		Logger.log().info("curDate [" + curDate + "] получаем первый торговый день месяца вычитая 2 месяца [" +  date + "]");

		assertEquals(LocalDateTime.parse("2020-04-01T00:00"), date);
		
		curDate = LocalDateTime.parse("2020-01-25T00:00:00");
		
		date = StockQuoteHistory.storage().getFirstTradinDayAtMonthAgo(curDate, ticker, 4);
		
		Logger.log().info("curDate [" + curDate + "] получаем первый торговый день месяца вычитая 4 месяца [" +  date + "]");
		
		assertEquals(LocalDateTime.parse("2019-10-01T00:00"), date);

	}
}

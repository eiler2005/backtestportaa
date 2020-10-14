package ru.backtesting.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.patriques.output.AlphaVantageException;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;

public class PortfolioUtils {
	public static double buyPortfolio(List<PositionInformation> positions, AssetAllocation assetsAllocation, TradingTimeFrame period, double moneyAmount) {
		double price = 0;
				
		for (PositionInformation position : positions) {
			String ticker = position.getTickerInf().getTicker();
			double currentQuote = StockQuoteHistory.storage().getQuoteByDate(ticker, period, position.getTime()).getClose();
								
			double quantity = calculateQuantityStocks(ticker, currentQuote, moneyAmount, assetsAllocation);
			
			position.buy(quantity, currentQuote*quantity);
			
			Logger.log().info("Купили [ticker: " + position.getTickerInf() + "] " +Logger.log().doubleAsString(quantity) + 
					" лотов, на сумму " + Logger.log().doubleAsString(currentQuote*quantity));

			price += currentQuote*quantity;
		}
		
		Logger.log().info("Купили активов на сумму: " + Logger.log().doubleAsString(price));
		
		return price;
	}
	
	public static double calculateQuantityStocks(String ticker, double tickerQuote, 
			double portfolioBalance, AssetAllocation assetsAlloc) {
		return (((double)assetsAlloc.getAllocationPercent()/100)*portfolioBalance/tickerQuote);		
	}
	
	public static double calculateAllPositionsBalance(List<PositionInformation> positions) {
		double sum = 0;
		
		for(PositionInformation pos : positions)
			sum += pos.getPrice();
		
		return sum;
	}
	
	public static double calculatePortBalanceOnDate(List<PositionInformation> positions, TradingTimeFrame period, LocalDateTime date, boolean logging) {
		double sum = 0;
		
		if ( logging)
			Logger.log().trace("Делаем расчет стоимости портфеля");
		
		for(PositionInformation pos : positions) {
			String ticker = pos.getTickerInf().getTicker();
			
			if ( ticker.equals(Ticker.CASH_TICKER) ) {
				sum += pos.getPrice();
				
				if ( logging)
					Logger.log().trace("Позиция [" + pos.getTickerInf() + "] лотов [" + Logger.log().doubleAsString(pos.getQuantity()) + "], цена: " + Logger.log().doubleAsString(pos.getPrice()));
			}
			else {				
				if ( StockQuoteHistory.storage().containsQuoteValueInStorage(ticker, period, date) ) {		
					double quoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, period, date).getClose();
						sum += pos.getQuantity()*quoteValue;
				
					if (logging)
						Logger.log().trace("Позиция [" + pos.getTickerInf() + "] лотов [" + Logger.log().doubleAsString(pos.getQuantity()) + "], котировка [" + quoteValue + "], " + 
								"цена: " + Logger.log().doubleAsString(pos.getQuantity()*quoteValue));
				}
				else {
					if (logging)
						Logger.log().trace("Позиция [" + pos.getTickerInf() + "] лотов [" + Logger.log().doubleAsString(pos.getQuantity()) + "] не используется в портфеле, т к не загружены котировки");
					
				}
			}
		}
			
		return sum;
	}
	
	public static boolean isHoldInPortfolio(List<MarketIndicatorInterface> timingSignals, String ticker, TradingTimeFrame period, LocalDateTime date) {
		boolean holdInPort = true;
		
		if ( ticker.equals(Ticker.CASH_TICKER) )
			return true;
		
		if ( timingSignals != null && timingSignals.size() != 0 ) {
			
			for (MarketIndicatorInterface signal : timingSignals) {
				int result = signal.testSignal(date, ticker);
				
				Logger.log().info("Для позиции портфеля с тикером " + ticker + " и датой " + date + 
						" сигнал на покупку/продажу равен: " + result + ", цена акции: " + 
						StockQuoteHistory.storage().getQuoteByDate(ticker, period, date).getClose());
				
				if (result == -1 )
				    holdInPort = false;
			}
		}
		
		return holdInPort;
	}
	
	public static void printPositions(List<PositionInformation> positions) {
		for(PositionInformation pos : positions)
			Logger.log().info(pos.toString());
	}
	

	public static double CAGRInPercent(double beginningBalance, double endingBalance, LocalDate beginningDate, LocalDate endingDate) {		
		long days = DateUtils.duration(beginningDate, endingDate).toDays();
		
		return CAGRInPercent(beginningBalance, endingBalance, (double)( days / 365));
	}
	
	public static double CAGRInPercent(double beginningBalance, double endingBalance, double years) {		
		return (Math.pow(endingBalance/beginningBalance, 1 / years) - 1)*100;
	}

	public static List<PositionInformation> calculatePortPosOnMoneyLimit(LocalDateTime date, TradingTimeFrame timeFrame,
			List<? extends AssetAllocation> assetsAlloc, TickerInf outOfMarketPosTicker, double moneyCount,
			List<MarketIndicatorInterface> signals) {
		List<PositionInformation> positions = new ArrayList<PositionInformation>();
	
		for (int i = 0; i < assetsAlloc.size(); i++) {
			String ticker = assetsAlloc.get(i).getTicker();
	
			double allocationPercent = assetsAlloc.get(i).getAllocationPercent();
	
			if (allocationPercent != 0) {
	
				if (!StockQuoteHistory.storage().containsDataInStorage(ticker, timeFrame)
						&& !ticker.equals(Ticker.CASH_TICKER))
					throw new AlphaVantageException("Не рассчитаны котировки для тикера: " + ticker + " на дату: "
							+ date + " для периода: " + timeFrame);
	
				// надо принять решение покупаем текущую акцию или уходим в outOfMarketTicker
	
				boolean isHoldInPortfolio = isHoldInPortfolio(signals, ticker, timeFrame, date);
	
				Logger.log().info("Приняли решение держать в портфеле(true)/продавать(false) [" + ticker + "] : "
						+ isHoldInPortfolio);
	
				// для кэша все остается без изменений
				double quoteValue = 0, quantity = 1;
	
				if (ticker.equals(Ticker.CASH_TICKER)) {
					quoteValue = (double) allocationPercent * moneyCount / 100;
	
					quantity = 1;
				}
	
				if (isHoldInPortfolio && !ticker.equals(Ticker.CASH_TICKER)) {
					// купить в соответствии с assetAllocation
					// считаем сколько стоит акция на данный момент времени
					quoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, timeFrame, date).getClose();
	
					// считаем сколько мы можем купить акций по цене на данный момент времени с
					// учетом текущей стоимости портфеля
					quantity = calculateQuantityStocks(ticker, quoteValue, moneyCount,
							assetsAlloc.get(i));
	
					// у нас есть на новые покупки quantity*quote
	
					Logger.log()
							.info("Купили в портфель [" + assetsAlloc.get(i).getTickerInf() + "] "
									+ Logger.log().doubleAsString(quantity) + " лотов на сумму "
									+ Logger.log().doubleAsString(quantity * quoteValue) + ", цена лота: "
									+ Logger.log().doubleAsString(quoteValue));
	
					PositionInformation position = new PositionInformation(assetsAlloc.get(i).getTickerInf(), date);
	
					position.buy(quantity, quantity * quoteValue);
	
					positions.add(position);
				} else { // купить в соответствии с outOfMarketTicket и других аллокаций
					// перекладываем текущую позицию в outOfMarketPos
					Logger.log().info("Перекладываемся в hedge-актив " + outOfMarketPosTicker + " вместо " + ticker
							+ " на дату " + date);
	
					PositionInformation hegdePos = new PositionInformation(outOfMarketPosTicker, date);
	
					if (outOfMarketPosTicker.getTicker().equals(Ticker.CASH_TICKER)) {
						hegdePos.buy(1, quoteValue * quantity);
	
						Logger.log().info("Закрыли позицию и вышли в hedge-актив [" + outOfMarketPosTicker
								+ "] на сумму " + Logger.log().doubleAsString(quoteValue * quantity));
					} else {
						double hedgeQuote = StockQuoteHistory.storage()
								.getQuoteByDate(outOfMarketPosTicker.getTicker(), timeFrame, date).getClose();
	
						double hedgeQuantity = (double) allocationPercent * moneyCount / hedgeQuote / 100;
	
						hegdePos.buy(hedgeQuantity, hedgeQuantity * hedgeQuote);
	
						Logger.log().info("Зашли в hedge-актив [" + outOfMarketPosTicker + "]");
	
						Logger.log()
								.info("Купили в портфель [" + outOfMarketPosTicker + "] "
										+ Logger.log().doubleAsString(hedgeQuantity) + " лотов на сумму "
										+ Logger.log().doubleAsString(hedgeQuantity * hedgeQuote) + ", цена лота: "
										+ Logger.log().doubleAsString(hedgeQuote));
					}
	
					positions.add(hegdePos);
				}
			}
		}
	
		return positions;
	}
}

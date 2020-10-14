package ru.backtesting.port.signals;

import java.time.LocalDateTime;
import java.util.List;

import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

public class TopOverMovingAverFixedHandler implements PositionSignalHandler {
	private int percentOver;
	private String ticker;
	private MovingAverageIndicatorSignal sma;
	private LocalDateTime nextDayInPeriod;
	
	private List<PositionInformation> positions;
	private double balance;
		
	public TopOverMovingAverFixedHandler(String ticker, int percentOver) {
		this.ticker = ticker;
		this.percentOver = percentOver;
		
		sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND,
				TradingTimeFrame.Daily, 0);
		
		this.nextDayInPeriod = null;
	}
	
	@Override
	public boolean handleOnDate(LocalDateTime prevDate, LocalDateTime date, Portfolio port, boolean logging) {

		if (nextDayInPeriod == null || (nextDayInPeriod != null && date.isAfter(nextDayInPeriod))) {
			StockQuoteHistory.storage().loadQuotesData(ticker, TradingTimeFrame.Daily, Portfolio.RISK_ON_OFF_TICKERS_CLOSE);

			int riskIndicator = sma.testSignal(date, ticker);

			double smaValue = sma.getIndValue();

			double quoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, sma.getInterval(), date).getClose();

			double growthPerc = ((quoteValue - smaValue) / smaValue) * 100;

			// выше sma на percentOver
			if (growthPerc >= percentOver) {
				if (logging)
					Logger.log()
							.info("-- signal handler " + TopOverMovingAverFixedHandler.class.getSimpleName()
							+ " (выход после превышения на " + percentOver + " процентов sma(160) тикера " + ticker
							+ ") на дату: " + date + " --");
				
				if (logging)
					Logger.log().info(sma.getMarketIndType() + "[" + sma.getTimePeriod() + "] on date [" + date + "]: ticker ["
						+ ticker + "] quote = " + Logger.log().doubleAsString(quoteValue) + ", "
						+ sma.getMarketIndType() + " = " + Logger.log().doubleAsString(smaValue));

				
				this.balance = PortfolioUtils.calculatePortBalanceOnDate(port.getPositionsSet().getPositions(prevDate),
						TradingTimeFrame.Daily, date, false);

				Logger.log().info("growthPerc : " + growthPerc);
				
				Logger.log().info("curPortfolioBalance : " + balance + ", дата: " + date);
				
				List<PositionInformation> fixedPositionList = FixedLossOrProfitSignalHandler
						.calcPositionsForCash(port.getPositionsSet().getSetOfUniquePositions(), balance);

				this.positions = fixedPositionList;

				this.nextDayInPeriod = FixedLossOrProfitSignalHandler.getNextDateInPeriod(date,
						port.getBacktestDates());

				Logger.log().info("-- Зафиксировали прибыль до следующей даты аллокации " + nextDayInPeriod + " --");

				return true;
			}
		}

		return false;
	}

	@Override
	public List<PositionInformation> getPositions() {
		return positions;
	}

	@Override
	public double getBalance() {
		return balance;
	}

	@Override
	public void handleAllocations(List<AssetAllocPerfInf> allocList) {
		FixedLossOrProfitSignalHandler.calcAllOnCashAlloc(allocList);
	}

	public int getPercentOver() {
		return percentOver;
	}

	public String getTicker() {
		return ticker;
	}

	public MovingAverageIndicatorSignal getSma() {
		return sma;
	}
}

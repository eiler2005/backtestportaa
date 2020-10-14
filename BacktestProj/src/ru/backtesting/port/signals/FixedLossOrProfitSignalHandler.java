package ru.backtesting.port.signals;

import java.time.LocalDateTime;
import java.util.List;

import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetAllocationUtils;
import ru.backtesting.port.base.aa.momentum.DualMomUtils;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

public class FixedLossOrProfitSignalHandler implements PositionSignalHandler {
	private int percentPerf;
	private List<PositionInformation> positions;
	private double balance;
	private LocalDateTime nextDayInPeriod;

	public FixedLossOrProfitSignalHandler(int percentPerf) {
		this.percentPerf = percentPerf;
		this.nextDayInPeriod = null;
	}

	@Override
	public boolean handleOnDate(LocalDateTime prevDate, LocalDateTime date, Portfolio port, boolean logging) {
		if (logging)
			Logger.log().info("-- signal handler " + FixedLossOrProfitSignalHandler.class.getSimpleName() + 
					" (выход после прибылей или убытков на " + percentPerf + " процентов) на дату: " + date + " --");

		double curPortfolioBalance = PortfolioUtils.calculatePortBalanceOnDate(
				port.getPositionsSet().getPositions(prevDate), TradingTimeFrame.Daily, date, false);

		double prevBalcance = port.getBalanceOnDate().get(prevDate);

		double growthPerc = ((curPortfolioBalance - prevBalcance) / prevBalcance) * 100;

		// fix profit
		if ( percentPerf >= 0 && growthPerc >= percentPerf)
			return handle(prevDate, date, prevBalcance, curPortfolioBalance, growthPerc, port);

		// fix loss
		if ( percentPerf < 0 && growthPerc <= percentPerf)
			return handle(prevDate, date, prevBalcance, curPortfolioBalance, growthPerc, port);

		return false;
	}

	private boolean handle(LocalDateTime prevDate, LocalDateTime date, double prevBalcance, double curPortfolioBalance,
			double growthPerc, Portfolio port) {
		if (nextDayInPeriod == null || (nextDayInPeriod != null && date.isAfter(nextDayInPeriod))) {

			Logger.log().info("-- Сработал сигнал по фиксации прибыли (или убытка) " + date + " --");

			Logger.log().info("-- handler поиск по балансу: " + date + " --");

			Logger.log().info("prevBalcance : " + prevBalcance + ", дата: " + prevDate);

			Logger.log().info("curPortfolioBalance : " + curPortfolioBalance + ", дата: " + date);

			Logger.log().info("Процент роста > " + percentPerf + " %: " + Logger.log().doubleAsString(growthPerc)
					+ " за период с " + "[" + prevDate + "] по [" + date + "] ");

			List<PositionInformation> fixedPositionList = calcPositionsForCash(port.getPositionsSet().getSetOfUniquePositions(), curPortfolioBalance);

			this.balance = curPortfolioBalance;
			this.positions = fixedPositionList;

			this.nextDayInPeriod = getNextDateInPeriod(date, port.getBacktestDates());

			Logger.log().info("-- Зафиксировали прибыль до следующей даты аллокации " + nextDayInPeriod + " --");

			return true;
		}

		return false;
	}
	
	public static List<PositionInformation> calcPositionsForCash(List<PositionInformation> uniquePositions, double curPortfolioBalance) {
		List<PositionInformation> fixedPositionList = uniquePositions;

		for (PositionInformation pos : fixedPositionList)
			pos.sell();

		PositionInformation cashPos = PositionInformation.findPosByTickers(fixedPositionList, Ticker.cash());

		cashPos.buy(1, curPortfolioBalance);
		
		return fixedPositionList;
	}

	public static void calcAllOnCashAlloc(List<AssetAllocPerfInf> allocList) {
		AssetAllocationUtils.sellAssetsInPort(allocList);

		MomAssetAllocPerfInf aa = new MomAssetAllocPerfInf(Ticker.cash(), null, null, 0, 0,
				DualMomUtils.NOT_AVAILABLE_QUOTE_PERF);

		allocList.add(aa);

		AssetAllocationUtils.setAllocPercentByTicker(Ticker.cash(), 100, allocList);
	}
	
	@Override
	public void handleAllocations(List<AssetAllocPerfInf> allocList) {
		calcAllOnCashAlloc(allocList);
	}

	public static LocalDateTime getNextDateInPeriod(LocalDateTime date, List<LocalDateTime> periodDates) {
		for (LocalDateTime curDate : periodDates) {
			double duration = DateUtils.duration(date.toLocalDate(), curDate.toLocalDate()).toDays();

			if (duration > 0) {
				return curDate;
			}
		}

		return null;
	}

	@Override
	public List<PositionInformation> getPositions() {
		return positions;
	}

	@Override
	public double getBalance() {
		return balance;
	}

	public int getPercentPerf() {
		return percentPerf;
	}
}

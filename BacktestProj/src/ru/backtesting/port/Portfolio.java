package ru.backtesting.port;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.patriques.output.AlphaVantageException;

import com.google.common.collect.Lists;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AssetAllocationBase;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.results.BacktestResultsStorage;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.rebalancing.TimingModelInf;
import ru.backtesting.rebalancing.TimingModelType;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

public class Portfolio {
	public static final String CASH_TICKER = "CASH";

	private String name;
	private int startYear;
	private int endYear;
	private double initialAmount;
	private LinkedHashMap<LocalDateTime, List<PositionInformation>> postionsOnDates;
	private boolean reinvestDividends;
	private TimingModelInf timingModelInf;
	private TimingModel timingModel;
	private String pvzLink;
	private LocalDateTime launchDate;

	public Portfolio(String name, String pvzLink, int startYear, int endYear, double initialAmount,
			TimingModel timingModel, TimingModelInf timingModelInf, boolean reinvestDividends) {
		this.pvzLink = pvzLink;

		PortfolioConsctructor(name, pvzLink, startYear, endYear, initialAmount, timingModel, timingModelInf,
				reinvestDividends);
	}

	public Portfolio(String name, int startYear, int endYear, int initialAmount, TimingModel timingModel,
			TimingModelInf timingModelInf, boolean reinvestDividends) {
		PortfolioConsctructor(name, pvzLink, startYear, endYear, initialAmount, timingModel, timingModelInf,
				reinvestDividends);
	}

	private void PortfolioConsctructor(String name, String pvzLink, int startYear, int endYear, double initialAmount,
			TimingModel timingModel, TimingModelInf timingModelInf, boolean reinvestDividends) {
		this.name = name;
		this.startYear = startYear;
		this.endYear = endYear;
		this.initialAmount = initialAmount;
		this.postionsOnDates = new LinkedHashMap<LocalDateTime, List<PositionInformation>>();
		this.timingModelInf = timingModelInf;
		this.timingModel = timingModel;

		this.reinvestDividends = reinvestDividends;

		Logger.log().info(prinfPortfolioInformation());

		this.launchDate = LocalDateTime.now();

		BacktestResultsStorage.getInstance().putBasePortfolioParams(launchDate, this);

		BacktestResultsStorage.getInstance().putMarketTimingModelParams(launchDate, this);
	}

	public void fillQuotesData() {
		String outOfMarketPosTicker = timingModel.getOutOfMarketPosTicker();
		TradingTimeFrame timeFrame = timingModel.getTimeFrame();

		// портфель забивается данными - без расчета цены и т п
		for (String ticker : timingModel.getPortTickers())
			fillQuotesData(ticker, timeFrame, timingModelInf.getFrequency(), postionsOnDates);

		for (String ticker : timingModel.getAllocChoiceModel().getRiskOnOffTickers())
			StockQuoteHistory.storage().loadQuotesData(ticker, TradingTimeFrame.Daily, reinvestDividends);
		
		if (outOfMarketPosTicker != null && !outOfMarketPosTicker.equals(CASH_TICKER)) {
			if (StockQuoteHistory.storage().containsDataInStorage(outOfMarketPosTicker, timeFrame))
				return;

			StockQuoteHistory.storage().loadQuotesData(outOfMarketPosTicker, timeFrame, reinvestDividends);

			if (!timeFrame.equals(TradingTimeFrame.Daily))
				StockQuoteHistory.storage().loadQuotesData(outOfMarketPosTicker, TradingTimeFrame.Daily,
						reinvestDividends);
		}
	}

	private void fillQuotesData(String ticker, TradingTimeFrame timeFrame, Frequency frequency,
			LinkedHashMap<LocalDateTime, List<PositionInformation>> positions) {
		if (StockQuoteHistory.storage().containsDataInStorage(ticker, timeFrame))
			return;

		StockQuoteHistory.storage().loadQuotesData(ticker, TradingTimeFrame.Daily, reinvestDividends);

		if (!timeFrame.equals(TradingTimeFrame.Daily))
			StockQuoteHistory.storage().loadQuotesData(ticker, timeFrame, reinvestDividends);

		List<LocalDateTime> dates = StockQuoteHistory.storage().getTradingDatesByFilter(ticker, timeFrame, startYear,
				endYear, frequency);

		for (LocalDateTime date : dates)
			if (positions.get(date) == null) {
				List<PositionInformation> otherPositions = new ArrayList<PositionInformation>();
				otherPositions.add(new PositionInformation(ticker, date));

				positions.put(date, otherPositions);
			} else {
				List<PositionInformation> otherPositions = positions.get(date);
				otherPositions.add(new PositionInformation(ticker, date));
			}
	}

	public void backtestPortfolio() {
		TimingModelType modelType = timingModelInf.getMethod();
		TradingTimeFrame timeFrame = timingModel.getTimeFrame();
		List<AssetAllocationBase> assetAllocList = timingModel.getFixedAllocations();
		AllocChoiceModelType allocChoiceType = timingModel.getAllocChoiceModel().getType();
		List<MarketIndicatorInterface> riskControlSignals = timingModel.getRiskControlSignals();

		// BuyAndHold: купил в начале срока - продал в конце
		if (modelType.equals(TimingModelType.BuyAndHold)) {
			// купить по первой дате

			// currPortfolioPrice = PortfolioUtils.buyPortfolio(postionsOnDates.get(key),
			// assetsAllocation, initialAmount, reinvestDividends);

			// продать по последней

			// разница - профит

			// ------------- ДОДЕЛАТЬ

			throw new NotImplementedException("Для портфелей с типом " + modelType + " пока нет реализации");
		} else if (modelType.equals(TimingModelType.AssetAllocationTiming)) {
			LocalDateTime prevDate = null;

			// обход по датам
			for (LocalDateTime date : postionsOnDates.keySet()) {
				Logger.log().info("|| || Формирование портфеля на дату: " + date + " || ||");

				Logger.log().info("Начинаем пересчитывать стоимость позиций в портфеле");

				if (!isQuotesAreAvailable(date, postionsOnDates.get(date)))
					Logger.log().info("На дату [" + date
							+ "] рассчитаны не все котировки, поэтому не пересчитываем содержимое портфеля");
				else {
					double portfolioBalance = prevDate == null ? initialAmount
							: PortfolioUtils.calculateAllPositionsBalance(postionsOnDates.get(prevDate), timeFrame,
									date, true);

					Logger.log()
							.info("Пересчитали стоимость портфеля на дату [" + date
									+ "](сколько денег у нас есть для покупки): "
									+ Logger.log().doubleAsString(portfolioBalance));

					// покупаем или кеш
					// меняем ли ассет аллок

					List<PositionInformation> portPositions = null;

					
					if (allocChoiceType == AllocChoiceModelType.FixedAssetAllocation) {
						// fixed asset alloc

						portPositions = calculatePortOnDate(date, timeFrame, assetAllocList, portfolioBalance,
								riskControlSignals);
					} else if (allocChoiceType == AllocChoiceModelType.Momentum || 
							allocChoiceType == AllocChoiceModelType.MovingAveragesForAsset) {
						List<AssetAllocationBase> alloc = timingModel.calculateAllocationsBySignals(date, launchDate);

						portPositions = calculatePortOnDate(date, timeFrame, alloc, portfolioBalance,
								riskControlSignals);
					} else
						throw new IllegalArgumentException("Необходимо сделать обработку для модели типа:" + allocChoiceType);

					double newPortfolioBalance = PortfolioUtils.calculateAllPositionsBalance(portPositions);

					Logger.log().info("Стоимость портфеля на [" + date + "] : "
							+ Logger.log().doubleAsString(newPortfolioBalance));

					Logger.log()
							.info("Информация по позициям новой аллокации портфеля[с "
									+ date.toLocalDate().plusMonths(1).getMonth() + " "
									+ date.toLocalDate().plusMonths(1).getYear() + " года] ниже:");

					PortfolioUtils.printPositions(portPositions);

					Logger.log().info("-------------");

					postionsOnDates.put(date, portPositions);

					BacktestResultsStorage.getInstance().putPortBalanceOnDate(launchDate, date, newPortfolioBalance);					
					prevDate = date;
				}
				
			}			
		} else if (modelType.equals(TimingModelType.ForSignals)) {
			// вместо распределения ограничиваем риски макс позицией в том или ином
			// инструменте - скорее риск-менеджмент, а не распределение

			// по frequency перекладываемся в сигналы в соответствии с риском asset alloc

			// если нет сигнала, то в защитный актив

			// мощно перекладыываться c частотой раз в день - не чаще

			// ------------- ДОДЕЛАТЬ - нужно ли

			throw new NotImplementedException("Для портфелей с типом " + modelType + " пока нет реализации");
		}
		
		BacktestResultsStorage.getInstance().putStockQuantityInPort(launchDate, timingModel, postionsOnDates);

	}

	public List<PositionInformation> calculatePortOnDate(LocalDateTime date, TradingTimeFrame timeFrame,
			List<AssetAllocationBase> assetsAllocation, double portfolioBalance, List<MarketIndicatorInterface> signals) {
		String outOfMarketPosTicker = timingModel.getOutOfMarketPosTicker();

		List<PositionInformation> positions = postionsOnDates.get(date);
		positions = new ArrayList<PositionInformation>();

		for (int i = 0; i < assetsAllocation.size(); i++) {
			String ticker = assetsAllocation.get(i).getTicker();

			double allocationPercent = assetsAllocation.get(i).getAllocationPercent();

			if (allocationPercent != 0) {

				if (!StockQuoteHistory.storage().containsDataInStorage(ticker, timeFrame)
						&& !ticker.equals(CASH_TICKER))
					throw new AlphaVantageException("Не рассчитаны котировки для тикера: " + ticker + " на дату: "
							+ date + " для периода: " + timeFrame);

				// надо принять решение покупаем текущую акцию или уходим в outOfMarketTicker

				boolean isHoldInPortfolio = PortfolioUtils.isHoldInPortfolio(signals, ticker, timeFrame, date);

				Logger.log().info("Приняли решение держать в портфеле(true)/продавать(false) [" + ticker + "] : "
						+ isHoldInPortfolio);

				// для кэша все остается без изменений
				double quoteValue = 0, quantity = 1;

				if (ticker.equals(CASH_TICKER)) {
					quoteValue = (double) allocationPercent * portfolioBalance / 100;

					quantity = 1;
				}

				if (isHoldInPortfolio && !ticker.equals(CASH_TICKER)) {
					// купить в соответствии с assetAllocation
					// считаем сколько стоит акция на данный момент времени
					quoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, timeFrame, date).getClose();

					// считаем сколько мы можем купить акций по цене на данный момент времени с
					// учетом текущей стоимости портфеля
					quantity = PortfolioUtils.calculateQuantityStocks(ticker, quoteValue, portfolioBalance,
							assetsAllocation.get(i));

					// у нас есть на новые покупки quantity*quote

					Logger.log()
							.info("Купили в портфель [" + ticker + "] " + Logger.log().doubleAsString(quantity)
									+ " лотов на сумму " + Logger.log().doubleAsString(quantity * quoteValue)
									+ ", цена лота: " + Logger.log().doubleAsString(quoteValue));

					PositionInformation position = new PositionInformation(ticker, date);

					position.buy(quantity, quantity * quoteValue);

					positions.add(position);
				} else { // купить в соответствии с outOfMarketTicket и других аллокаций
					// перекладываем текущую позицию в outOfMarketPos
					Logger.log().info("Перекладываемся в hedge-актив " + outOfMarketPosTicker + " вместо " + ticker
							+ " на дату " + date);

					PositionInformation hegdePos = new PositionInformation(outOfMarketPosTicker, date);

					if (outOfMarketPosTicker.equals(CASH_TICKER)) {
						hegdePos.buy(1, quoteValue * quantity);

						Logger.log().info("Закрыли позицию и вышли в hedge-актив [" + outOfMarketPosTicker
								+ "] на сумму " + Logger.log().doubleAsString(quoteValue * quantity));
					} else {
						double hedgeQuote = StockQuoteHistory.storage()
								.getQuoteByDate(outOfMarketPosTicker, timeFrame, date).getClose();

						double hedgeQuantity = (double) allocationPercent * portfolioBalance / hedgeQuote / 100;

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

		// sellAllPositionWhenPortIsFull(positions, assetsAllocation);

		return positions;
	}

	@Deprecated
	private void sellAllPositionWhenPortIsFull(List<PositionInformation> positions, List<AssetAllocationBase> assetsAlloc) {
		int firstFullIndex = -1;

		// ищем первую позиции с 100 процентным уровнем аллокации
		for (int i = 0; i < assetsAlloc.size(); i++) {
			double allocationPercent = assetsAlloc.get(i).getAllocationPercent();

			if (allocationPercent == 100) {
				firstFullIndex = i;
				break;
			}
		}

		// продаем все кроме позиции с 100 процентным уровнем аллокации
		for (int i = 0; i < assetsAlloc.size(); i++) {
			PositionInformation position = positions.get(i);

			if (firstFullIndex != -1 && i != firstFullIndex)
				position.sell();
		}
	}

	public boolean isQuotesAreAvailable(LocalDateTime date, List<PositionInformation> positions) {
		// cмотрим есть ли котировки на текущую дату в базе
		boolean isQuotesAreAvailableCurTickers = false;

		if (PositionInformation.getTickers(positions) != null)
			isQuotesAreAvailableCurTickers = StockQuoteHistory.storage().containsDataInStorageOnDate(
					PositionInformation.getTickers(positions), TradingTimeFrame.Daily, date);

		return isQuotesAreAvailableCurTickers;
	}

	private String prinfPortfolioInformation() {
		TimingModelType modelType = timingModelInf.getMethod();
		TradingTimeFrame timeFrame = timingModel.getTimeFrame();
		String outOfMarketPosTicker = timingModel.getOutOfMarketPosTicker();
		List<AssetAllocationBase> alloc = timingModel.getFixedAllocations();

		String inf = "";

		inf += "|| prinfPortfolioInformation - Параметры портфеля указаны ниже ||" + "\n";
		if (modelType == TimingModelType.BuyAndHold) {
			inf += "Портфель типа Buy&Hold, название : " + name + "\n";
		} else if (modelType == TimingModelType.AssetAllocationTiming) {
			inf += "Портфель типа TimingPortfolio с ребалансировкой активов по пропорциям, название " + name + "\n";
			inf += "Частота ребалансировки активов: " + timingModelInf.getFrequency() + "\n";
			inf += "Распределение активов: " + alloc + "\n";
			inf += "Инвестирование дивидендов: " + reinvestDividends + "\n";

			if (outOfMarketPosTicker != null)
				inf += "Название hedge-актива при медвежьих рынках или срабатывании сигналов: " + outOfMarketPosTicker
						+ "\n";
		} else if (modelType == TimingModelType.ForSignals) {
			inf += "Портфель типа ForSignals с ребалансировкой активов по сигналам, название " + name + "\n";
			inf += "Частота ребалансировки активов: " + timingModelInf.getFrequency() + "\n";
			inf += "Распределение активов: " + alloc + "\n";
			inf += "Инвестирование дивидендов: " + reinvestDividends + "\n";

			if (outOfMarketPosTicker != null)
				inf += "Название hedge-актива при медвежьих рынках или срабатывании сигналов: " + outOfMarketPosTicker
						+ "\n";
		}

		return inf;
	}

	public int getStartYear() {
		return startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public double getInitialAmount() {
		return initialAmount;
	}

	public void printAllPosiotions() {
		Logger.log().info("Portfolio: " + name);
		Logger.log().info("=============");

		for (AssetAllocationBase asset : timingModel.getFixedAllocations()) {
			String ticker = asset.getTicker();
			Logger.log().info("Ticker: " + ticker + ", allocation - " + asset.getAllocationPercent() + " %");
		}

		for (LocalDateTime date : postionsOnDates.keySet()) {
			Logger.log().info("date: " + date);

			List<PositionInformation> positions = postionsOnDates.get(date);

			for (PositionInformation position : positions) {
				StockQuote quote = StockQuoteHistory.storage().getQuoteByDate(position.getTicker(),
						timingModel.getTimeFrame(), position.getTime());

				Logger.log().info("____quantity:   " + position.getQuantity());
				Logger.log().info("____price: " + position.getQuantity() * quote.getClose());
				Logger.log().info("____open:   " + quote.getOpen());
				Logger.log().info("____high:   " + quote.getHigh());
				Logger.log().info("____low:    " + quote.getLow());
				Logger.log().info("____close:  " + quote.getClose());
				Logger.log().info("____dividentAmount:  " + quote.getDividentAmount());
			}

			Logger.log().info("-------------");
		}
	}

	public String getName() {
		return name;
	}

	public LinkedHashMap<LocalDateTime, List<PositionInformation>> getPostionsOnDates() {
		return postionsOnDates;
	}

	public TimingModelInf getModelInf() {
		return timingModelInf;
	}

	public TimingModel getTimingModel() {
		return timingModel;
	}

	public String getPvzLink() {
		return pvzLink;
	}

	public double getFinalBalance() {
		List<LocalDateTime> portDates = Lists.newArrayList(postionsOnDates.keySet());

		int lastIndex = portDates.size() - 1;

		List<PositionInformation> positions = postionsOnDates.get(portDates.get(lastIndex));

		return PortfolioUtils.calculateAllPositionsBalance(positions);
	}

	public boolean isReinvestDividends() {
		return reinvestDividends;
	}

	public void putPortMetrics(double maxDD, double cagr) {
		BacktestResultsStorage.getInstance().putBacktestResults(launchDate, startYear, endYear, cagr, maxDD,
				getFinalBalance());
	}

	public void writeBackTestResultsToExcel() {
		BacktestResultsStorage.getInstance().writeToFile(launchDate);
	}
}

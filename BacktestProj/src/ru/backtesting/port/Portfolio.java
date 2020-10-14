package ru.backtesting.port;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.port.results.BackTestResultsUtils;
import ru.backtesting.port.results.BacktestResultsStorage;
import ru.backtesting.port.signals.PositionSignalHandler;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.rebalancing.TimingModelInf;
import ru.backtesting.rebalancing.TimingModelType;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.GlobalProperties;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

public class Portfolio {
	public static final boolean RISK_ON_OFF_TICKERS_CLOSE = false;
	
	protected String name;
	protected int startYear;
	protected int endYear;
	protected double initialAmount;
	protected boolean reinvestDividends;
	protected TimingModelInf timingModelInf;
	protected TimingModel timingModel;
	protected String pvzLink;
	protected LocalDateTime launchDate;

	protected PositionSet positionsSet;
	protected List<LocalDateTime> backtestDates;
	protected Map<LocalDateTime, Double> balanceOnDate;

	private List<LocalDateTime> allTradingDates;

	protected PositionSignalHandler exitSignalHandler;
	
	protected Portfolio() {

	}

	public Portfolio(String name, String pvzLink, int startYear, int endYear, double initialAmount,
			TimingModel timingModel, TimingModelInf timingModelInf, PositionSignalHandler handler, boolean reinvestDividends) {
		this.pvzLink = pvzLink;

		this.exitSignalHandler = handler;
		
		PortfolioConsctructor(name, pvzLink, startYear, endYear, initialAmount, timingModel, timingModelInf,
				reinvestDividends);
	}

	public Portfolio(String name, int startYear, int endYear, int initialAmount, TimingModel timingModel,
			TimingModelInf timingModelInf, boolean reinvestDividends) {
		PortfolioConsctructor(name, pvzLink, startYear, endYear, initialAmount, timingModel, timingModelInf,
				reinvestDividends);
	}
	
	protected void PortfolioConsctructor(String name, String pvzLink, int startYear, int endYear, double initialAmount,
			TimingModel timingModel, TimingModelInf timingModelInf, boolean reinvestDividends) {
		this.name = name;
		this.startYear = startYear;
		this.endYear = endYear;
		this.initialAmount = initialAmount;
		this.timingModelInf = timingModelInf;
		this.timingModel = timingModel;

		this.reinvestDividends = reinvestDividends;

		backtestDates = new ArrayList<LocalDateTime>();
		positionsSet = new PositionSet();

		Logger.log().info(prinfPortfolioInformation());

		this.launchDate = LocalDateTime.now();

		this.balanceOnDate = new LinkedHashMap<LocalDateTime, Double>();

		allTradingDates = new ArrayList<LocalDateTime>();

		BacktestResultsStorage.getInstance().putBasePortfolioParams(launchDate, this);

		BacktestResultsStorage.getInstance().putMarketTimingModelParams(launchDate, this);
	}
	
	private void fillAllTradingDatesInStore() {
		String ticker = "eem";

		Logger.log().info("Получаем информацию по доступным датам (когда были торги) на базе тикера " + ticker
				+ ", т к у него история с начала 2000ых годов");

		StockQuoteHistory.storage().loadQuotesData(ticker, TradingTimeFrame.Daily, reinvestDividends);

		allTradingDates = StockQuoteHistory.storage().getTradingDatesByPeriod(ticker, TradingTimeFrame.Daily);
	}

	public void fillQuotesData() {
		String outOfMarketPosTicker = timingModel.getOutOfMarketPosTicker().getTicker();
		TradingTimeFrame timeFrame = timingModel.getTimeFrame();

		List<TickerInf> tickersInf = new ArrayList<TickerInf>();
		tickersInf.addAll(timingModel.getPortTickers());
		tickersInf.add(timingModel.getOutOfMarketPosTicker());

		// портфель забивается данными - без расчета цены и т п
		for (TickerInf ticker : tickersInf)
			fillQuotesData(ticker, timeFrame, timingModelInf.getFrequency());

		// in risk-on off alloc model tickers
		for (String ticker : timingModel.getAllocChoiceModel().getRiskOnOffTickers())
			StockQuoteHistory.storage().loadQuotesData(ticker, TradingTimeFrame.Daily, RISK_ON_OFF_TICKERS_CLOSE);

		if (outOfMarketPosTicker != null && !outOfMarketPosTicker.equals(Ticker.CASH_TICKER))
			if (!StockQuoteHistory.storage().containsDataInStorage(outOfMarketPosTicker, timeFrame)) {
				StockQuoteHistory.storage().loadQuotesData(outOfMarketPosTicker, timeFrame, reinvestDividends);

				if (!timeFrame.equals(TradingTimeFrame.Daily))
					StockQuoteHistory.storage().loadQuotesData(outOfMarketPosTicker, TradingTimeFrame.Daily,
							reinvestDividends);
			}

		for (LocalDateTime date : positionsSet.getDates())
			Logger.log().trace("Заполнили портфель пустыми позициями для перед очисткой [" + date + "]:"
					+ positionsSet.getPositions(date));

		Logger.log().info(
				"Выводим всю информацию по датам-тикерам-котировкам, которые есть в базе котировок, даты - positionsSet.getDates()");
		Logger.log().info(
				PortPrintngInformationHelper.printStockQuotes(Ticker.getTiskers(tickersInf), positionsSet.getDates()));

		Logger.log().info(
				"Выводим всю информацию по позициям в портфеле перед прогоном бектеста - positionsSet.getPositions() и перед checkTodayQuotesOnAvailability");
		Logger.log().info(PortPrintngInformationHelper.printPortPositons(positionsSet));

		checkTodayQuotesOnAvailability(timingModel.getPortTickers());

		for (LocalDateTime date : positionsSet.getDates())
			Logger.log().trace(
					"Заполнили портфель пустыми позициями для даты [" + date + "]:" + positionsSet.getPositions(date));

		Logger.log().info(
				"Выводим всю информацию по позициям в портфеле (после checkTodayQuotesOnAvailability()) - positionsSet.getPositions()");
		Logger.log().info(PortPrintngInformationHelper.printPortPositons(positionsSet));

		fillAllTradingDatesInStore();
	}

	private void checkTodayQuotesOnAvailability(List<TickerInf> tickers) {		
		for (int i = backtestDates.size() - 1; i >= 0; i--) {
			LocalDateTime date = backtestDates.get(i);

			if ( isQuotesAreAvailable(date, positionsSet.getPositions(date)) ) {
			
			// if (positionContainsQuote(tickers, date)) {
				Logger.log()
						.trace("Метод checkTodayQuotesOnAvailability: Нашли посл дату в портфеле со всеми котировками ["
								+ date + "].");
			} else {
				Logger.log().trace("Метод checkTodayQuotesOnAvailability: Удаляем из портфеля дату[" + date
						+ "] без полностью заполненных котировок: " + positionsSet.getPositions(date));

				backtestDates.remove(date);
				positionsSet.remove(date);
				balanceOnDate.remove(date);
			}
		}
	}

	@Deprecated
	private boolean positionContainsQuote(List<TickerInf> tickers, LocalDateTime date) {
		for (TickerInf ticker : tickers) {
			boolean findTickers = false;

			for (PositionInformation pos : positionsSet.getPositions(date)) {
				if (pos.getTickerInf().equals(ticker))
					findTickers = true;
			}

			if (findTickers == false) {
				findTickers = StockQuoteHistory.storage().containsQuoteValueInStorage(ticker.getTicker(), TradingTimeFrame.Daily,
						date);

				if (findTickers == false)
					return false;
			}
		}

		return true;
	}

	private void fillQuotesData(TickerInf tickerInf, TradingTimeFrame timeFrame, Frequency frequency) {
		String ticker = tickerInf.getTicker();

		if (StockQuoteHistory.storage().containsDataInStorage(ticker, timeFrame)
				&& positionsSet.containsTicker(tickerInf))
			return;

		StockQuoteHistory.storage().loadQuotesData(ticker, TradingTimeFrame.Daily, reinvestDividends);

		if (!timeFrame.equals(TradingTimeFrame.Daily))
			StockQuoteHistory.storage().loadQuotesData(ticker, timeFrame, reinvestDividends);

		List<LocalDateTime> dates = null;

		if (GlobalProperties.instance().isSoftQuotesInPort()) {
			LocalDateTime firstQuoteAvailabilityDay = StockQuoteHistory.storage().getFirstQuoteAvailabilityDay(ticker,
					timeFrame);

			dates = StockQuoteHistory.storage().getTradingDatesByFilter(ticker, timeFrame, firstQuoteAvailabilityDay,
					startYear, endYear, frequency);
		} else
			dates = StockQuoteHistory.storage().getTradingDatesByFilter(ticker, timeFrame, startYear, endYear,
					frequency);

		Logger.log().trace(
				"Получили торговые даты для [" + tickerInf + "] и типа ребалансировки [" + frequency + "] :" + dates);
		Logger.log().trace(PortPrintngInformationHelper.printTradingDays(ticker, frequency, dates));

		Logger.log().trace("Добавляем даты в текущий набор торговых дней :" + backtestDates);

		DateUtils.addDatesWithSort(backtestDates, dates);

		Logger.log().trace("После сортировки : " + backtestDates);

		for (LocalDateTime date : backtestDates)
			positionsSet.addNewPosition(tickerInf, date);			
	}

	public void backtestPortfolio() {
		TimingModelType modelType = timingModelInf.getMethod();
		TradingTimeFrame timeFrame = timingModel.getTimeFrame();
		List<? extends AssetAllocation> assetAllocList = timingModel.getFixedAllocations();
		AllocChoiceModelType allocChoiceType = timingModel.getAllocChoiceModel().getType();
		List<MarketIndicatorInterface> riskControlSignals = timingModel.getRiskControlSignals();

		Logger.log().info("Портфель будет делать бектест по следующим датам :" + backtestDates);

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
	
			double moneyCount = initialAmount;

			List<PositionInformation> portPositions = null;
			List<AssetAllocPerfInf> curPortAlloc = null;

			boolean runBackTest = true;
			
			for (LocalDateTime date : allTradingDates) {

				boolean isSignalTriggered = false;
				
				if (exitSignalHandler != null && date.getYear() >= startYear && prevDate != null)
					isSignalTriggered = exitSignalHandler.handleOnDate(prevDate, date, this, false);

				// обход по датам из портфеля в соответствии с параметрами ребалансиовки
				if (backtestDates.contains(date) && !isSignalTriggered) {
					Logger.log().info("|| || ___________________________________________ || ||");
					Logger.log().info("|| || Формирование портфеля на дату: " + date + " || ||");

					Logger.log().info("Начинаем пересчитывать стоимость позиций в портфеле");

					if (!isQuotesAreAvailable(date, positionsSet.getPositions(date))) {
						Logger.log().error("На дату [" + date
								+ "] рассчитаны не все котировки, поэтому не пересчитываем содержимое портфеля и прекращаем бектест");
						
						runBackTest = false;
						
						backtestDates.remove(date);
						positionsSet.remove(date);
					}
					else {
						if (prevDate == null) {
							Logger.log().info("Начинаем формировать портфель. Начальный баланс: "
									+ Logger.log().doubleAsString(moneyCount));

							balanceOnDate.put(date, new Double(moneyCount));
						} else {
							Logger.log().info("Рассчитывем стоимость портфеля с даты предыдущего прогона " + prevDate);

							// Logger.log().info("Pred positions: ");

							// List<PositionInformation> prevPos = positionsSet.getPositions(date);
							// PortfolioUtils.printPositions(prevPos);

							double newPortfolioBalance = PortfolioUtils.calculatePortBalanceOnDate(
									positionsSet.getPositions(prevDate), timeFrame, date, true);

							Logger.log().info("Пересчитали стоимость портфеля: "
									+ Logger.log().doubleAsString(newPortfolioBalance) + " на дату " + date);

							double growthPerc = ((newPortfolioBalance - moneyCount) / moneyCount) * 100;

							Logger.log().info("Рост с [" + prevDate + "] по [" + date + "] составил (%): "
									+ Logger.log().doubleAsString(growthPerc));

							moneyCount = newPortfolioBalance;
						}

						// покупаем или кеш
						// меняем ли ассет аллок

						if (allocChoiceType == AllocChoiceModelType.FixedAssetAllocation) {
							// fixed asset alloc

							portPositions = PortfolioUtils.calculatePortPosOnMoneyLimit(date, timeFrame, assetAllocList,
									timingModel.getOutOfMarketPosTicker(), moneyCount, riskControlSignals);

							throw new IllegalArgumentException(
									"Необходимо сделать обработку для модели типа:" + allocChoiceType);
						} else if (allocChoiceType == AllocChoiceModelType.Momentum
								|| allocChoiceType == AllocChoiceModelType.MovingAveragesForAsset) {
							curPortAlloc = timingModel.calculateAllocationsBySignals(date, launchDate);

							portPositions = PortfolioUtils.calculatePortPosOnMoneyLimit(date, timeFrame, curPortAlloc,
									timingModel.getOutOfMarketPosTicker(), moneyCount, riskControlSignals);
						} else
							throw new IllegalArgumentException(
									"Необходимо сделать обработку для модели типа:" + allocChoiceType);
					}
				} else if ( exitSignalHandler != null && isSignalTriggered ){
					Logger.log().info("-- Получили сигнал на исполнение доп. условия на дату, пересчитываем портфель: " + date + " --");
					
					portPositions = exitSignalHandler.getPositions();
					
					moneyCount = exitSignalHandler.getBalance();
					
					if (allocChoiceType == AllocChoiceModelType.FixedAssetAllocation) {
						throw new IllegalArgumentException(
								"Необходимо сделать обработку для модели типа:" + allocChoiceType);					
					} else if (allocChoiceType == AllocChoiceModelType.Momentum
							|| allocChoiceType == AllocChoiceModelType.MovingAveragesForAsset) {
						curPortAlloc = timingModel.calculateAllocationsBySignals(date, launchDate);
					}
					
					exitSignalHandler.handleAllocations(curPortAlloc);
					
					isSignalTriggered = false;
					
					backtestDates.add(date);
					
					Collections.sort(backtestDates);
				}

				if ( backtestDates.contains(date) && runBackTest ) {
				
					balanceOnDate.put(date, new Double(moneyCount));
					
					positionsSet.updatePositions(date, portPositions);

					Logger.log().info("Заполняем портфель по новой аллокации");

					printBackTestResult(date, portPositions, curPortAlloc, moneyCount);

					// put detailed data in excel storage
					if ( timingModelInf.getFrequency() != Frequency.Daily )
						putBackTestResultDataInStorage(launchDate, date, curPortAlloc,
							positionsSet.getSetOfUniquePositions());

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

		BacktestResultsStorage.getInstance().putPortBalance(launchDate, balanceOnDate);

		BacktestResultsStorage.getInstance().putStockQuantityInPort(launchDate, timingModel, positionsSet);

	}
	
	private void printBackTestResult(LocalDateTime date, List<PositionInformation> positions,
			List<AssetAllocPerfInf> alloc, double balance) {
		Logger.log()
				.info("Информация по позициям новой аллокации портфеля[с " + date.toLocalDate().plusMonths(1).getMonth()
						+ " " + date.toLocalDate().plusMonths(1).getYear() + " года] ниже:");

		Logger.log().info("Аллокация в портфеле следующая:");
		Logger.log().info(PortPrintngInformationHelper.printPortAllocations(alloc));

		Logger.log().info(
				"Будем покупать в портфель следующие позиции на сумму: " + Logger.log().doubleAsString(balance));
		
		PortfolioUtils.printPositions(positions);

		Logger.log().info("-------------");

	}
	
	public void putBackTestResultDataInStorage(LocalDateTime launchDate, LocalDateTime date,
			List<AssetAllocPerfInf> allocList, List<PositionInformation> allPositions) {
		// detailed risk-onoff inf to excel
		List<String> riskOnOffTickers = timingModel.getAllocChoiceModel().getRiskOnOffTickers();

		List<AssetAllocPerfInf> riskOnOffAlloc = BackTestResultsUtils.filterAssetAllocPerfInfByParams(riskOnOffTickers,
				allocList);

		Logger.log().info("Помещаем в хранилище результатов Excel данные о аллокациях: (riskOnOff) " + riskOnOffAlloc
				+ " на дату " + date);

		BacktestResultsStorage.getInstance().putDetailedRiskOnOffInformation(launchDate, date, riskOnOffAlloc);

		// detailed other inf to excel

		List<AssetAllocPerfInf> allTickersAlloc = BackTestResultsUtils.generateAssetAllocForPort(allocList,
				allPositions);

		/*
		 * if ( 1 == 1 ) { Logger.log().info("Все позиции для вывода в эксель:");
		 * PortfolioUtils.printPositions(allPositions);
		 * 
		 * Logger.log().info("Аллокация для вывода в эксель:");
		 * Logger.log().trace(PortPrintngInformationHelper.printPortAllocations(
		 * allTickersAlloc)); }
		 */

		Logger.log().info("Помещаем в хранилище результатов Excel данные о аллокациях: (allTickers + outOfMarket) "
				+ allTickersAlloc + " на дату " + date);

		BacktestResultsStorage.getInstance().putDetailedBacktestInformation(launchDate, allTickersAlloc);
	}

	public boolean isQuotesAreAvailable(LocalDateTime date, List<PositionInformation> positions) {
		// cмотрим есть ли котировки на текущую дату в базе
		boolean isQuotesAreAvailableCurTickers = false;

		if (PositionInformation.getTickers(positions) != null)
			isQuotesAreAvailableCurTickers = StockQuoteHistory.storage().containsDataInStorageOnDate(
					Ticker.getTiskers(PositionInformation.getTickers(positions)), TradingTimeFrame.Daily, date);

		return isQuotesAreAvailableCurTickers;
	}

	private String prinfPortfolioInformation() {
		TimingModelType modelType = timingModelInf.getMethod();
		TradingTimeFrame timeFrame = timingModel.getTimeFrame();
		String outOfMarketPosTicker = timingModel.getOutOfMarketPosTicker().getTicker();
		List<? extends AssetAllocation> alloc = timingModel.getFixedAllocations();
		List<TickerInf> tickers = timingModel.getPortTickers();
		
		String inf = "";

		inf += "|| prinfPortfolioInformation - Параметры портфеля указаны ниже ||" + "\n";
		if (modelType == TimingModelType.BuyAndHold) {
			inf += "Портфель типа Buy&Hold, название : " + name + "\n";
		} else if (modelType == TimingModelType.AssetAllocationTiming) {
			inf += "Портфель типа TimingPortfolio с ребалансировкой активов по пропорциям, название " + name + "\n";
			inf += "Частота ребалансировки активов: " + timingModelInf.getFrequency() + "\n";
			
			
			if ( CollectionUtils.isNotEmpty(alloc) )
				inf += "Распределение активов: " + alloc + "\n";
			
			if ( CollectionUtils.isNotEmpty(tickers) )
				inf += "Распределение активов: " + Ticker.getTiskers(tickers) + "\n";
			
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

		for (AssetAllocation asset : timingModel.getFixedAllocations()) {
			String ticker = asset.getTicker();
			Logger.log().info("Ticker: " + ticker + ", allocation - " + asset.getAllocationPercent() + " %");
		}

		for (LocalDateTime date : positionsSet.getDates()) {
			Logger.log().info("date: " + date);

			List<PositionInformation> positions = positionsSet.getPositions(date);

			for (PositionInformation position : positions) {
				StockQuote quote = StockQuoteHistory.storage().getQuoteByDate(position.getTickerInf().getTicker(),
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
		int lastIndex = balanceOnDate.keySet().size() - 1;

		return balanceOnDate.get(balanceOnDate.keySet().toArray()[lastIndex]).doubleValue();
	}

	public boolean isReinvestDividends() {
		return reinvestDividends;
	}

	public void putPortMetrics(double cagr, double maxDD, double underwaterPeriodLenght) {
		BacktestResultsStorage.getInstance().putBacktestResults(launchDate, startYear, endYear, cagr, maxDD,
				underwaterPeriodLenght, getFinalBalance());
	}

	public void writeBackTestResultsToExcel() {
		BacktestResultsStorage.getInstance().writeToFile(launchDate);
	}

	public PositionSet getPositionsSet() {
		return positionsSet;
	}

	public List<LocalDateTime> getBacktestDates() {
		return backtestDates;
	}

	public Map<LocalDateTime, Double> getBalanceOnDate() {
		return balanceOnDate;
	}

	public void addPvzLink(String pvzLink) {
		this.pvzLink = pvzLink;
	}

	public void addPosSignalHandler(PositionSignalHandler portSignalHandler) {
		this.exitSignalHandler = portSignalHandler;
	}

	public PositionSignalHandler getPosSignalHandler() {
		return exitSignalHandler;
	}
}

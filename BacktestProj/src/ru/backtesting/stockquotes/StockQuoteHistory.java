package ru.backtesting.stockquotes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.TimeSeriesResponse;
import org.patriques.output.timeseries.data.StockData;

import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;

public class StockQuoteHistory {	
	private static StockQuoteHistory instance;
	private HashMap<String, List<StockQuote>> quotes;
	private HashMap<String, List<LocalDateTime>> allDates;

	private StockQuoteHistory() {
		allDates = new HashMap<String, List<LocalDateTime>>();
		quotes = new HashMap<String, List<StockQuote>>();
	}

	public static synchronized StockQuoteHistory storage() {
		if (instance == null) {
			instance = new StockQuoteHistory();
		}

		return instance;
	}

	private String getKey(String ticker, TradingTimeFrame period) {
		return "[" + ticker.toLowerCase() + "] , [" + period + "]";
	}

	/*
	 * корректные настройки без дивидендов - daily и getClose с дивами -
	 * dailyAdjusted и adjustedClose
	 */
	public List<LocalDateTime> loadQuotesData(String ticker, TradingTimeFrame period, boolean dividends) {
		String tickerKey = getKey(ticker, period);

		if (containsDataInStorage(ticker, period))
			return allDates.get(tickerKey);

		Logger.log().info("Подключаемся к серверу www.alphavantage.co для получения котировок для тикера [" + ticker
				+ "] для периода " + period);
		
		TimeSeriesResponse response = null;

		if (period.equals(TradingTimeFrame.Daily))
			if ( ticker.equals(Ticker.CASH_TICKER) )
				response = StockConnector.daily(Ticker.CASH_EQUIVALENT_DATES_TICKER, dividends);
			else
				response = StockConnector.daily(ticker, dividends);
		else if (period.equals(TradingTimeFrame.Weekly))
			if ( ticker.equals(Ticker.CASH_TICKER) )
				response = StockConnector.weekly(Ticker.CASH_EQUIVALENT_DATES_TICKER, dividends);
			else
				response = StockConnector.weekly(ticker, dividends);
		else if (period.equals(TradingTimeFrame.Monthly))
			if ( ticker.equals(Ticker.CASH_TICKER) )
				response = StockConnector.monthly(Ticker.CASH_EQUIVALENT_DATES_TICKER, dividends);
			else
				response = StockConnector.monthly(ticker, dividends);


		List<StockData> stockData = response.getStockData();

		if (stockData == null || stockData.isEmpty())
			throw new AlphaVantageException("Для тикера " + ticker + " не найдены данные");

		Collections.reverse(stockData);

		for (StockData stock : stockData) {
			LocalDateTime dateTime = stock.getDateTime();

			StockQuote quote = new StockQuote(ticker, dateTime, stock.getOpen(), stock.getClose(),
					stock.getAdjustedClose(), stock.getHigh(), stock.getLow(), stock.getDividendAmount(), dividends);

			if ( ticker.equals(Ticker.CASH_TICKER) )
				quote = new StockQuote(ticker, dateTime, 1, 1,
						1, stock.getHigh(), 1, 1, dividends);
			
			 //if (!(DateUtils.compareDatesByDay(dateTime, LocalDateTime.parse("2020-06-25T00:00:00"))
			//		&& ticker.equalsIgnoreCase("qqq"))) {

				// add all dates
				if (allDates.get(tickerKey) != null) {
					allDates.get(tickerKey).add(dateTime);
				} else {
					List<LocalDateTime> list = new ArrayList<LocalDateTime>();

					list.add(dateTime);
					allDates.put(tickerKey, list);

				}

				if (quotes.get(tickerKey) != null)
					quotes.get(tickerKey).add(quote);
				else {
					List<StockQuote> list = new ArrayList<StockQuote>();
					list.add(quote);
					quotes.put(tickerKey, list);
				}

			//}
			// else {
			//	 Logger.log().info(
			//				"По активу [" + ticker + "] для периода " + period + " успешно получены котировки.QQQ-123.");
			//}
		}

		Logger.log().info(
				"По активу [" + ticker + "] для периода " + period + " успешно получены котировки. Подробности ниже.");

		Logger.log().info(printQuotes(ticker, period));

		return allDates.get(tickerKey);
	}

	private String printQuotes(String ticker, TradingTimeFrame period) {
		String tickerKey = getKey(ticker, period);

		String quotesStr = ticker + " : ";

		int i = 0;

		for (LocalDateTime date : allDates.get(tickerKey)) {
			quotesStr += "[" + Logger.log().dateAsString(date.toLocalDate()) + " - "
					+ Logger.log().doubleAsString(quotes.get(tickerKey).get(i).getClose()) + "]";

			i++;
		}

		return quotesStr;
	}

	@Deprecated
	private List<LocalDateTime> getDatesByYearFilter(String ticker, TradingTimeFrame period, int startYear,
			int endYear) {
		String tickerKey = getKey(ticker, period);

		List<LocalDateTime> datesByYearFilter = new ArrayList<LocalDateTime>();

		if (containsDataInStorage(ticker, period))
			datesByYearFilter = DateUtils.filterDateListByYear(allDates.get(tickerKey), startYear, endYear);

		if (datesByYearFilter.size() == 0)
			throw new AlphaVantageException("По активу [" + ticker
					+ "] не найдены котировки в промежутке между следующими годами [" + startYear + ", " + endYear
					+ "]. "
					+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.com или они не загружены");
		else
			return datesByYearFilter;
	}

	public List<LocalDateTime> getQuoteDatesForTicker(String ticker, TradingTimeFrame period) {
		return allDates.get(getKey(ticker, period));
	}

	public List<Double> getQuoteValuesByDates(String ticker, TradingTimeFrame period, List<LocalDateTime> dates) {
		List<Double> quoteValues = new ArrayList<Double>();

		for (LocalDateTime date : dates) {
			double quoteValue = getQuoteByDate(ticker, period, date).getClose();

			quoteValues.add(quoteValue);
		}

		return quoteValues;
	}

	public StockQuote getQuoteByDate(String ticker, TradingTimeFrame period, LocalDateTime date) {
		List<StockQuote> list = quotes.get(getKey(ticker, period));

		if ( CollectionUtils.isEmpty(list) )
			throw new AlphaVantageException("По активу [" + ticker + "] на дату " + date + " не загружены котировки. "
					+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.co");
		
		for (StockQuote q : list)
			if (DateUtils.compareDatesByDay(q.getTime(), date))
				return q;

		// попытаемся поискать в других периодах значения котировки акции
		List<TradingTimeFrame> periods = Arrays.asList(
				new TradingTimeFrame[] { TradingTimeFrame.Daily, TradingTimeFrame.Weekly, TradingTimeFrame.Monthly });

		for (TradingTimeFrame curPeriod : periods)
			if (!curPeriod.equals(period) && containsDataInStorageOnDate(ticker, curPeriod, date))
				return getQuoteByDate(ticker, curPeriod, date);

		throw new AlphaVantageException("По активу [" + ticker + "] на дату " + date + " не загружены котировки. "
				+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.co");
	}
	
	public LocalDateTime getFirstQuoteAvailabilityDay(String ticker, TradingTimeFrame period) {
		List<StockQuote> list = quotes.get(getKey(ticker, period));

		if ( list != null && list.size() > 0  )
			return list.get(0).getTime();
		

		// попытаемся поискать в других периодах значения котировки акции
		List<TradingTimeFrame> periods = Arrays.asList(
				new TradingTimeFrame[] { TradingTimeFrame.Daily, TradingTimeFrame.Weekly, TradingTimeFrame.Monthly });

		for (TradingTimeFrame curPeriod : periods)
			if (!curPeriod.equals(period) ) {
				LocalDateTime date = getFirstQuoteAvailabilityDay(ticker, curPeriod);
				
				if (date != null)
					return date;
			}
		
		throw new AlphaVantageException("По активу [" + ticker + "]  не загружены котировки. "
				+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.co");
	}
	
	public boolean containsQuoteValueInStorage(String ticker, TradingTimeFrame period, LocalDateTime date) {
		try {
			getQuoteByDate(ticker, period, date);
			
			return true;
		}
		catch (AlphaVantageException e) {
			return false;
		}
	}
	
	public boolean containsDataInStorage(String ticker, TradingTimeFrame period) {
		String tickerKey = getKey(ticker, period);

		return quotes.containsKey(tickerKey) && allDates.containsKey(tickerKey);
	}

	public LocalDateTime getFirstTradingDayInYear(String ticker, TradingTimeFrame period, int startYear) {
		List<LocalDateTime> datesQ = allDates.get(getKey(ticker, period));

		for (LocalDateTime date : datesQ) {
			if (date.getYear() == startYear && date.getMonth().equals(Month.JANUARY))
				return date;
		}

		return null;
	}

	public LocalDateTime getLastTradingDayInYear(String ticker, TradingTimeFrame period, int year) {
		List<LocalDateTime> datesQ = allDates.get(getKey(ticker, period));

		Collections.reverse(datesQ);

		for (LocalDateTime date : datesQ)
			if (date.getYear() == year)
				return date;

		return null;
	}

	public boolean containsDataInStorageOnDate(List<String> tickers, TradingTimeFrame period, LocalDateTime date) {
		for (String ticker : tickers)
			if (!containsDataInStorageOnDate(ticker, period, date))
				return false;

		return true;
	}

	public boolean containsDataInStorageOnDate(String ticker, TradingTimeFrame period, LocalDateTime date) {
		if ( ticker.equals(Ticker.CASH_TICKER) )
			return true;
		
		List<StockQuote> list = quotes.get(getKey(ticker, period));

		if (list == null)
			return false;

		for (StockQuote q : list)
			if (q != null && DateUtils.compareDatesByDay(q.getTime(), date))
				return true;

		return false;
	}

	public List<LocalDateTime> getTradingDatesByPeriod(String ticker, TradingTimeFrame period) {
		String tickerKey = getKey(ticker, period);

		if (allDates.containsKey(tickerKey))
			return allDates.get(tickerKey);
		else
			throw new AlphaVantageException(
					"Необходимо загрузить данные для тикера " + ticker + " и периода " + period);
	}

	public List<LocalDateTime> getTradingDatesByFilter(String ticker, TradingTimeFrame period, LocalDateTime firstQuoteAvailabilityDay,
			int startYear, int endYear, Frequency frequency) {
		if (!containsDataInStorage(ticker, period))
			throw new AlphaVantageException(
					"Необходимо загрузить данные для тикера " + ticker + " и периода " + period);

		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();

		List<LocalDateTime> frequencyDates = getTradingDatesByPeriod(ticker, period);

		Logger.log().info("Получены даты для тикера " + ticker + " и периода " + period + " :" + frequencyDates);
		
		Logger.log().info("Добавляем первый торговый день в портфель: " + Logger.log().dateAsString(firstQuoteAvailabilityDay.toLocalDate()));

		// добавляем первую торговую дату как в pvz - последний торговый день (startYear
		// - 1)
		
		if ( DateUtils.containsLastDayByParams(frequencyDates, Month.DECEMBER, startYear - 1) ) {
			LocalDateTime firstTradingDay = DateUtils.getLastDayByParams(frequencyDates, Month.DECEMBER, startYear - 1);
			
			filteredDates.add(firstTradingDay);
		}	
		
		switch (frequency) {
		case Annually:
			throw new NotImplementedException("Не реализована обработка для метода ребалансировки " + frequency);
		case SemiAnnually:
			throw new NotImplementedException("Не реализована обработка для метода ребалансировки " + frequency);
		case Quarterly:
			throw new NotImplementedException("Не реализована обработка для метода ребалансировки " + frequency);
		case Monthly:
			filteredDates.addAll(
					DateUtils.filteredDatesByMonth(frequencyDates, firstQuoteAvailabilityDay, startYear, endYear));
			break;
		case Weekly:
			throw new NotImplementedException("Не реализована обработка для метода ребалансировки " + frequency);
		case Daily:
			filteredDates.addAll(
					DateUtils.filteredDatesByDay(frequencyDates, firstQuoteAvailabilityDay, startYear, endYear));
			break;
		default:
			throw new NotImplementedException("Не реализована обработка для метода ребалансировки " + frequency);
		}

		if (filteredDates.size() == 0)
			throw new AlphaVantageException("По активу [" + ticker
					+ "] не найдены котировки в промежутке между следующими годами [" + startYear + ", " + endYear
					+ "]. "
					+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.com или они не загружены");

		Collections.sort(filteredDates);
		
		Logger.log().info("Отфильтрованы даты для тикера " + ticker + " и периода " + period + " с перидочностью "
				+ frequency + " и годами [" + startYear + ", " + endYear + "]" + " :" + filteredDates);
		
		return filteredDates;
	}
	
	public List<LocalDateTime> getTradingDatesByFilter(String ticker, TradingTimeFrame period, int startYear,
			int endYear, Frequency frequency) {
		if (!containsDataInStorage(ticker, period))
			throw new AlphaVantageException(
					"Необходимо загрузить данные для тикера " + ticker + " и периода " + period);

		// if ( period == TradingTimeFrame.Daily )
		// throw new RuntimeException("Некорректно указан период TradingPeriod. Нельзя
		// указывать TradingPeriod = Daily. "
		// + "Это приведет к некорретному расчету портфеля каждый день, что не приведет
		// к хорошим результатам");

		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();

		List<LocalDateTime> frequencyDates = getTradingDatesByPeriod(ticker, period);

		Logger.log().info("Получены даты для тикера " + ticker + " и периода " + period + " :" + frequencyDates);

		// добавляем первую торговую дату как в pvz - последний торговый день (startYear
		// - 1)
		LocalDateTime firstTradingDay = DateUtils.getLastDayByParams(frequencyDates, Month.DECEMBER, startYear - 1);
		filteredDates.add(firstTradingDay);

		Logger.log().info("Добавляем первый торговый день как в Portfolio Visualizer - последний торговый день года "
				+ (startYear - 1) + ": " + Logger.log().dateAsString(firstTradingDay.toLocalDate()));

		switch (frequency) {
		case Annually:
			filteredDates.addAll(DateUtils.filteredDatesByAnnyally(frequencyDates, startYear, endYear));
			break;
		case SemiAnnually:
			filteredDates.addAll(DateUtils.filteredDatesBySemiAnnually(frequencyDates, startYear, endYear));
			break;
		case Quarterly:
			filteredDates.addAll(DateUtils.filteredDatesByQuarter(frequencyDates, startYear, endYear));
			break;
		case Monthly:
			filteredDates.addAll(DateUtils.filteredDatesByMonth(frequencyDates, startYear, endYear));
			break;
		case Weekly: // todo
			filteredDates.addAll(DateUtils.filteredDatesByWeek(frequencyDates, startYear, endYear));
			break;
		case Daily:
			filteredDates.addAll(DateUtils.filteredDatesByDay(frequencyDates, startYear, endYear));
			break;
		default:
			throw new NotImplementedException("Не реализована обработка для метода ребалансировки " + frequency);
		}

		if (filteredDates.size() == 0)
			throw new AlphaVantageException("По активу [" + ticker
					+ "] не найдены котировки в промежутке между следующими годами [" + startYear + ", " + endYear
					+ "]. "
					+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.com или они не загружены");

		Logger.log().info("Отфильтрованы даты для тикера " + ticker + " и периода " + period + " с перидочностью "
				+ frequency + " и годами [" + startYear + ", " + endYear + "]" + " :" + filteredDates);

		return filteredDates;
	}

	public LocalDateTime getPreviousTradingDay(LocalDateTime date, String ticker, int daysPeriod) {
		List<LocalDateTime> datesList = StockQuoteHistory.storage().getTradingDatesByPeriod(ticker,
				TradingTimeFrame.Daily);

		LocalDateTime[] datesArr = new LocalDateTime[datesList.size()];
		datesArr = datesList.toArray(datesArr);

		int dateInd = 0;

		for (int i = 0; i < datesArr.length; i++)
			if (date.equals(datesArr[i])) {
				dateInd = i;

				break;
			}

		// берем первую дату
		LocalDateTime pastDate = datesArr[0];

		// или отсчитываем daysPeriod
		if (daysPeriod <= dateInd)
			pastDate = datesArr[dateInd - daysPeriod];

		return pastDate;
	}

	public boolean containsTradinDayAtMonthAgo(LocalDateTime date, String ticker, int periodInMonth) {
		if (periodInMonth < 1)
			throw new IllegalArgumentException(
					"Период в месяцах должен быть больше или равен 1, наш период равен - " + periodInMonth);

		try {
			List<LocalDateTime> datesList = StockQuoteHistory.storage().getTradingDatesByPeriod(ticker,
					TradingTimeFrame.Daily);

			boolean isFistDayOfYear = DateUtils.compareDatesByDay(date,
					DateUtils.getFirsDayByParams(datesList, date.getMonth(), date.getYear()));

			Month month = date.getMonth();
			int year = date.getYear();

			LocalDateTime firstTradingDay = null;

			// берем первый день месяца, если дата за в этом месяце
			if (periodInMonth == 1 && !isFistDayOfYear) {
				firstTradingDay = DateUtils.getFirsDayByParams(datesList, month, year);
			}
			// если текущая дата равна первому дню месяца и мы хотим дату на месяц назад -
			// вычитаем 1 месяц и берем первый день месяца
			else if (periodInMonth == 1) {
				LocalDateTime firstDayOfMonth = DateUtils.getFirsDayByParams(datesList, month, year);

				if (DateUtils.compareDatesByDay(firstDayOfMonth, date)) {
					LocalDate subtructDate = firstDayOfMonth.toLocalDate().minusMonths(1);

					firstTradingDay = DateUtils.getFirsDayByParams(datesList, subtructDate.getMonth(),
							subtructDate.getYear());
				}
			} else {
				LocalDate subtructDate = date.toLocalDate().minusMonths(periodInMonth - 1);

				firstTradingDay = DateUtils.getFirsDayByParams(datesList, subtructDate.getMonth(),
						subtructDate.getYear());
			}

			if (DateUtils.compareDatesByDay(date, firstTradingDay))
				throw new RuntimeErrorException(new Error("Получили дату на месяц ранее для актива " + ticker
						+ " от даты " + Logger.log().dateAsString(date.toLocalDate())
						+ ". Даты равны. Это некорректно. Проверьте программу на наличие ошибок!"));

			if (firstTradingDay != null)
				return true;
			else
				return false;
		} catch (AlphaVantageException e) {
			return false;
		}
	}
	
	public LocalDateTime getFirstTradinDayAtMonthAgo(LocalDateTime date, String ticker, int periodInMonth) {
		if (periodInMonth < 1)
			throw new IllegalArgumentException(
					"Период в месяцах должен быть больше или равен 1, наш период равен - " + periodInMonth);

		List<LocalDateTime> datesList = StockQuoteHistory.storage().getTradingDatesByPeriod(ticker,
				TradingTimeFrame.Daily);

		boolean isFistDayOfYear = DateUtils.compareDatesByDay(date,
				DateUtils.getFirsDayByParams(datesList, date.getMonth(), date.getYear()));

		Month month = date.getMonth();
		int year = date.getYear();

		LocalDateTime firstTradingDay = null;
		
		// берем первый день месяца, если дата за в этом месяце
		if (periodInMonth == 1 && !isFistDayOfYear) {
			firstTradingDay =  DateUtils.getFirsDayByParams(datesList, month, year);
		}
		// если текущая дата равна первому дню месяца и мы хотим дату на месяц назад - вычитаем 1 месяц и берем первый день месяца
		else if (periodInMonth == 1) {
			LocalDateTime firstDayOfMonth = DateUtils.getFirsDayByParams(datesList, month, year);
			
			if (DateUtils.compareDatesByDay(firstDayOfMonth, date)) {
				LocalDate subtructDate = firstDayOfMonth.toLocalDate().minusMonths(1);

				firstTradingDay =  DateUtils.getFirsDayByParams(datesList, subtructDate.getMonth(), subtructDate.getYear());
			}
		} else {
			LocalDate subtructDate = date.toLocalDate().minusMonths(periodInMonth - 1);

			firstTradingDay = DateUtils.getFirsDayByParams(datesList, subtructDate.getMonth(), subtructDate.getYear());
		}
		
		if (  DateUtils.compareDatesByDay(date, firstTradingDay))
			throw new RuntimeErrorException(new Error("Получили дату на месяц ранее для актива " + ticker + " от даты " + Logger.log().dateAsString(date.toLocalDate()) + 
					". Даты равны. Это некорректно. Проверьте программу на наличие ошибок!"));
		else
			return firstTradingDay;
	}

	public static void loadStockData(String ticker, TradingTimeFrame period, boolean dividends) {
		storage().loadQuotesData(ticker, period, dividends);

		if (!period.equals(TradingTimeFrame.Daily))
			storage().loadQuotesData(ticker, TradingTimeFrame.Daily, dividends);
	}
}

package ru.backtesting.port.metrics;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.google.common.primitives.Longs;

import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

public class PortfolioMetrics {
	private final static String CN_NAME_PORT_BALANCE = "portfolio balance";
	private final static String CN_NAME_PORT_PERCENT_DRAWDOWN = "drawdown (%)";
	private final static String CN_NAME_PORT_PERCENT_MAXPROFT = "max profit in period (%)";

	private final static String CN_NAME_PORT_BETTER_PERIOD_DRAWDOWN = "better period lenght (days)";
	private final static String CN_NAME_PORT_DATE = "initial date";
	private final static String CN_NAME_MAX_DRAWDOWN_DATE = "max drawdown date";
	private final static String TABLE_NAME_PORT_BALANCE_ON_DATE = "Portfolio balance on date (for drawdown calculating)";
	
	private Portfolio portfolio;
	
	public PortfolioMetrics(Portfolio port) {
		// TODO Auto-generated constructor stub
		this.portfolio = port;
	}
	
	// table sample
	// Worst 10 drawdowns included above
	// Drawdowns for Timing Portfolio
    //	Rank	Start	    End	          Length	Recovery    By	Recovery     Time	Underwater Period   Drawdown
    //	1	    Sep 1987	Nov 1987	3 months	Jan 1989	1 year 2 months	   1 year 5 months	         -19.36%
	public void calcDrawdown() {
		LinkedHashMap<LocalDateTime, List<PositionInformation>> positions = portfolio.getPostionsOnDates();
		
		int startYear = portfolio.getStartYear();
		int endYear = portfolio.getEndYear();

		Set<String> allTickers = portfolio.getAllTickersInPort();
		
		List<PositionInformation> rebalancedPositions = null;
				
		List<LocalDateTime> datesColumnData = new ArrayList<LocalDateTime>();
		List<Number> balanceOnDateColumnData = new ArrayList<Number>();
		
		for (LocalDate locDate : DateUtils.getDatesBetweenUsingJava8(LocalDate.parse(startYear + "-01-01"), 
				LocalDate.parse(endYear + "-12-31"))) {	
			LocalDateTime date = DateUtils.asLocalDateTime(DateUtils.asDate(locDate));
						
			boolean isHaveStockData = StockQuoteHistory.storage().containsDataInStorageOnDate(allTickers, date);
			
			// начинаем обход всех дат, на которые есть котировки по тикерам портфеля
			if ( isHaveStockData && getPostionOnDateDay(positions, date) != null ) {
				// берем позиции из портфеля и пересчитываем баланс на дату
				// далее по указанным позициям пересчитываем портфель до слебующей ребалансировке
				
				List<PositionInformation> curPositions = positions.get(getPostionOnDateDay(positions, date));
					
				double balance = PortfolioUtils.calculateAllPositionsBalance(curPositions, date, false, false);
										
				rebalancedPositions = curPositions;	
				
				datesColumnData.add(date);
				balanceOnDateColumnData.add(new Double(balance));
				
			}
			else if (rebalancedPositions != null && isHaveStockData) {
				double balance = PortfolioUtils.calculateAllPositionsBalance(rebalancedPositions, date, false, false);
								
				// ДОДЕЛАТЬ - даты как в портфеле или ежедневный пересчет
				//datesColumnData.add(date);
				//balanceOnDateColumnData.add(new Double(balance));
			}
		}
		
		DoubleColumn balanceColumn = DoubleColumn.create(CN_NAME_PORT_BALANCE, balanceOnDateColumnData);
		balanceColumn.setPrintFormatter(Logger.DOUBLE_FORMAT, "_missing value_");
		
		DateTimeColumn dateColumn = DateTimeColumn.create(CN_NAME_PORT_DATE, datesColumnData);
		dateColumn.setPrintFormatter(Logger.DATE_FORMAT_SIMPLE);
		
		Table balancedTable = Table.create(TABLE_NAME_PORT_BALANCE_ON_DATE)
				.addColumns(dateColumn, balanceColumn);
			
		List<Number> drawdownColumnData = new ArrayList<Number>();
		List<Number> betterPeriodLenghtData = new ArrayList<Number>();
		List<LocalDateTime> maxDrawdownDateData = new ArrayList<LocalDateTime>();
		List<Number> betterPeriodMaxProfitData = new ArrayList<Number>();
		
		for(int i =0; i < datesColumnData.size(); i++) {
			LocalDateTime date = datesColumnData.get(i);
			double balance = balanceOnDateColumnData.get(i).doubleValue();
			
			System.out.println("|| filter - " + date + ", balance - " + balance);

			// ищем все даты, на время которых баланс портфеля был меньше текущего - просадки и время в течение которого портфель мог уйти в минус
			Table datesAndBalanceWithDrawndownT = balancedTable.select(CN_NAME_PORT_DATE, CN_NAME_PORT_BALANCE).
				where(balancedTable.dateTimeColumn(CN_NAME_PORT_DATE).isBetweenIncluding(date, LocalDateTime.now()).
					and(balancedTable.doubleColumn(CN_NAME_PORT_BALANCE).isLessThanOrEqualTo(balance))).
				dropRowsWithMissingValues();
			
			if ( !datesAndBalanceWithDrawndownT.isEmpty() ) {
				List<LocalDateTime> filteredDates = datesAndBalanceWithDrawndownT.dateTimeColumn(CN_NAME_PORT_DATE).asList();
				List<Double> filteredValue = datesAndBalanceWithDrawndownT.doubleColumn(CN_NAME_PORT_BALANCE).asList();
			
				double minValue = Collections.min(filteredValue);
				double maxValue = Collections.max(filteredValue);
				
				double percentDD = (balance-minValue)/balance*100;
				double percentMaxProfit = (maxValue-balance)/balance*100;

				LocalDateTime drawdownDate = filteredDates.get(filteredValue.indexOf(minValue));
				
				Duration duration = Duration.between(date, drawdownDate);
				
				System.out.println(datesAndBalanceWithDrawndownT.print(50));
				System.out.println("period with loss, in days = " + duration.toDays() + ", max drawdown[balance - "+ balance + 
						", minvalue - " + minValue + ", date - " + drawdownDate + "] = " + percentDD + " %, maxProfit - " + percentMaxProfit + " %");				
				
				drawdownColumnData.add(Double.valueOf(percentDD));
				betterPeriodLenghtData.add(Long.valueOf(duration.toDays()));
				maxDrawdownDateData.add(drawdownDate);
				betterPeriodMaxProfitData.add(percentMaxProfit);
			}
			else {
				drawdownColumnData.add(Double.valueOf(0));
				betterPeriodLenghtData.add(Long.valueOf(0));
				betterPeriodMaxProfitData.add(Double.valueOf(0));
			}
		}
				
		balancedTable.addColumns(DoubleColumn.create(CN_NAME_PORT_PERCENT_DRAWDOWN, drawdownColumnData));
		balancedTable.addColumns(LongColumn.create(CN_NAME_PORT_BETTER_PERIOD_DRAWDOWN, Longs.toArray(betterPeriodLenghtData)));
		balancedTable.addColumns(DateTimeColumn.create(CN_NAME_MAX_DRAWDOWN_DATE, maxDrawdownDateData));
		balancedTable.addColumns(DoubleColumn.create(CN_NAME_PORT_PERCENT_MAXPROFT, betterPeriodMaxProfitData));

		
		// таблицу с максимальной просадкой после даты входа в портфель	
		
		System.out.println("||drawdown sort table||");
		
		System.out.println(balancedTable.sortAscendingOn(CN_NAME_PORT_PERCENT_DRAWDOWN).print(30));
		
		System.out.println("max dd - " + Collections.max(balancedTable.doubleColumn(CN_NAME_PORT_PERCENT_DRAWDOWN).asList()));
		
		System.out.println("||underwater period sort table||");
		System.out.println(balancedTable.sortAscendingOn(CN_NAME_PORT_BETTER_PERIOD_DRAWDOWN).print(30));
		System.out.println("underwater lenght - " + Collections.max(balancedTable.longColumn(CN_NAME_PORT_BETTER_PERIOD_DRAWDOWN).asList()));

	}
	
	private LocalDateTime getPostionOnDateDay(LinkedHashMap<LocalDateTime, 
			List<PositionInformation>> positions, LocalDateTime myDate) {
		for(LocalDateTime date : positions.keySet() ) 
			if ( DateUtils.compareDatesByDay(date, myDate) )
				return date;
		
		return null;
		
	}
}

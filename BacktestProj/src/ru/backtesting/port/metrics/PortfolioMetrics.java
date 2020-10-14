package ru.backtesting.port.metrics;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Longs;

import ru.backtesting.port.Portfolio;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;
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
	private final static String CN_NAME_MIN_BALANCE_FOR_DRAWDOWN_DATE = "min balance (for drawdown date)";

	
	private final static String TABLE_NAME_PORT_BALANCE_ON_DATE = "Portfolio balance on date (for drawdown calculating)";
	
	private Portfolio portfolio;
	private double maxDrawdown;
	private double underwaterPeriodLenght;
	
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
		List<LocalDateTime> datesColumnData = new ArrayList<LocalDateTime>();
		List<Number> balanceOnDateColumnData = new ArrayList<Number>();
				
		for (LocalDateTime date : portfolio.getBacktestDates() ) {							
			datesColumnData.add(date);
			balanceOnDateColumnData.add(portfolio.getBalanceOnDate().get(date));
		}
		
		DoubleColumn balanceColumn = DoubleColumn.create(CN_NAME_PORT_BALANCE, balanceOnDateColumnData);		
		DateTimeColumn dateColumn = DateTimeColumn.create(CN_NAME_PORT_DATE, datesColumnData);
		
		Table balancedTable = Table.create(TABLE_NAME_PORT_BALANCE_ON_DATE)
				.addColumns(dateColumn, balanceColumn);
			
		List<Number> drawdownColumnData = new ArrayList<Number>();
		List<Number> betterPeriodLenghtData = new ArrayList<Number>();
		List<LocalDateTime> maxDrawdownDateData = new ArrayList<LocalDateTime>();
		List<Number> minBalanceForDDColumnData = new ArrayList<Number>();
		List<Number> betterPeriodMaxProfitData = new ArrayList<Number>();
		
		for(int i =0; i < datesColumnData.size(); i++) {
			LocalDateTime date = datesColumnData.get(i);
			double balance = balanceOnDateColumnData.get(i).doubleValue();
			
			Logger.log().trace("|| Заполняем таблицу для расчета drawdown - " + Logger.log().dateAsString(date.toLocalDate()) + ", balance - " 
					+ Logger.log().doubleAsString(balance));
			
			// ищем все даты, на время которых баланс портфеля был меньше текущего - просадки и время в течение которого портфель мог уйти в минус
			Table datesAndBalanceWithDrawndownT = balancedTable.select(CN_NAME_PORT_DATE, CN_NAME_PORT_BALANCE).
				where(balancedTable.dateTimeColumn(CN_NAME_PORT_DATE).isBetweenIncluding(date, LocalDateTime.now()).
					and(balancedTable.doubleColumn(CN_NAME_PORT_BALANCE).isLessThanOrEqualTo(balance))).
				dropRowsWithMissingValues();
			Logger.setTableFormatter(datesAndBalanceWithDrawndownT);			
			
			
			
			if ( !datesAndBalanceWithDrawndownT.isEmpty() ) {
				List<LocalDateTime> filteredDates = datesAndBalanceWithDrawndownT.dateTimeColumn(CN_NAME_PORT_DATE).asList();
				List<Double> filteredValue = datesAndBalanceWithDrawndownT.doubleColumn(CN_NAME_PORT_BALANCE).asList();
			
				double minBalance = Collections.min(filteredValue);
				double percentDD = (balance-minBalance)/balance*100;

				LocalDateTime drawdownDate = filteredDates.get(filteredValue.indexOf(minBalance));
				
				Duration duration = DateUtils.duration(date.toLocalDate(), drawdownDate.toLocalDate());
				
				Logger.log().trace(datesAndBalanceWithDrawndownT.printAll() + "\n");
				
				// ищем все даты, на время которых баланс портфеля был больше текущего - для max profit
				Table datesAndBalanceWithoutDrawndownT = balancedTable.select(CN_NAME_PORT_DATE, CN_NAME_PORT_BALANCE).
						where(balancedTable.dateTimeColumn(CN_NAME_PORT_DATE).isBetweenIncluding(date, LocalDateTime.now()).
							and(balancedTable.doubleColumn(CN_NAME_PORT_BALANCE).isGreaterThanOrEqualTo(balance))).
						dropRowsWithMissingValues();
				Logger.setTableFormatter(datesAndBalanceWithoutDrawndownT);			

				double maxValue = Collections.max(datesAndBalanceWithoutDrawndownT.doubleColumn(CN_NAME_PORT_BALANCE).asList());
				double percentMaxProfit = (maxValue-balance)/balance*100;
				// max profit
				
				Logger.log().trace("period with loss, in days = " + duration.toDays() + ", max drawdown[balance - " + Logger.log().doubleAsString(balance) + 
						", minvalue - " + Logger.log().doubleAsString(minBalance) + ", date - " + drawdownDate + "] = " + Logger.log().doubleAsString(percentDD) + " %, maxProfit - " + Logger.log().doubleAsString(percentMaxProfit) + " %");				
				
				drawdownColumnData.add(Double.valueOf(percentDD));
				betterPeriodLenghtData.add(Long.valueOf(duration.toDays()));
				maxDrawdownDateData.add(drawdownDate);
				minBalanceForDDColumnData.add(new Double(minBalance));
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

		// дата низшей точки и баланс
		balancedTable.addColumns(DateTimeColumn.create(CN_NAME_MAX_DRAWDOWN_DATE, maxDrawdownDateData));
		
		balancedTable.addColumns(DoubleColumn.create(CN_NAME_MIN_BALANCE_FOR_DRAWDOWN_DATE, minBalanceForDDColumnData));
				
		
		balancedTable.addColumns(DoubleColumn.create(CN_NAME_PORT_PERCENT_MAXPROFT, betterPeriodMaxProfitData));

		Logger.setTableFormatter(balancedTable);
		
		// таблицу с максимальной просадкой после даты входа в портфель	
		
		Logger.log().info("||drawdown sort table||");
		Logger.log().info(balancedTable.sortAscendingOn(CN_NAME_PORT_DATE).printAll());

		// Logger.log().info(balancedTable.sortAscendingOn(CN_NAME_PORT_PERCENT_DRAWDOWN).printAll());
		
		this.maxDrawdown  = Collections.max(balancedTable.doubleColumn(CN_NAME_PORT_PERCENT_DRAWDOWN).asList()).doubleValue();
				
		Logger.log().info("max dd - " + Logger.log().doubleAsString(maxDrawdown));
		
		this.underwaterPeriodLenght = Collections.max(balancedTable.longColumn(CN_NAME_PORT_BETTER_PERIOD_DRAWDOWN).asList()).doubleValue();
		
		Logger.log().info("||underwater period sort table||");
		Logger.log().info(balancedTable.sortAscendingOn(CN_NAME_PORT_BETTER_PERIOD_DRAWDOWN).print(30));
		Logger.log().info("underwater lenght - " + Logger.log().doubleAsString(underwaterPeriodLenght));

		
		Logger.log().info("||max profit in period sort table||");
		Logger.log().info(balancedTable.sortAscendingOn(CN_NAME_PORT_PERCENT_MAXPROFT).print(30));
		Logger.log().info("max profit in period (%) - " + Logger.log().doubleAsString(Collections.max(balancedTable.doubleColumn(CN_NAME_PORT_PERCENT_MAXPROFT).asList())));
	}
	
	public double getMaxDrawdown() {
		return maxDrawdown;
	}

	public double getUnderwaterPeriodLenght() {
		return underwaterPeriodLenght;
	}

	public double CAGRInPercent() {		
		
		List<LocalDateTime> backtestDates = new ArrayList<>(portfolio.getBalanceOnDate().keySet());
		
		LocalDate startYear = backtestDates.get(0).toLocalDate();
		
		LocalDate endDate = backtestDates.get(backtestDates.size() - 1).toLocalDate();
		
		double finalBalance = portfolio.getFinalBalance();
				
		return PortfolioUtils.CAGRInPercent(portfolio.getInitialAmount(), finalBalance, startYear, endDate);
	}
}

package ru.backtesting.port;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.doubles.DeduplicateValues;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

public class PortPrintngInformationHelper {
	public static String printStockQuotes(List<String> tickers, Collection<LocalDateTime> dates) {
		Table table = Table.create("Stock dates and quotes (date/ticker/quote value)");

		DateColumn dateColumn = DateColumn.create("date", DateUtils.asLocalDate(dates));

		table.addColumns(dateColumn);

		DeduplicateValues deduplicator = new DeduplicateValues();
		
		
		for (String ticker : tickers ) {
			double[] quoteArr = new double[dates.size()];
			
			int i = 0;
					
			for (LocalDateTime date : dates) {
				double quoteValue = 0;

				if (StockQuoteHistory.storage().containsQuoteValueInStorage(ticker, TradingTimeFrame.Daily, date))
					quoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, TradingTimeFrame.Daily, date).getClose();

				quoteArr[i] = quoteValue;
				
				i++;
			}
			
			
			DoubleColumn quoteColumn = DoubleColumn.create(deduplicator.check(ticker), 
					quoteArr);

			table.addColumns(quoteColumn);
		}

		return "\n" + table.printAll();
	}
	
	public static String printTradingDays(String ticker, Frequency frequency, List<LocalDateTime> dates) {
		Table table = Table.create("Trading days for ticker " + ticker + " and frequency (date/ticker) + " + frequency);
		
		DateColumn dateColumn = DateColumn.create("date", DateUtils.asLocalDate(dates));

		table.addColumns(dateColumn);
		
		return "\n" + table.printAll();
	}
	
	public static String printPortAllocations(List<AssetAllocPerfInf> allocations) {
		List<Column<?>> asetsAllocColumnList = new ArrayList<>();

		DeduplicateValues deduplicator = new DeduplicateValues();
		
		for (AssetAllocPerfInf inf : allocations) {
			String ticker = inf.getTicker();
			double allocPersent = inf.getAllocationPercent();
			
			DoubleColumn allocColumn = DoubleColumn.create(deduplicator.check(ticker),
					Arrays.asList(new Double[] { new Double(allocPersent) }));

			asetsAllocColumnList.add(allocColumn);
		}
		
		Table allocTable = Table.create("Port allocations (asset/allocation in %)", 
				asetsAllocColumnList.toArray(new Column<?>[asetsAllocColumnList.size()]));
		
		return "\n" + allocTable.printAll();

	}
	
	public static String printPortPositons(PositionSet positionsSet) {
		Table table = Table.create("Position table - date and blank positions (date/positions)");

		Set<LocalDateTime> dates = positionsSet.getDates();
		
		DateColumn dateColumn = DateColumn.create("date", DateUtils.asLocalDate(dates));

		table.addColumns(dateColumn);
		
		String[] postStrArr = new String[dates.size()];
				
		int i = 0;
		
		for (LocalDateTime date : dates) {
			List<PositionInformation> positions = positionsSet.getPositions(date);
			
			String posStr = "";
			
			for (PositionInformation pos :  positions ) {
				posStr += pos.getTickerInf().getTickerId() + " ";
			}
			
			postStrArr[i] = posStr;
			
			i++;
		}
				
		StringColumn posColumn = StringColumn.create("positions", postStrArr);

		table.addColumns(posColumn);
		
		return "\n" + table.printAll();
	}
	
	public static List<String> deduplicateColumns(List<String> columnNames) {
		Logger.log().trace("deduplicateColumns");
		
		String[] my_array = new String[columnNames.size()];
		
		my_array = columnNames.toArray(my_array);
		 
        for (int i = 0; i < my_array.length-1; i++)
        {
            for (int j = i+1; j < my_array.length; j++)
            {
                if( (my_array[i].equals(my_array[j])) && (i != j) )
                {
                	Logger.log().trace("Duplicate Element is : " + my_array[j]);
                	
                	my_array[j] = my_array[j] + "_";           
                }
            }
        }
        
        return Arrays.asList(my_array);
	}
}

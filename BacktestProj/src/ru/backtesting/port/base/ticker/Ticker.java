package ru.backtesting.port.base.ticker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.utils.doubles.TickerDeduplicator;

public class Ticker implements TickerInf, Comparable<TickerInf> {
	private String ticker;
			
	private String id;

	public static final String CASH_TICKER = "CASHx";
	public static final String CASH_EQUIVALENT_DATES_TICKER = "eem";
	
	public Ticker(String ticker) {
		super();
		this.ticker = ticker;
	}

	@Override
	public String getTicker() {
		return ticker;
	}

	@Override
	public String getTickerId() {
		if ( id == null )
			return ticker;
		else 
			return id;
	}

	@Override
	public void generateId() {
		if ( id == null )		
			this.id = TickerDeduplicator.instance().encode(ticker);
	}
	
	public static synchronized List<String> getTiskers(List<TickerInf> tickers) {
		List<String> list = new ArrayList<String>();
		
		for(TickerInf tickerInf : tickers)
			list.add(tickerInf.getTicker());
		
		return list;
	}
	
	public static synchronized List<String> getTickerIds(List<TickerInf> tickers) {
		List<String> list = new ArrayList<String>();
		
		for(TickerInf tickerInf : tickers)
			list.add(tickerInf.getTickerId());
		
		return list;
	}
	
	public static synchronized List<TickerInf> createTickersList(List<String> tickers) {
		List<TickerInf> list = new ArrayList<TickerInf>();
		
		for(String ticker : tickers)
			list.add(new Ticker(ticker));
		
		return list;
	}
	
	public static synchronized List<TickerInf> createTickersList(String ticker) {
		return Arrays.asList(new Ticker[] { new Ticker(ticker) });
	}
	
	@Override
	public String toString() {
		return getTickerId();
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof TickerInf  ) {
			TickerInf tickExc = (TickerInf) obj;
			
			String tickerActStr = getTickerId();
				
			String tickerExpStr = tickExc.getTickerId();

			return tickerActStr.equals(tickerExpStr);
		}
		
		return super.equals(obj);
	}

	public static AssetAllocation getAssetAllocForTickerInf(List<? extends AssetAllocation> assetsAlloc, TickerInf ticker) {
		for (AssetAllocation aa : assetsAlloc)
			if ( aa.getTickerInf().equals(ticker) )
				return aa;
						
		return null;
	}

	public static List<TickerInf> getTickersInfForAssetAlloc(List<? extends AssetAllocation> assetsAlloc) {
		List<TickerInf> tickers = new ArrayList<TickerInf>();
		
		for (AssetAllocation aa : assetsAlloc)
			tickers.add(aa.getTickerInf());
		
		return tickers;
	}

	@Override
	public int compareTo(TickerInf o) {
		return this.getTickerId().compareTo(o.getTickerId());
	}
	
	public static TickerInf cash() {
		return new Ticker(CASH_TICKER);
	}
}

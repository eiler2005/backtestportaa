package ru.backtesting.utils.doubles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ru.backtesting.port.base.AssetAllocation;

public class TickerDeduplicator {
	private static TickerDeduplicator helper;

	public static void main(String[] args) {
		String ticker = "tlt";

		String encodeTicker = TickerDeduplicator.instance().encode(ticker);

		System.out.println("The encrypted ticker is: \n" + encodeTicker);

		assertEquals("tlt", TickerDeduplicator.instance().decode(encodeTicker));
	}

	private TickerDeduplicator() {
	}

	public static TickerDeduplicator instance() {
		if (helper == null) {
			helper = new TickerDeduplicator();
		}

		return helper;
	}

	public boolean hasDuplicate(List<String> tickers) {
		Set<String> tickersSet = new HashSet<String>(tickers);

		if (tickersSet.size() < tickers.size())
			return true;
		
		return false;
	}
	
	public boolean hasDuplicateAlloc(List<? extends AssetAllocation> assetsAlloc) {
		List<String> tickersList = new ArrayList<String>();

		for (int i = 0; i < assetsAlloc.size(); i++) {
			String ticker = assetsAlloc.get(i).getTicker();

			tickersList.add(ticker.toLowerCase());
		}

		return hasDuplicate(tickersList);
	}

	public String encode(String ticker) {
		return ticker + "_crypt" + UUID.randomUUID().toString().substring(0, 3);
	}

	public List<String> decodeList(List<String> secretList) {
		List<String> values = new ArrayList<String>();
		
		for(String secret : secretList) {
			values.add(decode(secret));
		}
		
		return values;
	}

	
	public String decode(String secret) {
		if (secret.indexOf("_crypt") >= 0)
			return secret.substring(0, secret.indexOf("_crypt"));
		else
			return secret;
	}
}

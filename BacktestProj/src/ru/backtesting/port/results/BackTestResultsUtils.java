package ru.backtesting.port.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.momentum.DualMomUtils;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.utils.Logger;

public class BackTestResultsUtils {

	public static List<AssetAllocPerfInf> filterAssetAllocPerfInfByParams(List<String> tickers,
			List<AssetAllocPerfInf> allocPerfInfList) {
		List<AssetAllocPerfInf> list = new ArrayList<AssetAllocPerfInf>();

		Set<String> doublesTickers = new HashSet<String>();

		for (String ticker : tickers)
			for (AssetAllocPerfInf inf : allocPerfInfList)
				if (inf.getTicker().equals(ticker)) {

					// исключаем дубли - например abs mom perf growth spy и spy в аллокации активов
					// портфеля
					if (!doublesTickers.contains(inf.getTickerId().toLowerCase()))
						list.add(inf);

					doublesTickers.add(inf.getTickerId().toLowerCase());
				}

		return list;
	}

	public static List<AssetAllocPerfInf> generateAssetAllocForPort(List<AssetAllocPerfInf> curAllocPerfInfList, 
			List<PositionInformation> positions) {
		List<AssetAllocPerfInf> allocPortList = new ArrayList<AssetAllocPerfInf>();

		Logger.log().trace("(generateAssetAllocForPort) positions: " + positions);

		Logger.log().info("(generateAssetAllocForPort) curAllocPerfInfList: " + curAllocPerfInfList);

		for (PositionInformation pos : positions) {
			boolean isFind = false;

			for (AssetAllocPerfInf curPortAlloc : curAllocPerfInfList) {
				if (curPortAlloc.getTickerInf().equals(pos.getTickerInf()))
					if (!containsAssetAllocations(allocPortList, pos.getTickerInf())) {
						allocPortList.add(curPortAlloc);
						isFind = true;
					}
			}

			boolean isCashPos = pos.getTickerInf().equals(Ticker.cash());
			
			if (isFind == false && !isCashPos) {
				MomAssetAllocPerfInf aa = new MomAssetAllocPerfInf(
						pos.getTickerInf(), 
						null, null, 0, 0,
						DualMomUtils.NOT_AVAILABLE_QUOTE_PERF);
								
				allocPortList.add(aa);
			}
			
			if ( isCashPos) {
				MomAssetAllocPerfInf aa = new MomAssetAllocPerfInf(
						pos.getTickerInf(), 
						null, null, 0, 0,
						DualMomUtils.NOT_AVAILABLE_QUOTE_PERF);
								
				if ( pos.getPrice() != 0 ) 
					aa.setAllocPercent(100);
				
				allocPortList.add(aa);
			}
		}

		Logger.log().info("(generateAssetAllocForPort) allocPortList: " + allocPortList);

		return allocPortList;
	}

	private static boolean containsAssetAllocations(List<AssetAllocPerfInf> aaList, TickerInf ticker) {
		for (AssetAllocPerfInf aa : aaList)
			if (aa.getTickerInf().equals(ticker))
				return true;

		return false;
	}

}

package ru.backtesting.port;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import ru.backtesting.port.base.ticker.TickerInf;

public class PositionSet {	
	private LinkedHashMap<LocalDateTime, List<PositionInformation>> positions;
	
	public PositionSet() {
		positions = new LinkedHashMap<LocalDateTime, List<PositionInformation>>();
	}
	
	public void addNewPosition(TickerInf ticker, LocalDateTime date) {
		if ( !positions.containsKey(date) ) {
			List<PositionInformation> otherPositions = new ArrayList<PositionInformation>();
			otherPositions.add(new PositionInformation(ticker, date));

			positions.put(date, otherPositions);
		} else {
			List<PositionInformation> otherPositions = positions.get(date);
			
			if (  !PositionInformation.getTickers(otherPositions).contains(ticker) )
				otherPositions.add(new PositionInformation(ticker, date));
		}
		
		sort();
	}
	
	public void updatePositions(LocalDateTime date, List<PositionInformation> list) {
		if ( positions.containsKey(date) ) {
			List<PositionInformation> curDatePosList = positions.get(date);
						
			for(PositionInformation listPos : list)
				for(int i = 0; i < curDatePosList.size(); i++) {
					if ( curDatePosList.get(i).getTickerInf().getTickerId().equals(
							listPos.getTickerInf().getTickerId()) )
						curDatePosList.set(i, listPos);
				}
		}
		else 
			positions.put(date, list);
			//throw new IllegalArgumentException("На дату " + date + " в портфеле должны быть позиции. Проверьте код на ошибки!");
		
		sort();
	}
	
	public List<PositionInformation> getPositions(LocalDateTime date) {
		return positions.get(date);
	}
	
	public Set<LocalDateTime> getDates() {
		return positions.keySet();
	}
	
	private void sort() {
		Set<LocalDateTime> datesSet = positions.keySet();
		
		List<LocalDateTime> datesList = new ArrayList<LocalDateTime>(datesSet);
		
		Collections.sort(datesList);
		
		LinkedHashMap<LocalDateTime, List<PositionInformation>> sortPositions = new LinkedHashMap<LocalDateTime, List<PositionInformation>>();
		
		for(LocalDateTime date : datesList) {
			sortPositions.put(date, positions.get(date));
		}
		
		positions = sortPositions;
	}
	
	public void remove(LocalDateTime date) {
		positions.remove(date, positions.get(date));
	}
	
	public boolean containsTicker(TickerInf ticker) {
		boolean contains = false;
		
		for ( LocalDateTime date : positions.keySet() )
			for (PositionInformation pos : positions.get(date) ) {
				
				if ( pos.getTickerInf().equals(ticker) )
					return true;
		}
		
		return contains;
	}
	
	public List<PositionInformation> getSetOfUniquePositions() {
		List<PositionInformation> uniquePositions = new ArrayList<PositionInformation>();
		
		Set<String> uniqueTickers = new HashSet<String>();
		
		for(LocalDateTime date : positions.keySet() ) 
			for(PositionInformation pos : positions.get(date))
				if ( !uniqueTickers.contains(pos.getTickerInf().getTickerId()) ) {
					try {
						uniquePositions.add((PositionInformation) pos.clone());
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					
					uniqueTickers.add(pos.getTickerInf().getTickerId());
				}
		
		// uniquePositions.add(PositionInformation.cashPosition());
		
		return uniquePositions;
	}
}

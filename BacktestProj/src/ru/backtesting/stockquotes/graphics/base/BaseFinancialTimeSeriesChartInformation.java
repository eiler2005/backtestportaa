package ru.backtesting.stockquotes.graphics.base;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ru.backtesting.stockquotes.TradingPeriod;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.TypeUtils;

public abstract class BaseFinancialTimeSeriesChartInformation implements FinancialTimeSeriesChartInformation {
	protected String ticker;
	protected TradingPeriod period;
	
	protected List<String> tooltips;
	private String[] tooltipsArr;
	
	protected List<LocalDateTime> dates;
	private List<LocalDate> datesAsLocalDate;
	private LocalDate[] datesAsArr;

	protected List<Double> values;
	private double[] valuesArr;
	
	@Override
	public String getTicker() {
		return ticker;
	}

	@Override
	public TradingPeriod getTradingPeriod() {
		return period;
	}
	
	@Override
	public List<LocalDateTime> getDates() {
		return dates;
	}

	@Override
	public List<LocalDate> getDatesAsLocalDate() {
		if ( datesAsLocalDate == null )
			datesAsLocalDate = DateUtils.asLocalDate(dates);
		
		return datesAsLocalDate;
	}

	@Override
	public LocalDate[] getDatesAsLocalDateArr() {
		if ( datesAsArr == null )
			datesAsArr = DateUtils.asLocalDate(dates).toArray(new LocalDate[] {});
		
		return datesAsArr;
	}

	@Override
	public List<Double> getValues() {
		return values;
	}
	
	@Override
	public double[] getValuesAsDoubleArr() {
		if ( valuesArr == null )
			valuesArr = TypeUtils.toPrimitive(values);
		
		return valuesArr;
	}

	@Override
	public List<String> getTooltips() {
		return tooltips;
	}

	@Override
	public String[] getTooltipsArr() {
		if ( tooltipsArr == null )
			tooltipsArr = tooltips.toArray(new String[] {});
		
		return tooltipsArr;
	}
	
}

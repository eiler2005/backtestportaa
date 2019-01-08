package ru.backtesting.stockquotes.graphics.base;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FinancialTimeSeriesChartInformation {
	public String getTicker();

	public List<LocalDateTime> getDates();
	
	public List<LocalDate> getDatesAsLocalDate();
	
	public LocalDate[] getDatesAsLocalDateArr();

	public List<Double> getValues();
	
	public double[] getValuesAsDoubleArr();
	
	public double findValueByDate(LocalDateTime date);
	
	public List<String> getTooltips();
	
	public String[] getTooltipsArr();
}

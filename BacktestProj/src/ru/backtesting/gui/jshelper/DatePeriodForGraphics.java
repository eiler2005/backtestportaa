package ru.backtesting.gui.jshelper;

public final class DatePeriodForGraphics {
	private int startYear;
	private int endYear;
	
	public DatePeriodForGraphics(int startYear, int endYear) {
		super();
		this.startYear = startYear;
		this.endYear = endYear;
	}
	
	public int getStartYear() {
		return startYear;
	}
	public int getEndYear() {
		return endYear;
	}
}
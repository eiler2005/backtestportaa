package ru.backtesting.test;

import java.time.LocalDate;

import org.apache.commons.math3.random.GaussianRandomGenerator;

import ru.backtesting.utils.PortfolioUtils;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

// https://jtablesaw.github.io/tablesaw/userguide/toc

public class TablesawTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] values = {1, 2, 3, 7, 9.44242, 11};
		DoubleColumn column = DoubleColumn.create("my numbers", values);
		System.out.println(column.print());
		
		String[] animals = {"bear", "cat", "giraffe"};
		double[] cuteness = {90.1, 84.3, 99.7};

		Table cuteAnimals = Table.create("Cute Animals")
			.addColumns(
				StringColumn.create("Animal types", animals),
				DoubleColumn.create("rating", cuteness));
		
		System.out.println(cuteAnimals.structure());
		
		gagrTest();
	}
	
	public static void gagrTest() {
		LocalDate begDay = LocalDate.parse("2007-09-30");
		LocalDate lastDay = LocalDate.parse("2018-12-31");

		double gagr = PortfolioUtils.CAGRInPercent(10000, 24549, begDay, lastDay);
		
		System.out.println(gagr);
		
		begDay = LocalDate.parse("2013-06-01");
		lastDay = LocalDate.parse("2018-09-09");
		
		gagr = PortfolioUtils.CAGRInPercent(10000, 16897.14, begDay, lastDay);
			
		System.out.println(gagr);
	}
}

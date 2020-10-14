package ru.backtesting.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

public class Logger {
	final static org.apache.logging.log4j.Logger logger4j = LoggerContext.getContext().getLogger("PortfolioBacktest");

	public static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.00");;
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	public static final DateTimeFormatter DATE_FORMAT_SIMPLE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static final SimpleDateFormat DATE_FORMAT_FOR_LOG = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

	private static Logger instance;

	private PrintStream out;

	private Logger() {
		try {
			Properties props = System.getProperties();

			String log4ConfigFilePath = props.getProperty("user.dir") + "\\resources\\log4j.xml";

			System.out.println("Current log4j config file path is " + log4ConfigFilePath);

			ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4ConfigFilePath));
			XmlConfiguration xmlConfig = new XmlConfiguration(LoggerContext.getContext(), source);

			LoggerContext.getContext().setConfiguration(xmlConfig);

			LoggerContext.getContext().start();

			out = new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static synchronized Logger log() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}

	public String doubleAsString(double number) {
		return DOUBLE_FORMAT.format(number);
	}

	public String dateAsString(LocalDate date) {
		return DATE_FORMAT_SIMPLE.format(date);
	}

	public void info(String message) {
		logger4j.debug(message + addStackTrace());

		// logger4j.debug("[" +
		// DATE_FORMAT_FOR_LOG.format(Calendar.getInstance().getTime()) + "] " + info);
	}

	public void trace(String message) {
		logger4j.trace(message + addStackTrace());
	}

	public void error(String message) {
		logger4j.error(message + addStackTrace());
	}

	private String addStackTrace() {
		if (GlobalProperties.instance().traceWithMethodInvoke()) {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

			String str = "                 -->[";

			
			for (int i = stackTraceElements.length - 1; i > 1; i--) {
				StackTraceElement el = stackTraceElements[i];

				try {
					str += "[" + Class.forName(el.getClassName()).getSimpleName() + "->" + el.getMethodName() + "->"
							+ el.getLineNumber() + "] ";
				} catch (ClassNotFoundException e) {
					logger4j.error(e.getMessage());
				}
			}

			str += "]";
			
			return str;
		} else
			return "";
	}

	public static synchronized void setTableFormatter(Table table) {
		for (Column column : table.columns()) {
			if (column instanceof DateTimeColumn)
				((DateTimeColumn) column).setPrintFormatter(Logger.DATE_FORMAT_SIMPLE);
			else if (column instanceof DoubleColumn)
				((DoubleColumn) column).setPrintFormatter(Logger.DOUBLE_FORMAT, "_missing value_");
		}
	}
}

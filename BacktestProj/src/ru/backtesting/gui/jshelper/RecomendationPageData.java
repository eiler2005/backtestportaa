package ru.backtesting.gui.jshelper;

import java.io.File;
import java.io.IOException;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;

import ru.backtesting.gui.BacktestingAppGUIMain;
import ru.backtesting.test.TablesawTest;

public class RecomendationPageData {
	private Browser browser;
	
	public RecomendationPageData(Browser browser) {
		this.browser = browser;
	}
	
	public void save(String ticker, String period, String sma50, String sma200, 
			String wma50, String wma200, String rsi) {
        System.out.println("ticker = " + ticker);
        System.out.println("period = " + period);
        System.out.println("sma50 = " + sma50);
        System.out.println("sma200 = " + sma200);
        System.out.println("wma50 = " + wma50);
        System.out.println("wma200 = " + wma200);
        System.out.println("rsi = " + rsi);
        
        DOMDocument document = browser.getDocument();
        
        /* sample
        DOMNode root = document.findElement(By.id("active"));
        DOMNode textNode = document.createTextNode("Some text");
        DOMElement paragraph = document.createElement("p");
        paragraph.appendChild(textNode);
        root.appendChild(paragraph);
        */
        
        DOMElement imageEl = document.findElement(By.id("image"));
           
        String recomedFile = new File("WEB-INF" + File.separator + "samples/overlayPlot.html").toURI().toString();

        
        // imageEl.setInnerHTML(TablesawTest.timeSeriesPlot());
        
        try {
			imageEl.setInnerHTML(BacktestingAppGUIMain.readFile(recomedFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //imageEl.scrollToTop();

        browser.loadHTML(browser.getHTML());
    }
}

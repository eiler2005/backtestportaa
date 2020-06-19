package ru.backtesting.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.BrowserType;
import com.teamdev.jxbrowser.chromium.ContextMenuHandler;
import com.teamdev.jxbrowser.chromium.ContextMenuParams;
import com.teamdev.jxbrowser.chromium.InputEventsHandler;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.NetworkService;
import com.teamdev.jxbrowser.chromium.PluginInfo;
import com.teamdev.jxbrowser.chromium.PluginManager;
import com.teamdev.jxbrowser.chromium.ResourceHandler;
import com.teamdev.jxbrowser.chromium.ResourceParams;
import com.teamdev.jxbrowser.chromium.ResourceType;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FrameLoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import ru.backtesting.gui.jshelper.RecomendationPageJSHelper;
import ru.backtesting.port.MarketConstants;
import ru.backtesting.utils.Logger;

public class BacktestingAppGUIMain {

	private static final String JS_VAR_NAME_RECOMENDATION_PAGE_DATA = "recomendationPageData";
	private static final String WEB_CATALOGUE = "WEB-INF";
	private static final String MARKET_INF_PAGE_HTML = "templates" + File.separator + "marketInfPage.html";
	private static final String RECOMEND_PAGE_HTML = "templates" + File.separator + "recomendationPage.html";

	
	private JFrame frame;
	private Browser recomendPageBrowser, marketInfPageBrowser;
	private BrowserView recomendPageView, marketInfPageView;
	private boolean isMainViewMarket = true;
		
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// "CDE/Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
		// "Metal", "javax.swing.plaf.metal.MetalLookAndFeel"
		UIManager.installLookAndFeel("Metal", "javax.swing.plaf.metal.MetalLookAndFeel");
			
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BacktestingAppGUIMain window = new BacktestingAppGUIMain();
					window.frame.setVisible(true);
					window.frame.setLocationRelativeTo(null);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public BacktestingAppGUIMain() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Backtesting Investing Portfolio Tool Application\n");
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		//frame.setBounds(100, 100, 1070, 888);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.WHITE);
		frame.getContentPane().setBackground(Color.WHITE); 
		frame.getContentPane().getParent().setBackground(Color.WHITE); 

		
		BorderLayout borderLayout = new BorderLayout();
		frame.getContentPane().setLayout(borderLayout);

		
		JButton recomSystemButton = new JButton("Рекомендательная система");
		recomSystemButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Logger.log().info("Нажата кнопка \"recomSystemButton\"");
				
				if ( recomendPageBrowser == null && recomendPageView == null )
					try {
						recomendPageBrowser = initializationBaseBrowser();
						recomendPageView = createRecomendPageBrowserView(recomendPageBrowser);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
				
				if ( isMainViewMarket ) {
					frame.getContentPane().remove(marketInfPageView);
					frame.getContentPane().add(recomendPageView, BorderLayout.CENTER);
					isMainViewMarket = false;
				}
				
				// recomendPageBrowser.reload();
				recomendPageView.revalidate();
			}
		});
		
		JButton backtestPortButton = new JButton("Тестирование портфеля");
		backtestPortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Logger.log().info("Нажата кнопка \"backtestPortButton\"");

			}
		});
		
		JButton infromationPortButton = new JButton("Интересная информация о инвестировании");
		infromationPortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Logger.log().info("Нажата кнопка \"infromationPortButton\"");
				
				if ( recomendPageView != null )
					frame.getContentPane().remove(recomendPageView);
								
				frame.getContentPane().add(marketInfPageView, BorderLayout.CENTER);			
				
				isMainViewMarket = true;
				
				// marketInfPageBrowser.reload();
				marketInfPageView.revalidate();
			}
		});
		
		JPanel panel = new JPanel();
		
		JSeparator separator = new JSeparator();
		
		GroupLayout groupLayout = new GroupLayout(panel);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(446)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(infromationPortButton)
					.addGap(10)
					.addComponent(backtestPortButton)
					.addGap(70)
					.addComponent(recomSystemButton, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(219, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(recomSystemButton)
							.addComponent(backtestPortButton)
							.addComponent(infromationPortButton)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		
		panel.setBackground(Color.WHITE);
		
        panel.setBorder(new EmptyBorder(1, 1, 1, 1));
		
		frame.getContentPane().add(panel, BorderLayout.BEFORE_FIRST_LINE);
		
		try {
			marketInfPageBrowser = initializationBaseBrowser();
			
			marketInfPageView = createMarketInfPageBrowserView(marketInfPageBrowser);
			
			frame.getContentPane().add(marketInfPageView, BorderLayout.CENTER);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public Browser initializationBaseBrowser() throws IOException {    	
		Browser browser = new Browser(BrowserType.LIGHTWEIGHT);
    			
		// Gets the current Browser's preferences
		BrowserPreferences preferences = browser.getPreferences();
		preferences.setImagesEnabled(true);
		preferences.setJavaScriptEnabled(true);
		preferences.setAllowDisplayingInsecureContent(true);
		preferences.setAllowRunningInsecureContent(true);
		preferences.setJavaScriptCanAccessClipboard(true);
		
		browser.setPreferences(preferences);
		
		browser.addLoadListener(new LoadAdapter() {
			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent event) {
				// TODO Auto-generated method stub
				super.onDocumentLoadedInFrame(event);
				
			}
			
            @Override
            public void onFinishLoadingFrame(FinishLoadingEvent event) {
                if (event.isMainFrame()) {
                    //System.out.println("HTML = " + event.getBrowser().getHTML());
                	
    	            buildRecomendPagePage(event.getBrowser());
                }
            }
        });
		
		PluginManager pluginManager = browser.getPluginManager();
		List<PluginInfo> pluginsList = pluginManager.getPluginsInfo();
		for (PluginInfo plugin : pluginsList) {
		    System.out.println("Plugin Name: " + plugin.getName());
		}
		
		browser.addScriptContextListener(new ScriptContextAdapter() {
		    @Override
		    public void onScriptContextCreated(ScriptContextEvent event) {
		        Browser browser = event.getBrowser();
		        JSValue window = browser.executeJavaScriptAndReturnValue("window");
		        
                JSValue value = window.asObject().getProperty(JS_VAR_NAME_RECOMENDATION_PAGE_DATA);
                
		        Logger.log().info("Результат выполнения скрипта: " + value);			    
		    }
		});

		RecomendationPageJSHelper jsHelperObject = new RecomendationPageJSHelper(browser);
		
		browser.addLoadListener(new LoadAdapter() {
	            @Override
	            public void onFinishLoadingFrame(FinishLoadingEvent event) {
	                if (event.isMainFrame()) {
	                	Browser browser = event.getBrowser();
	                    JSValue value = browser.executeJavaScriptAndReturnValue("window");
        
	                    value.asObject().setProperty(JS_VAR_NAME_RECOMENDATION_PAGE_DATA, 
	                    		jsHelperObject);
	                    
	                    Logger.log().info("Загрузили в страницу \"" + event.getBrowser().getURL() + "\" переменную \"" + 
	                    		JS_VAR_NAME_RECOMENDATION_PAGE_DATA + "\"");
	                }	                
	            }
	    });
		
		NetworkService networkService = browser.getContext().getNetworkService();
		networkService.setResourceHandler(new ResourceHandler() {
		    @Override
		    public boolean canLoadResource(ResourceParams params) {
                // System.out.println("URL: " + params.getURL());
                // System.out.println("Type: " + params.getResourceType());
                
		        boolean isNotAnImageType = 
		                params.getResourceType() != ResourceType.IMAGE;
		        if (isNotAnImageType) {
		            return true;    
		        }

		        // loading of all images
		        return true;
		    }
		});
		
		return browser;
	}
	
	public BrowserView createMarketInfPageBrowserView(Browser browser) throws IOException {
		BrowserView view = new BrowserView(browser);
	        
		view.setKeyEventsHandler(new InputEventsHandler<KeyEvent>() {
	            public boolean handle(KeyEvent event) {
	        		Logger.log().info("handle key event: getKeyCode() - " + event.getKeyCode() + ", isAltDown() - " + event.isAltDown());
	            	
	            	if ( event.isAltDown() && event.getKeyCode() == KeyEvent.VK_LEFT ) {
	            		browser.goBack();
	            		
	            		browser.reload();
	            		return true;
	            	}
	            	
	            	if ( event.isAltDown() && event.getKeyCode() == KeyEvent.VK_RIGHT ) {
	            		browser.goForward();
	            		browser.reload();
	            		return true;
	            	}
	            	
	            	return false;
	            }
	        });
		
		browser.setContextMenuHandler(new MyContextMenuHandler(view));

		File webDir = new File(Paths.get(".").toFile().getCanonicalPath() + File.separator + WEB_CATALOGUE);

		String webPageFile = new File(webDir + File.separator + MARKET_INF_PAGE_HTML).toURI().toString();

		// browser.loadHTML(readFile(recomedFile));

		// System.out.println(readFile(recomedFile));

		Logger.log().info("Загружаем страницу в браузере: " + webPageFile);
		
		browser.loadURL(webPageFile);

		view.setBackground(Color.WHITE);

		return view;
	}
	
	public BrowserView createRecomendPageBrowserView(Browser browser) throws IOException {
		BrowserView view = new BrowserView(browser);
        
		browser.setContextMenuHandler(new MyContextMenuHandler(view));

		File webDir = new File(Paths.get(".").toFile().getCanonicalPath() + File.separator + WEB_CATALOGUE);

		String webPageFile = new File(webDir + File.separator + RECOMEND_PAGE_HTML).toURI().toString();

		// browser.loadHTML(readFile(recomedFile));

		// System.out.println(readFile(recomedFile));

		Logger.log().info("Загружаем страницу в браузере: " + webPageFile);
		
		browser.loadURL(webPageFile);
		
		view.setBackground(Color.WHITE);

		return view;
	}
	
	private void buildRecomendPagePage(Browser browser) {
		DOMDocument document = browser.getDocument();

		DOMElement spyRowEl = document.findElement(By.id("spyRow"));
		
		String newTicker = MarketConstants.BASE_USA_LONG_TERM_BOND_TICKER;
		String newTickerDivId = newTicker + "Row";
		
		String jsElId = "documentJS";
		
		if ( spyRowEl != null && document.findElement(By.id(newTickerDivId)) == null ) {
			String spyRowELText = spyRowEl.getInnerHTML();
			
			// replace #ticker# to "spy"
			spyRowEl.setInnerHTML(spyRowELText.replaceAll("#ticker#", MarketConstants.BASE_USA_STOCK_INDEX_TICKER));
			
			DOMElement newTickerDivEl = document.createElement("div");
			
			newTickerDivEl.setInnerHTML(spyRowELText.replaceAll("#ticker#", newTicker));
			newTickerDivEl.setAttribute("class", "row");
			newTickerDivEl.setAttribute("id", newTickerDivId);

			
			Logger.log().info("Добавляем к странице элемент с id \"" + newTickerDivId + "\"");
			Logger.log().info("Текст ниже:" + newTickerDivEl.getInnerHTML());

			DOMElement containerEL = document.findElement(By.id("bootstrapContainer"));
			DOMElement hrEL = document.findElement(By.id("hrId"));
			containerEL.insertChild(newTickerDivEl, hrEL);
			
			DOMElement tickerHeaderEl = document.findElement(By.id(newTicker + "Header"));
			tickerHeaderEl.setTextContent("Данные по активу " + newTicker);
			
			DOMElement tickerCalcButtonEl = document.findElement(By.id(newTicker + "Button"));
			tickerCalcButtonEl.setTextContent("Рассчитать данные по " + newTicker);
			
			DOMElement documentJSEl = document.findElement(By.id(jsElId));

			Logger.log().info("Получили элемент с id \"" + jsElId + "\"");

			
			Logger.log().info("Текст элемента с id \"" + jsElId + "\"" + documentJSEl.getTextContent());
		}
	}
	
	@Deprecated
	public static String readFile(String path) throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(URI.create(path)));
		return new String(encoded, Charset.forName("UTF-8"));
	}
	
	private static class MyContextMenuHandler implements ContextMenuHandler {

        private final JComponent component;

        private MyContextMenuHandler(JComponent parentComponent) {
            this.component = parentComponent;
        }

        public void showContextMenu(final ContextMenuParams params) {
            final JPopupMenu popupMenu = new JPopupMenu();
            if (!params.getLinkText().isEmpty()) {
                popupMenu.add(createMenuItem("Open link in new window", new Runnable() {
                    public void run() {
                        String linkURL = params.getLinkURL();
                        System.out.println("linkURL = " + linkURL);
                        
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            try {
								Desktop.getDesktop().browse(new URI(linkURL));
							} catch (IOException e) {
								e.printStackTrace();
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
                        }
                    }
                }));
            }

            final Browser browser = params.getBrowser();
            popupMenu.add(createMenuItem("Reload", new Runnable() {
                public void run() {
                    browser.reload();
                }
            }));
            

            final Point location = params.getLocation();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    popupMenu.show(component, location.x, location.y);
                }
            });
        }

        private static JMenuItem createMenuItem(String title, final Runnable action) {
            JMenuItem reloadMenuItem = new JMenuItem(title);
            reloadMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
            return reloadMenuItem;
        }
    }
}

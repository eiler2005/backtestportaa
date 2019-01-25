package ru.backtesting.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.NetworkService;
import com.teamdev.jxbrowser.chromium.PluginInfo;
import com.teamdev.jxbrowser.chromium.PluginManager;
import com.teamdev.jxbrowser.chromium.ResourceHandler;
import com.teamdev.jxbrowser.chromium.ResourceParams;
import com.teamdev.jxbrowser.chromium.ResourceType;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import ru.backtesting.gui.jshelper.RecomendationPageData;

public class BacktestingAppGUIMain {

	private static final String JS_VAR_NAME_RECOMENDATION_PAGE_DATA = "marketInformationPageData";
	private static final String WEB_CATALOGUE = "WEB-INF";
	private static final String RECOMENDATION_PAGE_HTML = "templates" + File.separator + "marketInfPage.html";
	private JFrame frame;

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
		frame.setBounds(100, 100, 1070, 888);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.WHITE);
		frame.getContentPane().setBackground(Color.WHITE); 
		frame.getContentPane().getParent().setBackground(Color.WHITE); 

		
		BorderLayout borderLayout = new BorderLayout();
		frame.getContentPane().setLayout(borderLayout);

		
		JButton recomSystemButton = new JButton("Рекомендательная система");
		recomSystemButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// to do
			}
		});
		
		JButton backtestPortButton = new JButton("Тестирование портфеля");
		backtestPortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// to do
			}
		});
		
		JButton infromationPortButton = new JButton("Интересная информация о инвестировании");
		infromationPortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
					.addComponent(recomSystemButton)
					.addGap(10)
					.addComponent(backtestPortButton)
					.addGap(70)
					.addComponent(infromationPortButton, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
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
			addBrowser();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public void addBrowser() throws IOException {
    	File tempDir = new File(Paths.get(".").toFile().getCanonicalPath() + File.separator + WEB_CATALOGUE);
    	
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
            public void onFinishLoadingFrame(FinishLoadingEvent event) {
                if (event.isMainFrame()) {
                    //System.out.println("HTML = " + event.getBrowser().getHTML());
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
                if (value.isJavaObject()) {
                	RecomendationPageData object = (RecomendationPageData) value.asJavaObject();
                }
		    }
		});

		
		browser.addLoadListener(new LoadAdapter() {
	            @Override
	            public void onFinishLoadingFrame(FinishLoadingEvent event) {
	                if (event.isMainFrame()) {
	                	Browser browser = event.getBrowser();
	                    JSValue value = browser.executeJavaScriptAndReturnValue("window");
        
	                    value.asObject().setProperty(JS_VAR_NAME_RECOMENDATION_PAGE_DATA, new RecomendationPageData(event.getBrowser()));
	                }
	            }
	    });
		
		NetworkService networkService = browser.getContext().getNetworkService();
		networkService.setResourceHandler(new ResourceHandler() {
		    @Override
		    public boolean canLoadResource(ResourceParams params) {
                System.out.println("URL: " + params.getURL());
                System.out.println("Type: " + params.getResourceType());
                
		        boolean isNotAnImageType = 
		                params.getResourceType() != ResourceType.IMAGE;
		        if (isNotAnImageType) {
		            return true;    
		        }

		        // loading of all images
		        return true;
		    }
		});
		
        BrowserView view = new BrowserView(browser);
        		        
        browser.setContextMenuHandler(new MyContextMenuHandler(view));

        
        String recomedFile = new File(tempDir + File.separator + RECOMENDATION_PAGE_HTML).toURI().toString();
        
        // browser.loadHTML(readFile(recomedFile));
        
        
        System.out.println(readFile(recomedFile));
        
        browser.loadURL(recomedFile);

		view.setBackground(Color.WHITE);
				
		frame.getContentPane().add(view, BorderLayout.CENTER);
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
	
	String str = "";
	String html = "<!DOCTYPE html>\n" + 
    		"<html lang=\"en\">\n" + 
    		"<head>\n" + 
    		"	<title>Table V03</title>\n" + 
    		"	<meta charset=\"UTF-8\">\n" + 
    		"	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
    		"<!--===============================================================================================-->	\n" + 
    		"	<link rel=\"icon\" type=\"image/png\" href=\"" + str + "/images/icons/favicon.ico\"/>\n" + 
    		"<!--===============================================================================================-->\n" + 
    		"	<link rel=\"stylesheet\" type=\"text/css\" href=\""  + str + "/vendor/bootstrap/css/bootstrap.min.css\">\n" + 
    		"<!--===============================================================================================-->\n" + 
    		"	<link rel=\"stylesheet\" type=\"text/css\" href=\""  + str + "/fonts/font-awesome-4.7.0/css/font-awesome.min.css\">\n" + 
    		"<!--===============================================================================================-->\n" + 
    		"	<link rel=\"stylesheet\" type=\"text/css\" href=\""  + str + "/vendor/animate/animate.css\">\n" + 
    		"<!--===============================================================================================-->\n" + 
    		"	<link rel=\"stylesheet\" type=\"text/css\" href=\""  + str + "/vendor/select2/select2.min.css\">\n" + 
    		"<!--===============================================================================================-->\n" + 
    		"	<link rel=\"stylesheet\" type=\"text/css\" href=\""  + str + "/vendor/perfect-scrollbar/perfect-scrollbar.css\">\n" + 
    		"<!--===============================================================================================-->\n" + 
    		"	<link rel=\"stylesheet\" type=\"text/css\" href=\""  + str + "/css/util.css\">\n" + 
    		"	<link rel=\"stylesheet\" type=\"text/css\" href=\""  + str + "/css/main.css\">\n" + 
    		"<!--===============================================================================================-->" +
    		"</head>\n" + 
    		"<body>\n" + 
    		"	\n" + 
    		"	<div class=\"limiter\">\n" + 
    		"		<div class=\"container-table100\">\n" + 
    		"			<div class=\"wrap-table100\">" +
    		"<div class=\"table100 ver1 m-b-110\">\n" + 
    		"					<table data-vertable=\"ver1\">\n" + 
    		"						<thead>\n" + 
    		"							<tr class=\"row100 head\">\n" + 
    		"								<th class=\"column100 column1\" data-column=\"column1\"></th>\n" + 
    		"								<th class=\"column100 column2\" data-column=\"column2\">Sunday</th>\n" + 
    		"								<th class=\"column100 column3\" data-column=\"column3\">Monday</th>\n" + 
    		"								<th class=\"column100 column4\" data-column=\"column4\">Tuesday</th>\n" + 
    		"								<th class=\"column100 column5\" data-column=\"column5\">Wednesday</th>\n" + 
    		"								<th class=\"column100 column6\" data-column=\"column6\">Thursday</th>\n" + 
    		"								<th class=\"column100 column7\" data-column=\"column7\">Friday</th>\n" + 
    		"								<th class=\"column100 column8\" data-column=\"column8\">Saturday</th>\n" + 
    		"							</tr>\n" + 
    		"						</thead>\n" + 
    		"						<tbody>\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Lawrence Scott</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">8:00 AM</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">--</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">--</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">8:00 AM</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">--</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">8:00 AM</td>\n" + 
    		"							</tr>\n" + 
    		"\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Jane Medina</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">--</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">--</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">9:00 AM</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">--</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">--</td>\n" + 
    		"							</tr>\n" + 
    		"\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Billy Mitchell</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">9:00 AM</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">--</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">--</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">--</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">--</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">2:00 PM</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">8:00 AM</td>\n" + 
    		"							</tr>\n" + 
    		"\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Beverly Reid</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">--</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">--</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">9:00 AM</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">--</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">--</td>\n" + 
    		"							</tr>\n" + 
    		"\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Tiffany Wade</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">8:00 AM</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">--</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">--</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">8:00 AM</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">--</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">8:00 AM</td>\n" + 
    		"							</tr>\n" + 
    		"\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Sean Adams</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">--</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">--</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">9:00 AM</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">--</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">--</td>\n" + 
    		"							</tr>\n" + 
    		"\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Rachel Simpson</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">9:00 AM</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">--</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">--</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">--</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">--</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">2:00 PM</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">8:00 AM</td>\n" + 
    		"							</tr>\n" + 
    		"\n" + 
    		"							<tr class=\"row100\">\n" + 
    		"								<td class=\"column100 column1\" data-column=\"column1\">Mark Salazar</td>\n" + 
    		"								<td class=\"column100 column2\" data-column=\"column2\">8:00 AM</td>\n" + 
    		"								<td class=\"column100 column3\" data-column=\"column3\">--</td>\n" + 
    		"								<td class=\"column100 column4\" data-column=\"column4\">--</td>\n" + 
    		"								<td class=\"column100 column5\" data-column=\"column5\">8:00 AM</td>\n" + 
    		"								<td class=\"column100 column6\" data-column=\"column6\">--</td>\n" + 
    		"								<td class=\"column100 column7\" data-column=\"column7\">5:00 PM</td>\n" + 
    		"								<td class=\"column100 column8\" data-column=\"column8\">8:00 AM</td>\n" + 
    		"							</tr>\n" + 
    		"						</tbody>\n" + 
    		"					</table>\n" + 
    		"				</div>" +
    		"	<script src=\""  + str + "/js/main.js\"></script>\n" + 
    		"\n" + 
    		"</body>\n" + 
    		"</html>";
}

package ru.backtesting.test;

import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class JFrameHTMLTest extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextPane logPane;
    private HTMLDocument logDoc;
    private HTMLEditorKit logKit;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                	JFrameHTMLTest test = new JFrameHTMLTest();
                    test.setVisible(true);

                    test.log("<span>Hello 1</span><br/>"); //Test 1
                    test.log("<span>Hello 2</span><br/>"); //Test 2
                    test.log("<span>Hello 3</span><br/>"); //Test 3

                    test.log("<span>Hello </span>");       //Test 4
                    test.log("<span>world!</span>");       //Test 5

                    test.printHTML();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Log some data...
     * @param str
     */
    public void log(String s){
        s = s.replaceAll("\n", "<br/>");
        try {
            logKit.insertHTML(logDoc, logDoc.getLength(), s, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void printHTML(){
        try{
            StringWriter writer = new StringWriter();
            logKit.write(writer, logDoc, 0, logDoc.getLength());
            String s = writer.toString();
            System.out.println(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clearLog(){
        try {
            logPane.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the frame.
     */
    public JFrameHTMLTest() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
            gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .addContainerGap())
        );
        gl_contentPane.setVerticalGroup(
            gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addContainerGap())
        );

        logPane = new JTextPane();
        logPane.setContentType("text/html");
        logPane.setEditable(false);
        scrollPane.setViewportView(logPane);
        contentPane.setLayout(gl_contentPane);

        DefaultCaret caret = (DefaultCaret)logPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        logDoc = (HTMLDocument) logPane.getDocument();
        logKit = (HTMLEditorKit) logPane.getEditorKit();
        /*Font font = new Font("Consolas", Font.PLAIN, 14);
        String bodyRule = String.format("body{font-family:%s;font-size:%spt", 
                font.getFamily(), font.getSize());
        StyleSheet style = logDoc.getStyleSheet();
        style.addRule(bodyRule);*/
    }
    
    public static void showHtml(String text, File file) {
		JEditorPane jep = new JEditorPane();
	     jep.setEditable(false);   
	       jep.setContentType("text/html");
	       jep.setText(text);

	     
	     try {
	       jep.setPage(file.toURL());
	     }
	     catch (IOException e) {
	       jep.setContentType("text/html");
	       jep.setText("<html>Could not load http://www.oreilly.com </html>");
	     }
	      
	     JScrollPane scrollPane = new JScrollPane(jep);     
	     JFrame f = new JFrame("O'Reilly & Associates");
	     // Next line requires Java 1.3
	     f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	     f.getContentPane().add(scrollPane);
	     f.setSize(512, 342);
	     f.show();
	}
}
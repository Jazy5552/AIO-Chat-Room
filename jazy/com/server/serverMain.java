package jazy.com.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import jazy.com.gui.mainClass;

//TODO add a poll service
//TODO add a restart button

public class serverMain extends JFrame{
	private static final long serialVersionUID = 1L;
	static int port;
	static int maxConnections;
	static clientHandler cl;
	boolean movingOn = false;
	
	static JTextPane logArea = new JTextPane(mainClass.doc);
	JPanel ioPanel = new JPanel();
	static public JLabel status = new JLabel("Waiting for Connections");
	JPanel buttonPanel = new JPanel();
	JButton restartB = new JButton("Restart");
	JButton clearB = new JButton("Clear");
	JButton infoB = new JButton("Info");
	JTextArea inputArea = new JTextArea(3,0);
	static StyledDocument d = logArea.getStyledDocument();
	boolean inInfo = false;
	
	public serverMain(int port, int max){
		serverMain.port = port;
		status.setText("Waiting:"+port);
		maxConnections = max;
		displayGUI();
		try{
		cl = new clientHandler(new ServerSocket(port));
		}catch(Exception e){
			System.out.println("Error:"+e.getMessage());
			restart("Error binding to port!"); //Restarts with this string in the inital start up
		}
		log("Listening for clients...");
	}
	
	public static void main(String args[]){
		mainClass.makeDoc();
		new serverMain(5502,10);
	}
	
	public void displayGUI(){ //Draw that gui!
		mainClass.makeDoc();
		setTitle("Chatroom Server");
		setLayout(new FlowLayout());
		
		BoxLayout lay = new BoxLayout(ioPanel, BoxLayout.Y_AXIS);
		ioPanel.setLayout(lay);
		BorderLayout lay2 = new BorderLayout();
		lay2.setHgap(30);
		lay2.setVgap(30);
		buttonPanel.setLayout(lay2);
		
		ioPanel.add(status);
		
		logArea.setEditable(false);
		d = logArea.getStyledDocument();
		try{d.insertString(0, "Log Area:\n", d.getStyle("red"));}catch(Exception ee){}
		JScrollPane jp = new JScrollPane(logArea);
		jp.setPreferredSize(new Dimension(400,150));
		jp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		ioPanel.add(jp);
		
		inputArea.setLineWrap(true);
		inputArea.setBackground(Color.darkGray);
		inputArea.setForeground(Color.green);
		JScrollPane jjp = new JScrollPane(inputArea);
		jjp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jjp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		inputArea.addKeyListener(new myKeyListener());
		ioPanel.add(jjp);
		
		restartB.setActionCommand("restart");
		clearB.setActionCommand("clear");
		infoB.setActionCommand("info");
		restartB.addActionListener(new myActionListener());
		clearB.addActionListener(new myActionListener());
		infoB.addActionListener(new myActionListener());
		JPanel em = new JPanel();
		em.setLayout(new BoxLayout(em,BoxLayout.Y_AXIS));
		em.add(restartB);
		em.add(clearB);
		//em.Add send button for input
		buttonPanel.add(infoB,BorderLayout.NORTH);
		buttonPanel.add(em,BorderLayout.SOUTH);
		
		add(ioPanel);
		add(buttonPanel);
		
		setVisible(true);
		addWindowListener(new myWindowListener());
		setLocationRelativeTo(null);
		pack();
	}
	
	static public synchronized void log(String mes){
		try{
		String time;
		if (mes.contains("[")&&mes.contains("]:")){
			time = mes.substring(0,mes.indexOf("]:")+2);
		}else{
			time = mainClass.getTimeStamp("");
		}
		try{
		d.insertString(d.getLength(), time, d.getStyle("bold"));
		d.insertString(d.getLength(), mes.replace(time, "")+"\n", d.getStyle("regular"));
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		logArea.setCaretPosition(d.getLength());
		}catch(Exception ee){}
	}
	
	private void restart(final String newText){
		movingOn = true;
		cl.disconnectAll();
		removeAll();
		dispose();
		new Thread(new Runnable(){
			public void run(){
				mainClass m = new mainClass();
				m.status.setText(m.status.getText()+"\n"+newText);
			}
		}).start();
	}
	
	private class myActionListener implements ActionListener{
		String doc;
		Color o;

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("restart")){ if (inInfo) return;
				restart("");
			}else if (e.getActionCommand().equals("clear")){ if (inInfo) return;
				logArea.setText("");
				try{
				d.insertString(0, "Log Area:\n", d.getStyle("green"));
				}catch(Exception ee){}
			}else if (e.getActionCommand().equals("info")){
				if (!inInfo){ //Display info
					doc = logArea.getText();
					BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/info.txt")));
					logArea.setText("");
					o = logArea.getBackground();
					logArea.setBackground(Color.cyan);
					try{
					d.insertString(d.getLength(), "Info:\n", d.getStyle("bold"));
					String inP;
					while((inP = br.readLine())!=null){
						d.insertString(d.getLength(), inP, d.getStyle("regular"));
						logArea.setText(logArea.getText()+"\n");
					}
					d.insertString(d.getLength(), "(Hit the info button to exit)", d.getStyle("red"));
					}catch(Exception ee){}finally{try{br.close();}catch(Exception asd){}}
					logArea.setCaretPosition(0);
					inInfo = true;
				}else{ //Return logArea
					logArea.setText(doc);
					logArea.setBackground(o);
					inInfo = false;
				}
				
			}
		}
		
	}
	
	private class myWindowListener implements WindowListener{
		public void windowActivated(WindowEvent e) {
		}
		public void windowClosed(WindowEvent e) { if(movingOn) return;
			System.exit(0);
		}
		public void windowClosing(WindowEvent e) { if(movingOn) return;
			cl.disconnectAll();
			dispose();
		}
		public void windowDeactivated(WindowEvent e) {
		}

		public void windowDeiconified(WindowEvent e) {
		}

		public void windowIconified(WindowEvent e) {
		}

		public void windowOpened(WindowEvent e) {
		}
	}
	
	private class myKeyListener implements KeyListener{
		public void keyPressed(KeyEvent e) {
		}
		public void keyReleased(KeyEvent e) { if (inInfo) return;
			if (e.getKeyCode() == KeyEvent.VK_ENTER){
				if (e.isShiftDown()){
					inputArea.setText(inputArea.getText() + "\n");
				}else{
					String sende = inputArea.getText();
					cl.sendMes(mainClass.getTimeStamp("Server")+sende.substring(0,sende.lastIndexOf("\n")));
					inputArea.setText("");
				}
			}
		}
		public void keyTyped(KeyEvent e) {
		}
	}
}

package jazy.com.client;

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

public class clientMain extends JFrame{
	private static final long serialVersionUID = 1L;
	final String hostName;
	final int port;
	static JLabel status = new JLabel("Starting up...");
	static JTextPane logArea = new JTextPane();
	static JTextArea inputArea = new JTextArea(3,0);
	static StyledDocument d = logArea.getStyledDocument();
	static clientStreams cs;
	JButton infoB = new JButton("Info");
	JButton sendB = new JButton("Send");
	JButton clearB = new JButton("Clear");
	JButton restartB = new JButton("Restart");
	boolean movingOn = false;
	boolean inInfo = false;
	
	public clientMain(String hostname, int port){
		this.hostName = hostname;
		this.port = port;
		setLayout(new FlowLayout());
		setTitle("Chat Room Client");
		
		JPanel buttonPanel = new JPanel();
		JPanel ioPanel = new JPanel();
		BorderLayout lay2 = new BorderLayout();
		lay2.setHgap(10);
		lay2.setVgap(10);
		buttonPanel.setLayout(lay2);
		BoxLayout lay1 = new BoxLayout(ioPanel,BoxLayout.Y_AXIS);
		ioPanel.setLayout(lay1);
		
		ioPanel.add(status);
		
		logArea.setText("");
		logArea.setEditable(false);
		d = mainClass.doc;
		logArea.setStyledDocument(d);
		try{d.insertString(0, "Log Area:\n", d.getStyle("green"));}catch(Exception e){System.out.println(e.getMessage());}
		JScrollPane jp2 = new JScrollPane(logArea);
		jp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jp2.setPreferredSize(new Dimension(400,150));
		ioPanel.add(jp2);
		
		sendB.setActionCommand("send");
		sendB.addActionListener(new myActionListener());
		ioPanel.add(sendB);
		
		inputArea.setText("Input Area");
		inputArea.setLineWrap(true);
		inputArea.setBackground(Color.green);
		inputArea.addKeyListener(new myKeyListener());
		JScrollPane jp1 = new JScrollPane(inputArea);
		jp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		ioPanel.add(jp1);
		
		add(ioPanel);
		
		infoB.setActionCommand("info");
		infoB.addActionListener(new myActionListener());
		buttonPanel.add(infoB,BorderLayout.NORTH);
		
		clearB.setActionCommand("clear");
		clearB.addActionListener(new myActionListener());
		restartB.setActionCommand("restart");
		restartB.addActionListener(new myActionListener());
		JPanel tmp = new JPanel();
		BoxLayout lay3 = new BoxLayout(tmp,BoxLayout.Y_AXIS);
		tmp.setLayout(lay3);
		tmp.add(clearB);
		tmp.add(restartB);
		buttonPanel.add(tmp,BorderLayout.SOUTH);
		
		add(buttonPanel);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		cs = new clientStreams(hostName,port);
		addWindowListener(new myWindowListener());
	}
	
	public static void main(String args[]){
		mainClass.makeDoc();
		new clientMain("localhost",5502);
	}
	
	static public synchronized void log(String mes,String style){
		try{
		String time;
		if (mes.contains("[")&&mes.contains("]:")){
			time = mes.substring(0,mes.indexOf("]:")+2);
		}else{
			time = mainClass.getTimeStamp("");
		}
		try{
		d.insertString(d.getLength(), time, d.getStyle("bold"));
		d.insertString(d.getLength(), mes.replace(time, "")+"\n", d.getStyle(style));
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		logArea.setCaretPosition(d.getLength());
		}catch(Exception ee){}
	}
	
	private class myActionListener implements ActionListener{
		String doc;
		Color o;

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("restart")){ if (inInfo) return;
				restart("");
			}else if (e.getActionCommand().equals("clear")){ if (inInfo) return;
				logArea.setText("");
				inputArea.setText("");
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
				
			}else if (e.getActionCommand().equals("send")){ if (inInfo) return;
				String sende = inputArea.getText();
				inputArea.setText("");
				cs.send(sende);
			}
		}
	}
	
	private void restart(final String newText){
		movingOn = true;
		cs.disconnect();
		removeAll();
		dispose();
		new Thread(new Runnable(){
			public void run(){
				mainClass m = new mainClass();
				m.status.setText(m.status.getText()+"\n"+newText);
			}
		}).start();
	}
	
	private class myWindowListener implements WindowListener{
		public void windowActivated(WindowEvent e) {
		}
		public void windowClosed(WindowEvent e) { if(movingOn) return;
			System.exit(0);
		}
		public void windowClosing(WindowEvent e) { if(movingOn) return;
			cs.disconnect();
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
		public void keyPressed(KeyEvent arg0) {
		}
		public void keyReleased(KeyEvent arg0) { if (inInfo) return;
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER){
				if (arg0.isShiftDown()){
					inputArea.setText(inputArea.getText()+"\n");
				}else{
					String sende = inputArea.getText();
					inputArea.setText("");
					sende = sende.substring(0,sende.lastIndexOf("\n"));
					cs.send(sende);
				}
			}
		}
		public void keyTyped(KeyEvent arg0) {
		}
		
	}
}

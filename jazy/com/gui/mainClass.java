package jazy.com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import jazy.com.client.clientMain;
import jazy.com.server.serverMain;

public class mainClass {
	
	int port;
	String hostName;
	int maxConnections;
	boolean ok;
	long ping;
	Thread eventDis = Thread.currentThread();
	boolean movingOn = false;
	final int pingTimeOut = 5000;
	
	public static void main(String args[]){
		mainClass.makeDoc();
		new mainClass();
	}
	
	JFrame frame = new JFrame("Jazy's Chat Room");
	public JTextArea status;
	boolean busy = false;
	public static StyledDocument doc = new JTextPane().getStyledDocument();
	
	static public String getTimeStamp(String from){
		Date date = new Date();
		String re = from +" ";
		re += "["+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)+"]:";
		return re;
	}
	
	public mainClass(){
		
		status = new JTextArea(3,15);
		status.setText("Please select Client or Server");
		status.setEditable(false);
		JButton client = new JButton("Client");
		client.setSize(50,15);
		client.setActionCommand("client");
		client.addActionListener(new buttonListener());
		JButton pingB = new JButton("Ping");
		pingB.setActionCommand("ping");
		pingB.addActionListener(new buttonListener());
		JButton server = new JButton("Server");
		server.setSize(50,15);
		server.setActionCommand("server");
		server.addActionListener(new buttonListener());
		BorderLayout layout = new BorderLayout();
		layout.setHgap(20);
		layout.setVgap(20);
		frame.getContentPane().setLayout(layout);
		
		frame.getContentPane().add(status,BorderLayout.NORTH);
		frame.getContentPane().add(client,BorderLayout.WEST);
		frame.getContentPane().add(server,BorderLayout.EAST);
		frame.getContentPane().add(pingB,BorderLayout.CENTER);
		frame.addWindowListener(new windowAdapter());
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
	}
	
	public static void makeDoc(){
		Style regular = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style style = doc.addStyle("regular", regular);
		style = doc.addStyle("bold", regular);
		StyleConstants.setBold(style, true);
		style = doc.addStyle("red", regular);
		StyleConstants.setForeground(style, Color.red);
		style = doc.addStyle("green", regular);
		StyleConstants.setForeground(style, Color.green);
	}
	
	public void log(String mes){
		status.setText(mes);
	}
	
	
	private class windowAdapter implements WindowListener {

		public void windowActivated(WindowEvent arg0) {
		}

		public void windowClosed(WindowEvent arg0) { if(movingOn) return; //Override
			System.exit(0);
		}

		public void windowClosing(WindowEvent arg0) { if(movingOn) return; //Override
			frame.dispose();
		}

		public void windowDeactivated(WindowEvent arg0) {
		}

		public void windowDeiconified(WindowEvent arg0) {
		}

		public void windowIconified(WindowEvent arg0) {
		}

		public void windowOpened(WindowEvent arg0) {
		}
		
	}
	
	public class buttonListener implements ActionListener {

		public void actionPerformed(ActionEvent e) { if (busy) return; busy = true;
		
			if (e.getActionCommand().equals("client")){ 
			log("Selected Client");
			String opText = "Input hostname: and port";
			do{
				ok = false;
				String inp;
				inp = JOptionPane.showInputDialog(opText,"HOSTNAME:PORT");
				if (inp == null){
					busy = false;
					log("Please select Client or Server");
					return;
				}
				if (!inp.contains(":")){
					opText = "Inavlid input, please try again...";
					continue;
				}
				try{
					port = Integer.parseInt(inp.substring(inp.indexOf(":")+1));
				}catch(Exception ee){
					System.out.print(ee.getMessage());
					opText = "Invalid port, try again...";
					continue;
				}
				hostName = inp.substring(0,inp.indexOf(":"));
				
				log("Polling "+inp);
				ok = true;
				
			}while(!ok);
			
			//Shit got real below...
			new Thread(new Runnable(){
				public void run(){
			Socket sc = null;
			try{ //Poll connection
				InetAddress to = InetAddress.getByName(hostName);
				System.out.println("Polling " + to.getHostAddress());
				sc = new Socket(to.getHostAddress(),port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sc.getOutputStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				long time = System.currentTimeMillis();
				out.write("<poll/>\n");
				out.flush();
				String inP;
				ping = -1;
				int i = 0;
				boolean timeOut = false;
				while ((!timeOut)&&(inP = in.readLine())!=null){
					if (inP.equals("<poll/>")){
						ping = System.currentTimeMillis() - time;
						break;
					}
					if (i >= pingTimeOut){
						timeOut = true;
					}
					i++;
				}
				if (!timeOut){
				log(hostName+":"+port+"\nPing:"+ping+"\nSuccess, Initializing client interface!");
				new Thread(new Runnable(){
					public void run(){
						try{
							Thread.sleep(1500);
						}catch(Exception ee){}
						movingOn = true;
						new clientMain(hostName,port);
						frame.dispose();
					}
				}).start();
				}else{
					log(hostName+" timed out! Please check connection...");
					busy = false;
				}
				
			}catch(Exception ee){
				System.out.println("Error polling:"+ee.getMessage());
				log("Error connecting to "+hostName+":"+port);
				busy = false;
			}finally{
				try{
					sc.close();
				}catch(Exception asda){}
			}
				}
			}).start();
			
			}else if (e.getActionCommand().equals("server")){ 
				
			log("Selected Server: Input Listen Port");
			String opText = "Listening Port (0 for random)";
			do{
				ok = false;
				String inp;
				try{
					inp = JOptionPane.showInputDialog(opText,"0");
					if (inp == null){ //Exit
						busy = false;
						log("Please select Client or Server");
						return;
					}
					port = Integer.parseInt(inp);
					
							//Test port
							ServerSocket sc = null;
							try{
								sc = new ServerSocket(port);
								port = sc.getLocalPort();
								ok = true;
							}catch(Exception ee){
								System.out.println(ee.getMessage());
								log("Cannot bind to port:"+port);
								opText = "Cannot bind to port, try again...";
							}finally{
								try{
								sc.close();
								}catch(Exception asd){}
							}
					
				}catch(NumberFormatException ee){
					System.out.println("Error with input:"+ee.getMessage());
					opText = "Port must be an integer!";
				}
			}while(!ok);
			
			log("Port choosen:"+port);
			
			ok = false;
			opText = "Max number of Connections";
			do{
				String inp = JOptionPane.showInputDialog(opText,"10");
				if (inp == null){
					maxConnections = 10;
					break;
				}
				try{
				maxConnections = Integer.parseInt(inp);
				ok = true;
				}catch(Exception ee){
					System.out.println(ee.getMessage());
					opText = "Not a valid integer, try again...";
				}
			}while(!ok);
			
			log("Port:"+port+" Maxconnections:"+maxConnections+"\nStarting server interface now!");
			new Thread(new Runnable(){
				public void run() {
					try{
						Thread.sleep(2500);
					}catch(Exception e){}
					movingOn = true;
					new serverMain(port,maxConnections);
					frame.dispose();
				}
			}).start();
			
			}else if (e.getActionCommand().equals("ping")){
				log("Selected Ping");
				String opText = "Input hostname: and port";
				do{
					ok = false;
					String inp;
					inp = JOptionPane.showInputDialog(opText,"HOSTNAME:PORT");
					if (inp == null){
						busy = false;
						log("Please select Client or Server");
						return;
					}
					if (!inp.contains(":")){
						opText = "Inavlid input, please try again...";
						continue;
					}
					try{
						port = Integer.parseInt(inp.substring(inp.indexOf(":")+1));
					}catch(Exception ee){
						System.out.print(ee.getMessage());
						opText = "Invalid port, try again...";
						continue;
					}
					hostName = inp.substring(0,inp.indexOf(":"));
					
					log("Polling "+inp);
					ok = true;
					
				}while(!ok);
				
				//Shit got real below...
				new Thread(new Runnable(){
					public void run(){
						Socket sc = null;
						try{ //Poll connection
							InetAddress to = InetAddress.getByName(hostName);
							System.out.println("Polling " + to.getHostAddress());
							sc = new Socket(to.getHostAddress(),port);
							BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sc.getOutputStream()));
							BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
							long time = System.currentTimeMillis();
							out.write("<poll/>\n");
							out.flush();
							String inP;
							ping = -1;
							int i = 0;
							boolean timeOut = false;
							while ((!timeOut)&&(inP = in.readLine())!=null){
								if (inP.equals("<poll/>")){
									ping = System.currentTimeMillis() - time;
									break;
								}
								if (i >= pingTimeOut){
									timeOut = true;
								}
								try{Thread.sleep(1);}catch(Exception asd){}
								i++;
							}
							if (!timeOut){
								log(hostName+":"+port+"\nPing:"+ping+"ms");
							}else{
								log(hostName+" timed out!");
							}
						}catch(Exception e){
							System.out.println(e.getMessage());
							log("Error:"+e.getMessage());
						}finally{
							try{
								sc.close();
							}catch(Exception ee){}
							busy = false;
						}
					}
				}).start();
				//End of thread
			}
		}
	}
}

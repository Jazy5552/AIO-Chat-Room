package jazy.com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.ServerSocket;
import java.util.ArrayList;

import jazy.com.gui.mainClass;

public class clientHandler extends Thread{

	static ArrayList<clientThread> clients = new ArrayList<clientThread>(0);
	static ServerSocket server;
	static int port;
	static BufferedReader in;
	static BufferedWriter out;
	
	
	public clientHandler(ServerSocket ss){
		server = ss;
		port = ss.getLocalPort();
		updateClientsList();
		new Thread(new Runnable(){
			public void run(){
				while(!server.isClosed()){
				updateClientsList();
				serverMain.status.setText("Port:"+port+" Connections:"+clients.size());
				try{Thread.sleep(800);}catch(Exception e){}
				}
				System.out.println("Socket Closed!!!");
			}
		}).start();
		start();
	}
	
	public void run(){
		while(!server.isClosed()){
			try{
			if (clients.size()<serverMain.maxConnections){
				clients.add(new clientThread(server.accept()));
			}
			Thread.sleep(20);
			}catch(Exception e){
				System.out.println("Error connecting:"+e.getMessage());
				serverMain.log(e.getMessage());
			}
		}
	}
	
	public void kickClient(String ipaddress){
		try{ //Safety precaution
		for (int i=0;i<clients.size();i++){
			if (ipaddress.equals(clients.get(i).id)){
				sendMes("<bye/>",clients.get(i).id);
				clients.get(i).close();
			}
		}
		}catch(Exception ee){}
	}
	
	public void sendMes(String message,String clientID){
		try{ //Safety precaution
		System.out.println("Message to "+clientID+": "+message);
		message+="\n<end/>";
		for (int i = 0;i<clients.size();i++){
			if (clients.get(i).id.equals(clientID)){
				clients.get(i).send(message);
			}
		}
		}catch(Exception ee){}
	}
	
	public void sendMes(String message){
		try{ //Safety precaution
		serverMain.log(message);
		message+="\n<end/>";
			for (int i = 0;i<clients.size();i++){
				clients.get(i).send(message);
			}
		}catch(Exception ee){}
	}
	
	public synchronized void updateClientsList(){
		if (clients.size()==0){
			return;
		}
		for (int i=0;i<clients.size();i++){
			if (clients.get(i).con.isClosed()){
				clients.remove(i);
			}
		}
	}
	
	public void disconnectAll(){
		sendMes(mainClass.getTimeStamp("Server")+"Closing down!");
		sendMes("<bye/>");
		updateClientsList();
		for (int i = 0;i<clients.size();i++){
			clients.get(i).close();
		}
		close();
	}
	
	public void close(){
		try{
			server.close();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	
	
	
	
	
	
}

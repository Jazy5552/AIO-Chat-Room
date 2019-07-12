package jazy.com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import jazy.com.gui.mainClass;

public class clientThread extends Thread{

	final int port;
	String id;
	final Socket con;
	final clientHandler ser = serverMain.cl;
	boolean disconnect = false;
	
	clientThread(Socket s){
		con = s;
		port = s.getLocalPort();
		id = s.getInetAddress().getHostAddress();
		try{
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		}catch(Exception e){
			System.out.println("Error with ServerClient:"+e.getMessage());
		}
		start(); //litsen for input
	}
	
	BufferedReader in;
	BufferedWriter out;
	
	public void run(){
		System.out.println(id+" listening for inStream");
		String inP;
		String tmp = "";
		try{
		while((!disconnect)&&((inP = in.readLine())!=null)){
			System.out.println("SERVERRECEIVED:"+inP);
			if (inP.equals("<bye/>")){
				serverMain.log(id + " disconnecting!");
				close();
			}else if (inP.equals("<con/>")){ //Establishing connection
				ser.sendMes(id+ " Connected!");
			}else if (inP.equals("/load")){
				ser.sendMes("\n"+serverMain.logArea.getText());
			}else if (inP.equals("<end/>")){ //end of stream
				tmp = tmp.substring(tmp.indexOf("\n")+1,tmp.length());
				if (tmp.replaceAll("\n", "").replaceAll(" ", "").equals("")) {
					tmp = "";
					continue;
				}
				tmp = tmp.replaceAll("\n", "\n   ");
				ser.sendMes(mainClass.getTimeStamp(id)+tmp);
				tmp = "";
			}else if (inP.equals("<poll/>")){ //Just polling server
				ser.sendMes("<poll/>",id);
				disconnect = true;
				close();
			}else if (inP.equals("<ping/>")){
				send("<ping/>");
			}else if (inP.startsWith("/setname")){
					try{ //TODO Save to file
						String newID = inP.replace("/setname", "");
						newID.replaceAll(" ", "");
						ser.sendMes(id+" changed to:"+newID);
						id = newID;
					}catch(Exception e){
						System.out.println(e.getMessage());
					}
			}else{
				tmp = tmp + "\n" +inP;
			}
		}
		}catch(Exception e){
			System.out.println("End of Stream:"+e.getMessage());
			if (!disconnect) serverMain.log(id + " lost connection!");
		}finally{
			close();
		}
	}
	
	public synchronized void send(String mes){
		System.out.println("SERVERSENT:"+mes);
		try{
		out.write(mes+"\n");
		out.flush();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public synchronized void close(){
		try{
			in.close();
			out.flush();
			out.close();
			con.close();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
}

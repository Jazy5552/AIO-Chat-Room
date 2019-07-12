package jazy.com.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class clientStreams extends Thread{
	
	BufferedReader in;
	BufferedWriter out;
	Socket con;
	String hostName;
	int port;
	
	clientStreams(String h,int p){
		try{
			con = new Socket(h,p);
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
		}catch(Exception e){
			System.out.println(e.getMessage());
			clientMain.log("Error Connecting:"+e.getMessage(),"red");
			clientMain.status.setText("Offline");
			try{
				con.close();
				in.close();
				out.close();
			}catch(Exception eee){}
			return;
		}
		port = con.getLocalPort();
		hostName = con.getInetAddress().getHostAddress();
		clientMain.status.setText("Online:"+hostName);
		start();
		send("<con/>\n");
	}
	
	public void run(){
		String inM;
		String tmp = "";
		try{
		while((inM = in.readLine())!=null){
			System.out.println("CLIENTRECEIVED:"+inM);
			if (inM.equals("<bye/>")){ //They are disconnecting us anyways
				break;
			}
			if (inM.equals("<end/>")){
				tmp = tmp.substring(tmp.indexOf("\n")+1,tmp.length());
				tmp = tmp.replaceAll("\n", "\n   ");
				clientMain.log(tmp,"regular");
				tmp = "";
			}else{
				tmp = tmp + "\n" +inM;
			}
			
		}
		}catch(Exception e){
			System.out.println("End of input:"+e.getMessage());
			clientMain.log("End of Stream:"+e.getMessage(),"red");
		}finally{
			disconnect();
		}
	}
	
	public void send(String mes){
		System.out.println("CLIENTSENT:"+mes+"\n<end/>");
		try {
			out.write(mes+"\n<end/>\n");
			out.flush();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			try{
			clientMain.d.insertString(clientMain.d.getLength(), "Message Not Sent!", clientMain.d.getStyle("red"));
			}catch(Exception ee){}
		}
	}
	
	public void disconnect(){
		send("<bye/>");
		clientMain.log("Disconnected!","red");
		clientMain.status.setText("Offline");
		try{
			out.close();
			in.close();
			con.close();
		}catch(Exception e){}
	}
	
}

 import java.io.IOException;
import java.net.ServerSocket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
		 

/** Signage Server **/

public class SignageServer {
	public static String signagepath = new File(".").getAbsoluteFile().getParent() + File.separator + "signage";
	//public static String signagepath = "/Users/kentaro/Trashcan_s/signage";
	//public static String disppath = "file:///Users/kentaro/Sites/aws_deploy/nodejs/ISTrashcanDisp/ISTrashcan.html";
	public static String path = new File(".").getAbsoluteFile().getParent() + File.separator + "server";
	//public static String path = "/Users/kentaro/Trashcan_s/server";
	
	
	public static int num = 0;
	
	public static class Server{
	   private ServerSocket ss;
	 
	Server(){
	       Server.CreateServerSocket Con = new Server.CreateServerSocket();
	       this.ss = Con.GetSS();
	       
	     while (true){
	       try
	       {
	           new FilesInOut(this.ss.accept() , num).start();
	       } catch (IOException e) {
	           System.out.println("[Server] Connection Lost");
	           System.exit(1);
	       }
	     }
	 }  
	
	 public class CreateServerSocket{
	       int port = 50000;
	       ServerSocket serversocket = null;
	 
	     public ServerSocket GetSS() {
	       try {
	    	   this.serversocket = new ServerSocket(this.port);
	           System.out.println("[Server] Waiting for Client...");
	       } catch (IOException e) {
	           System.out.println("[Server] Access is not Available");
	           System.exit(1);
	       }
	         return this.serversocket;
	     }
	   }
	 
	 public class FilesInOut extends Thread{
	     Socket socket = null;
	     String clientname = null;
	     BufferedInputStream in = null;
	     BufferedOutputStream out = null;
	   Calendar c;
	   SimpleDateFormat sdf1;
	   Date dt;
	   int tnum;
	 
	   public FilesInOut(Socket s, int n){
	       this.socket = s;
	       this.tnum = n;
	       
	     try{
	         this.in = new BufferedInputStream(this.socket.getInputStream());
	         this.out = new BufferedOutputStream(this.socket.getOutputStream());
	 
	         InetAddress inet = this.socket.getInetAddress();
	         this.clientname = inet.getHostName();
	         System.out.println("[Server] " + this.clientname + " Connect.");
	         num++;
	         
	     }catch (IOException ie){
	    	 
	       try {
	           this.in.close();
	           this.out.close();
	           this.socket.close();
	           System.out.println("[Server] Connection Disable"); }
	       catch (IOException localIOException1) {
	       }
	         return;
	     }
	   }
	 
	   public void run()
	   {
	       this.c = Calendar.getInstance();
	       this.dt = this.c.getTime();
	       this.sdf1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	     try
	     {
	         FileOutputStream fout = new FileOutputStream(path + File.separator + this.sdf1.format(this.dt) + "_" + this.tnum + ".png");
	 
	         byte[] buf = new byte[1024];
	         int len = 0;
	         while ((len = this.in.read(buf)) != -1) {
	           fout.write(buf, 0, len);
	       }
	         fout.flush();
	         fout.close();
	 
	         this.in.close();
	         this.out.close();
	         this.socket.close();
	         
	         System.out.println("[Server] " + this.sdf1.format(this.dt) + "_" + this.tnum + ".png Created");
	     }
	     catch (IOException ie) {
	         ie.printStackTrace();
	         System.out.println("[Server] Cannot Read Files");
	     }
	   }
	 }
	}
	
	public static class ThumbnailCopyThread extends Thread {
	    public void run() {
	    	
	    	FileCtrl fctrl = new FileCtrl();
	    	
	    	while (true){
	 	       try
	 	       {
	 	    	  File[] thumbfiles = fctrl.listFiles(path, "png",fctrl.TYPE_FILE, true, 0);
	 	    	  if(thumbfiles.length > 40){
	 	    		  fctrl.manageSignage(thumbfiles, signagepath, 40);
	 	    		  System.out.println("[Server] Copy Thumbnail");
	 	    	  }
	 	    	  
                  Thread.sleep(15*60*1000);
	 	       } catch (InterruptedException e) {
				e.printStackTrace();
			}
	 	     }
	    }
	}
	
	
	public static void main(String[] args) {
			
			ThumbnailCopyThread tct = new ThumbnailCopyThread();
	        tct.start();
	        
		    Server sv = new Server();
		    
		 }
	
	/** ブラウザの起動 
	public static void loadBrowser() {
		Desktop desktop = Desktop.getDesktop();
		String uriString = disppath;
		try {
			URI uri = new URI(uriString);
			desktop.browse(uri);
		} catch (URISyntaxException e) {
			System.out.println("ブラウザ起動失敗しました。");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ブラウザ起動失敗しました。");
			e.printStackTrace();
		}
	}
	**/
	
}

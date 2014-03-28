import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
//import de.uni_siegen.wineme.come_in.thumbnailer.thumbnailers

public class TrashcanMacClient {
	
	public static String crlf = System.getProperty("line.separator");
	public static String[] path = {System.getProperty("user.home") + File.separator + "Trashcan" + File.separator + "Trashcan", 
		System.getProperty("user.home") + File.separator + "Desktop"
		};  
	//public static String path = "/Users/kentaro/Trashcan";
	public static String settingpath = System.getProperty("user.home") + File.separator + "Trashcan" + File.separator + "setting";
	//public static String settingpath = "/Users/kentaro/Trashcan/setting";
	public static String host = "192.168.27.99";
	//public static String host = "127.0.0.1";
	public static String doctype = "doc,docx,ppt,pptx,xls,xlsx,pdf,txt,jpeg,jpg,png,bmp,gif,JPG,JPEG,GIF,PNG,PDF,BMP,DOC,DOCX,XLS,XLSX,PPT,PPTX,PDF,TXT";
	public static int port = 50000;
	
	/** main class **/
    public static void main(String[] args) {
    	
        //ファイル処理スレッドの開始
        FilesCapture fc = new FilesCapture();
        fc.start();
    }
        
     public static class FilesCapture extends Thread{
         
        	public void run(){
        		
        		//ファイル操作クラスの呼び出し
            	FileCtrl fctrl = new FileCtrl();
            	
            	//サムネイル取得クラスの呼び出し
            	ExternalCommand ec = new ExternalCommand();
        		
        		while(true){
        		System.out.println("FilesCapture Start");
        		
        		for(int t = 0; t < path.length; t++){
        		try{
                	//リストファイルの読み込み
	                	File outfile = new File(settingpath + File.separator + "filelist.txt");
	                	//File outfile = new File("/Users/kentaro/trashcan/filelist.txt");
	                	String currentfiles = fctrl.fileToString(outfile);
	                	
						//全ファイルの読み込み
	                	File[] files = fctrl.listFiles(path[t], doctype);
	                	files = fctrl.listFiles(path[t], doctype ,fctrl.TYPE_FILE, true, 0);
                	  
	                //ファイルの差分チェック
	                  ArrayList<File> list = new ArrayList<File>();
                	  for(int i = 0; i < files.length; i++){
                		//ファイルの差分チェック
                		  if(currentfiles.indexOf(files[i].getAbsolutePath()) == -1){
                			  list.add(files[i]);
                		  }
                	  }
                	  
                	  if(list.size() != 0){
                		File[] newfiles = (File[])list.toArray(new File[list.size()]);
                	  
                	  //新ファイル情報のリストファイルへの書き込み
                	  	FileWriter filewriter = new FileWriter(outfile, true);
        		        fctrl.printFileList(newfiles, filewriter);
        		        filewriter.close();
        		        System.out.println("ListFile Written");
        		        
        		      //サムネイル取得
        		        for(int i=0; i < newfiles.length; i++){
        			        try {
        			        	//サムネイルの作成
        						ArrayList outmess = ec.doExec(new String[]{settingpath + File.separator + "makeimg.sh", newfiles[i].getAbsolutePath(), "750", settingpath + File.separator + "thumbnail"}, "UTF-8");
        			        	System.out.println("Thumnail Result:" + outmess);
        			        	Thread.sleep(1*1*1000);
        					} catch (IOException e) {
        						e.printStackTrace();
        					} catch (InterruptedException e) {
								// TODO 自動生成された catch ブロック
								e.printStackTrace();
							}
        		        }
        		        
        		        //サーバーにサムネイル画像を転送
        		        //Socket soc = new Socket(host, port);
        		          
        		        for (int i = 0; i < newfiles.length; i++) {
        		        	boolean sent_flag = false;
        		        	while(!sent_flag){
        		        		System.out.println("Sent Start...");
        		        		sent_flag = SendFiles(newfiles[i]);
        		        		if(sent_flag == false){
        		        		try {
        		        			System.out.println("Waiting for Reconnect...");
        		        			Thread.sleep(1*60*1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
        		        		}
        		        	}
        		        	
        		        	try {
    		        			Thread.sleep(1*10*1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
        		        	
        	            }
        	        	//soc.close();
        		        
                	  }
        	        	
        	        	fctrl.clear();
        	        	
              	}catch(IOException e){
              	  System.out.println("FilesCapture Fail");	
              	  System.out.println(e);
                }
        		}
        		System.out.println("FilesCapture End");
        		
                try {
					Thread.sleep(1*60*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                
        		}
        	}
        	
        }
        

     /** ファイルの送信処理 
     * @return **/
    public static boolean SendFiles(File file) {
    	
    	try{
    		Socket soc = new Socket(host, port);
    	  BufferedOutputStream out = new BufferedOutputStream(soc.getOutputStream());
          FileInputStream fin = new FileInputStream(settingpath + File.separator + "thumbnail" + File.separator + file.getName() + ".png");
          byte[] buf = new byte[1024];
          int len = 0;
          while ((len = fin.read(buf)) != -1) {
            out.write(buf, 0, len);
          }
          out.flush();
          fin.close();
          out.close();
          soc.close();
          System.out.println(file + " Sent.");
          return true;
        }
        catch (FileNotFoundException ie) {
          System.out.println(file + " not Exist.");
          return true;
        }
        catch (UnknownHostException e) {
          e.printStackTrace();
          System.out.println("Unknown Host");
          return false;
        }
        catch (IOException e) {
          e.printStackTrace();
          System.out.println("IO Error");
          return false;
        }
    	}

}

/**
filewriter.write(crlf + "●拡張子docxのファイルを取得");
files = fctrl.listFiles(path, "*.docx");
printFileList(files, filewriter);
fctrl.clear();

	  
filewriter.write(crlf + "●全てのファイルとディレクトリを取得");
files = fctrl.listFiles(path, null,fctrl.TYPE_FILE_OR_DIR, true, 0);
printFileList(files, filewriter);
fctrl.clear();


filewriter.write(crlf + "●現在の日付から、2日前以降に更新されたファイルを取得");
files = fctrl.listFiles(path, null,fctrl.TYPE_FILE, true, 2);
printFileList(files, filewriter);
fctrl.clear();

filewriter.write(crlf + "●現在の日付から、30日以前の古いファイルを取得");
files = fctrl.listFiles(path, null,fctrl.TYPE_FILE, true, -30);
printFileList(files, filewriter);
fctrl.clear();
**/

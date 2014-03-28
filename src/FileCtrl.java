import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

public class FileCtrl {

    public static final int TYPE_FILE_OR_DIR = 1;
    public static final int TYPE_FILE = 2;
    public static final int TYPE_DIR = 3;
    public static String crlf = System.getProperty("line.separator");
    
    
    /** ファイルのリストアップ **/
    public File[] listFiles(String directoryPath, String fileName) {
        // ワイルドカード文字として*を正規表現に変換
        if (fileName != null) {
            fileName = fileName.replace(".", "\\.");
            fileName = fileName.replace("*", ".*");
        }
        return listFiles(directoryPath, fileName, TYPE_FILE, true, 0);
    }

    
    public File[] listFiles(String directoryPath, 
            String fileNamePattern, int type, 
            boolean isRecursive, int period) {
        
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException
            ("Path[" + dir.getAbsolutePath() + 
                    "] is not Dirctory");
        }
        File[] files = dir.listFiles();
        // その出力
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            addFile(type, fileNamePattern, set, file, period);
            // 再帰的に検索＆ディレクトリならば再帰的にリストに追加
            if (isRecursive && file.isDirectory()) {
                listFiles(file.getAbsolutePath(), fileNamePattern, 
                            type, isRecursive, period);
            }
        }
        return (File[]) set.toArray(new File[set.size()]);
    }
    
    
    private void addFile(int type, String match, TreeSet set,
            File file,int period) {
    	
    	//タイプで選別
    	switch (type) {
        case TYPE_FILE:
            if (!file.isFile()) {
                return;
            }
            
            //隠しファイル,特殊ファイルを除外
            if (file.getAbsolutePath().lastIndexOf(File.separator + ".") != -1) {
            	return;
            }else 
            	if(file.getAbsolutePath().lastIndexOf(".") == -1){
            	return;
            
            }
        	
        	//ファイルタイプ(拡張子)の指定
        	int point = file.getName().lastIndexOf(".");
            String extension = file.getName().substring(point + 1);
             if (match != null && match.indexOf(extension) == -1) {
                 return;
          }
            
            break;
        case TYPE_DIR:
            if (!file.isDirectory()) {
                return;
            }
            break;
        
        }
        
     // ファイル更新日付
        Date lastModifiedDate = new Date(file.lastModified());
        String lastModifiedDateStr = new SimpleDateFormat("yyyyMMdd")
                .format(lastModifiedDate);
        
        if (period != 0) {

            // 指定の日付（１日をミリ秒で計算）
            long oneDayTime = 24L * 60L * 60L * 1000L; 
            long periodTime = oneDayTime * Math.abs(period);
            Date designatedDate = 
                new Date(System.currentTimeMillis() - periodTime);
            String designatedDateStr = new SimpleDateFormat("yyyyMMdd")
                    .format(designatedDate);
            if (period > 0) {
                if (lastModifiedDateStr.compareTo(designatedDateStr) < 0) {
                    return;
                }
            } else {
                if (lastModifiedDateStr.compareTo(designatedDateStr) > 0) {
                    return;
                }
            }
        }
        // 全ての条件に該当する場合リストに格納
        set.add(file);

    }

    //アルファベット順に並べるためTreeSetを使用
    private TreeSet set = new TreeSet();

    public void clear(){
    	set.clear();
    }
    
    
    /** ファイル一覧のテキストへの書き込み **/
    public void printFileList(File[] files, FileWriter filewriter) {
    	
    	 Calendar c = Calendar.getInstance();
	     Date dt = c.getTime();
	     SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	       
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            
          //拡張子
            String extension = "";
            String filename = file.getName();             
            int point = filename.lastIndexOf(".");
            if (point != -1) {
                extension = filename.substring(point + 1);
            }
            
           //日付 
            Date lastModifiedDate = new Date(file.lastModified());
            String lastModifiedDateStr = new SimpleDateFormat("yyyy/MM/dd")
                    .format(lastModifiedDate);
            try {
				filewriter.write("\"" + sdf.format(dt) + "\",\"" + file.getAbsolutePath() + "\",\"" + extension + "\",\"" + lastModifiedDateStr + "\""+ crlf);
			} catch (IOException e) {
				System.out.println("File Write Error");	
				e.printStackTrace();
			}
        }
    }
    
    /** リストのファイルへの書き込み **/
    public void printFileArrayList(ArrayList a, FileWriter filewriter) {
    	
       for (int i = 0; i < a.size() ; i++) {
           try {
				filewriter.write(a.get(i) + crlf);
			} catch (IOException e) {
				System.out.println("File Write Error");	
				e.printStackTrace();
			}
       }
   }
    
    /** ランダムに抽出したファイルのコピー **/
    public void manageSignage(File[] files, String directoryPathTo, int filenum) {
            
    		//配列準備
    		int[] a = new int[files.length];
    		for(int i = 0; i < files.length; i++){
    			a[i] = i;
    		}
    		
    		//配列のシャッフル
    		 for(int i = 0; i < files.length; i++){
    	            int t = (int)(Math.random() * i);  //0～i-1の中から適当に選ぶ
    	            //選ばれた値と交換する
    	            int tmp = a[i];
    	            a[i]  = a[t];
    	            a[t]  = tmp;
    	        }
    		
            // ランダムにファイルをfilenum個取得しコピー
            for (int i = 0; i < filenum; i++) {
            	File file = files[a[i]];
            	if(file.length() != 0){
	            	try {
						copyFile(file.getAbsolutePath(),directoryPathTo + File.separator + i + ".png");
					} catch (IOException e) {
						System.out.println("Fail to File Copy " + file.getAbsolutePath() + " to " + directoryPathTo);
						e.printStackTrace();
					}
            	}
            }
            
    	}
    	

    /** 単体ファイルのコピー **/
    public static void copyFile(String srcPath, String destPath) 
        throws IOException {
        
		FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
        FileChannel destChannel = new FileOutputStream(destPath).getChannel();
        try {
            srcChannel.transferTo(0, srcChannel.size(), destChannel);
        } finally {
            srcChannel.close();
            destChannel.close();
        }

    }
    
    /** ファイルを読み込み、文字列に変換 **/
	  public String fileToString(File file) throws IOException {
	    BufferedReader br = null;
	    try {
	      // ファイルを読み込むバッファドリーダを作成します。
	      br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	      // 読み込んだ文字列を保持するストリングバッファを用意します。
	      StringBuffer sb = new StringBuffer();
	      // ファイルから読み込んだ一文字を保存する変数です。
	      int c;
	      // ファイルから１文字ずつ読み込み、バッファへ追加します。
	      while ((c = br.read()) != -1) {
	        sb.append((char) c);
	      }
	      // バッファの内容を文字列化して返します。
	      return sb.toString();
	    } finally {
	      // リーダを閉じます。
	      br.close();
	    }
	  }
 
    	  
}

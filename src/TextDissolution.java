import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class TextDissolution {

	static String fs = File.separator;	
	
	//外部プログラム実行クラスの呼び出し
	static ExternalCommand ec = new ExternalCommand();
	
	//ファイル取得クラスの呼び出し
	static FileCtrl fctrl = new FileCtrl();
	
	
	
	/* 特定ディレクトリからファイルを取得
     * @param {String} p ディレクトリのパス
     * @param {String} t 取得するファイルタイプ
     * @returns {File[]} ディレクトリに存在するファイル
    */
	public static File[] getfile(String p, String t){
		//ファイルの取得
		File[] files = fctrl.listFiles(p, t);
		files = fctrl.listFiles(p, t ,fctrl.TYPE_FILE, true, 0);
		
		return files;
	}

	
	/* docやpdfファイルからテキストを抽出
     * @param {File} f 対象のファイルオブジェクト
     * @returns {ArrayList} テキストの行ごとのリスト
    */
	private static ArrayList filetext(File f){
		//filtdumpのパス
		String pathfilt = "C:\\Program Files\\Windows Kits\\8.1\\bin\\x86\\filtdump.exe";
		
    	ArrayList outmess = new ArrayList();
    	
    	//ifilterの実行と返り値の取得
    	//filtdump.exeはコンソール出力の場合はMS932で文字を返すと思われる。
    	try {
			outmess.addAll(ec.doExec(new String[]{pathfilt, "-b", f.getAbsolutePath()}, "MS932"));
			
			//テキスト部分のみを取り出し
			Iterator it = outmess.iterator();
			boolean adopt = false;
			while (it.hasNext()) {
				String value = (String) it.next();
				if(adopt){
					adopt = false;
				}else{
					it.remove();
					if(value.startsWith("TEXT: ----")){
							adopt = true;
					}
				}
    		}
			
    	} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return outmess;
	}

	
	/* テキストファイルから特定の品詞のみを抽出
     * @param {File} 対象となるテキストファイルのオブジェクト 
     * @returns {ArrayList}　抽出された単語のリスト
    */
	private static ArrayList textword (File f){
		//MeCabのパス
		String pathmecab = "C:\\Program Files\\MeCab\\bin\\mecab.exe";
	
		ArrayList<String> woutmess = new ArrayList<String>();
		//MeCabの実行
		//MecabはUTF-8で返り値を返す。
		try {
			woutmess.addAll(ec.doExec(new String[]{pathmecab, f.getAbsolutePath()}, "UTF-8"));
		//名詞以外を除去
		Iterator wit = woutmess.iterator();
		
		while (wit.hasNext()) {
			String wvalue = (String) wit.next();
			
			System.out.println(wvalue);
			
			if(wvalue.equals("EOS")){
				wit.remove();
			}else{
				String[] valuetype = wvalue.split("[\t|,]", 0);
				if(!valuetype[2].startsWith("固有名詞")){
					wit.remove();
				}	
			}
		}
		
		//単語のみ取り出し
		for(int i = 0; i< woutmess.size(); i++){
			String all = woutmess.get(i);
			String[] allelem = all.split("[\t|,]", 0);
			woutmess.set(i, allelem[0]);
		}
		
		} catch (IOException e) {
			  e.printStackTrace();
			return null;
		  }
		return woutmess;		
		
	}
	
	/* MAIN */
	public static void main(String[] args) {
		
		/**
			String doctype = "doc,docx,ppt,pptx,xls,xlsx,pdf,DOC,DOCX,XLS,XLSX,PPT,PPTX,PDF";
		**/
		
		File df = new File("C:\\Users\\studentJP\\Documents\\Trashcan_s\\file\\test.pdf");

		//fileからtext抽出
		ArrayList ft = filetext(df);
		String filename = df.getName();
	    filename = filename.substring(0, filename.lastIndexOf(".")) + ".txt";
		File outfile = new File("C:\\Users\\studentJP\\Documents\\Trashcan_s\\text\\" + filename);
		
		FileWriter filewriter;
		try {
			filewriter = new FileWriter(outfile , true);
			fctrl.printFileArrayList(ft, filewriter);
	        filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		
        //textからword抽出
		ArrayList<String> word = textword(outfile);
		
		for(int ii = 0; ii < word.size(); ii++){
			System.out.println(word.get(ii));
		}
	}
	
}


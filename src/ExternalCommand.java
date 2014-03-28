import java.io.*;
import java.util.ArrayList;

/**
 * 外部コマンドを実行するクラス。 外部コマンドを実行し、そのコマンドが標準出力に出力する文字列を取得することができる。
 * 
 * @since 2004/05/05
 * @author Net Aqua Project all rights reserved.
 */

public class ExternalCommand implements Runnable {
	private StringWriter strWriter;
	private PrintWriter pwriter;
	private BufferedReader buffReader;	
	private ArrayList list = new ArrayList(1000);
	
	public ExternalCommand() {
	}

	/**
	 * 外部コマンドを実行する。
	 * 
	 * @param command
	 *            実行する外部コマンド
	 * @return String 外部コマンドが標準出力に出力する実行結果
	 * @throws IOException
	 */
	
	public ArrayList doExec(String[] command, String code) throws IOException {
		
		list.clear();
		
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(command);
		buffReader = new BufferedReader(new InputStreamReader(
				proc.getInputStream(), code));
		
		/**
		strWriter = new StringWriter();
		pwriter = new PrintWriter(strWriter);
		**/
		
		// 出力結果を読み終わるまで待つ。
		Thread th = new Thread(this);
		th.start();
		try {
			th.join();
		} catch (InterruptedException e) {
			throw new IOException("Command Exec Failed");
		}
		buffReader.close();
		
		/**
		pwriter.close();
		// 文字列の最後の改行を削除する。
		String temp = strWriter.toString();
		
		if ((temp.length() > 1)
				&& (temp.substring(temp.length() - 1).getBytes()[0] == 10)) {
			temp = temp.substring(0, temp.length() - 1);
		}
		
		return temp;
		**/
		
		
		try {
			proc.waitFor();
			proc.destroy();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return list;
		
		
	}

	/**
	 * コマンドの実行結果を読み出す。
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		String line = null;
		try {
			while ((line = buffReader.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

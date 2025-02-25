package constants;

import java.io.File;

/**
 * 環境設定.
 *
 * @author cyrus
 */
public interface Configurations {

	/**
	 * セッションファイルの出力先.
	 */
	File ACCESS_JWT_PATH = new File("data/accessJwt.txt");
}
package constants;

import java.io.File;

/**
 * 環境設定.
 *
 * @author cyrus
 */
public interface Configurations {

	/**
	 * アクセストークンの出力先.
	 */
	File ACCESS_JWT_PATH = new File("data/accessJwt.txt");

	/**
	 * リフレッシュトークンの出力先.
	 */
	File REFRESH_JWT_PATH = new File("data/refreshJwt.txt");
}
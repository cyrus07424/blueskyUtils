package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.atproto.server.ServerRefreshSessionResponse;
import bsky4j.api.entity.share.RefreshAuthRequest;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;

/**
 * セッションリフレッシュテスト.
 *
 * @author cyrus
 */
public class RefreshSessionExample {

	/**
	 * メイン.
	 *
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("■start.");

		// リフレッシュトークンをファイルから読み込み
		String refreshJwt = FileUtils.readFileToString(Configurations.REFRESH_JWT_PATH, StandardCharsets.UTF_8);

		try {
			// レスポンスを取得
			Response<ServerRefreshSessionResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.server().refreshSession(new RefreshAuthRequest(refreshJwt));

			// アクセストークンを取得
			String accessJwt = response.get().getAccessJwt();
			System.out.println("■accessJwt: " + accessJwt);

			// アクセストークンをファイルに出力
			FileUtils.write(Configurations.ACCESS_JWT_PATH, accessJwt, StandardCharsets.UTF_8);

			// リフレッシュトークンを取得
			refreshJwt = response.get().getRefreshJwt();
			System.out.println("■refreshJwt: " + refreshJwt);

			// リフレッシュトークンをファイルに出力
			FileUtils.write(Configurations.REFRESH_JWT_PATH, refreshJwt, StandardCharsets.UTF_8);
		} finally {
			System.out.println("■done.");
		}
	}
}
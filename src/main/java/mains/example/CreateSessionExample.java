package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.atproto.server.ServerCreateSessionRequest;
import bsky4j.api.entity.atproto.server.ServerCreateSessionResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;

/**
 * セッション作成テスト.
 *
 * @author cyrus
 */
public class CreateSessionExample {

	/**
	 * メイン.
	 *
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("■start.");

		// Scanner
		try (Scanner scanner = new Scanner(System.in)) {
			// ユーザー名を取得
			System.out.print("ユーザー名を入力してください: ");
			String identifier = scanner.nextLine();

			// パスワードを取得
			System.out.print("パスワードを入力してください: ");
			String password = scanner.nextLine();

			// レスポンスを取得
			Response<ServerCreateSessionResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.server().createSession(
							ServerCreateSessionRequest.builder()
									.identifier(identifier)
									.password(password)
									.build());

			// accessJwtを取得
			String accessJwt = response.get().getAccessJwt();
			System.out.println("■accessJwt: " + accessJwt);

			// accessJwtをファイルに出力
			FileUtils.write(Configurations.ACCESS_JWT_PATH, accessJwt, StandardCharsets.UTF_8);
		} finally {
			System.out.println("■done.");
		}
	}
}
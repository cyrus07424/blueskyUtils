package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.graph.GraphDeleteFollowRequest;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;

/**
 * フォロー解除テスト.
 *
 * @author cyrus
 */
public class DeleteFollowExample {

	/**
	 * メイン.
	 *
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("■start.");

		// アクセストークンをファイルから読み込み
		String accessJwt = FileUtils.readFileToString(Configurations.ACCESS_JWT_PATH, StandardCharsets.UTF_8);

		// Scanner
		try (Scanner scanner = new Scanner(System.in)) {
			// uriを取得
			System.out.print("uriを入力してください: ");
			String uri = scanner.nextLine();

			// レスポンスを取得
			Response<Void> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.graph().deleteFollow(
							GraphDeleteFollowRequest.builder()
									.accessJwt(accessJwt)
									.uri(uri)
									.build());
		} finally {
			System.out.println("■done.");
		}
	}
}
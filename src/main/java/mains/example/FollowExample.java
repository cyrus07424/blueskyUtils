package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.graph.GraphFollowRequest;
import bsky4j.api.entity.bsky.graph.GraphFollowResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;

/**
 * フォローテスト.
 *
 * @author cyrus
 */
public class FollowExample {

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
			// didを取得
			System.out.print("didを入力してください: ");
			String did = scanner.nextLine();

			// レスポンスを取得
			Response<GraphFollowResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.graph().follow(
							GraphFollowRequest.builder()
									.accessJwt(accessJwt)
									.subject(did)
									.build());

			String uri = response.get().getUri();
			System.out.println(uri);
		} finally {
			System.out.println("■done.");
		}
	}
}
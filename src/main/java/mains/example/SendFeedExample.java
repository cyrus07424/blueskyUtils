package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedPostRequest;
import bsky4j.api.entity.bsky.feed.FeedPostResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;

/**
 * 投稿テスト.
 *
 * @author cyrus
 */
public class SendFeedExample {

	/**
	 * メイン.
	 *
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("■start.");

		// accessJwtをファイルから読み込み
		String accessJwt = FileUtils.readFileToString(Configurations.ACCESS_JWT_PATH, StandardCharsets.UTF_8);

		// Scanner
		try (Scanner scanner = new Scanner(System.in)) {
			// 投稿内容を取得
			System.out.print("投稿内容を入力してください: ");
			String text = scanner.nextLine();

			// レスポンスを取得
			Response<FeedPostResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.feed().post(
							FeedPostRequest.builder()
									.accessJwt(accessJwt)
									.text(text)
									.build());
		} finally {
			System.out.println("■done.");
		}
	}
}
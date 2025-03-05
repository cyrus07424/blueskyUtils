package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedGetActorFeedsRequest;
import bsky4j.api.entity.bsky.feed.FeedGetActorFeedsResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;

/**
 * GetActorFeedsテスト.
 *
 * @author cyrus
 */
public class GetActorFeedsExample {

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
			// actorを取得
			System.out.print("actorを入力してください: ");
			String actor = scanner.nextLine();

			// レスポンスを取得
			Response<FeedGetActorFeedsResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.feed().getActorFeeds(
							FeedGetActorFeedsRequest.builder()
									.accessJwt(accessJwt)
									.actor(actor)
									.build());

			response.get().getFeeds().forEach(i -> {
				System.out.println(i.getDisplayName());
			});
		} finally {
			System.out.println("■done.");
		}
	}
}
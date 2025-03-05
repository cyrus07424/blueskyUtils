package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedLikeRequest;
import bsky4j.api.entity.bsky.feed.FeedLikeResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import bsky4j.model.atproto.repo.RepoStrongRef;
import constants.Configurations;

/**
 * いいねテスト.
 *
 * @author cyrus
 */
public class LikeExample {

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

			// cidを取得
			System.out.print("cidを入力してください: ");
			String cid = scanner.nextLine();

			RepoStrongRef ref = new RepoStrongRef(uri, cid);

			// レスポンスを取得
			Response<FeedLikeResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.feed().like(
							FeedLikeRequest.builder()
									.accessJwt(accessJwt)
									.subject(ref)
									.build());
		} finally {
			System.out.println("■done.");
		}
	}
}
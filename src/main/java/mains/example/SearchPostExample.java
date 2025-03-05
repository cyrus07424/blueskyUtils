package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedSearchPostsRequest;
import bsky4j.api.entity.bsky.feed.FeedSearchPostsResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;
import utils.DumpHelper;

/**
 * 投稿を検索テスト.
 *
 * @author cyrus
 */
public class SearchPostExample {

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
			// 検索キーワードを取得
			System.out.print("検索キーワードを入力してください: ");
			String q = scanner.nextLine();

			// レスポンスを取得
			Response<FeedSearchPostsResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.feed().searchPosts(
							FeedSearchPostsRequest.builder()
									.accessJwt(accessJwt)
									.q(q)
									.build());

			response.get().getPosts().forEach(DumpHelper::print);
		} finally {
			System.out.println("■done.");
		}
	}
}
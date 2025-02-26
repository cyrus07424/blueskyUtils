package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedGetAuthorFeedRequest;
import bsky4j.api.entity.bsky.feed.FeedGetAuthorFeedResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import bsky4j.model.bsky.embed.EmbedImagesView;
import bsky4j.model.bsky.embed.EmbedViewUnion;
import bsky4j.model.bsky.feed.FeedDefsPostView;
import bsky4j.model.bsky.feed.FeedPost;
import bsky4j.model.share.RecordUnion;
import constants.Configurations;

/**
 * GetAuthorFeedテスト.
 *
 * @author cyrus
 */
public class GetAuthorFeedExample {

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
			Response<FeedGetAuthorFeedResponse> feeds = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.feed().getAuthorFeed(
							FeedGetAuthorFeedRequest.builder()
									.accessJwt(accessJwt)
									.actor(actor)
									.build());

			feeds.get().getFeed().forEach(f -> {
				print(f.getPost());
			});
		} finally {
			System.out.println("■done.");
		}
	}

	/**
	 * 投稿を出力.
	 * 
	 * @param post
	 */
	private static void print(FeedDefsPostView post) {
		System.out.println("|POST|-----------------------------------------");
		System.out.println("URI> " + post.getUri());
		System.out.println("CID> " + post.getCid());

		if (post.getEmbed() != null) {
			EmbedViewUnion embed = post.getEmbed();
			if (embed instanceof EmbedImagesView) {
				System.out.println("ImageURL> " +
						((EmbedImagesView) embed)
								.getImages().get(0).getFullsize());
			}
		}

		RecordUnion record = post.getRecord();
		if (record instanceof FeedPost) {
			FeedPost feedMain = (FeedPost) record;
			System.out.println("TEXT> " + feedMain.getText());
		}
	}
}
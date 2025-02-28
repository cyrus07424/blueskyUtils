package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedGetTimelineRequest;
import bsky4j.api.entity.bsky.feed.FeedGetTimelineResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;
import utils.DumpHelper;

/**
 * タイムライン取得テスト.
 *
 * @author cyrus
 */
public class GetTimelineExample {

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

		try {
			// レスポンスを取得
			Response<FeedGetTimelineResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.feed().getTimeline(
							FeedGetTimelineRequest.builder()
									.accessJwt(accessJwt)
									.limit(10)
									.build());

			response.get().getFeed().forEach(f -> {
				DumpHelper.print(f.getPost());
			});
		} finally {
			System.out.println("■done.");
		}
	}
}
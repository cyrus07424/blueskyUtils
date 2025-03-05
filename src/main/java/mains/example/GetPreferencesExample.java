package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.actor.ActorGetPreferencesRequest;
import bsky4j.api.entity.bsky.actor.ActorGetPreferencesResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import bsky4j.model.bsky.actor.ActorDefsSavedFeedsPref;
import constants.Configurations;

/**
 * 設定取得テスト.
 *
 * @author cyrus
 */
public class GetPreferencesExample {

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
			Response<ActorGetPreferencesResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.actor().getPreferences(
							ActorGetPreferencesRequest.builder()
									.accessJwt(accessJwt)
									.build());

			response.get().getPreferences().forEach(s -> {
				if (s instanceof ActorDefsSavedFeedsPref) {
					((ActorDefsSavedFeedsPref) s).getSaved()
							.forEach(System.out::println);
				}
			});
		} finally {
			System.out.println("■done.");
		}
	}
}
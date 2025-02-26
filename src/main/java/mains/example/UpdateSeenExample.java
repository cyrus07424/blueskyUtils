package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.notification.NotificationUpdateSeenRequest;
import bsky4j.domain.Service;
import constants.Configurations;

/**
 * UpdateSeenテスト.
 *
 * @author cyrus
 */
public class UpdateSeenExample {

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
			BlueskyFactory.getInstance(
					Service.BSKY_SOCIAL.getUri())
					.notification()
					.updateSeen(NotificationUpdateSeenRequest
							.builder()
							.accessJwt(accessJwt)
							.build());
		} finally {
			System.out.println("■done.");
		}
	}
}
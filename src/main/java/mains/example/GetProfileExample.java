package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.actor.ActorGetProfileRequest;
import bsky4j.api.entity.bsky.actor.ActorGetProfileResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import constants.Configurations;
import utils.DumpHelper;

/**
 * プロフィール取得テスト.
 *
 * @author cyrus
 */
public class GetProfileExample {

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
			Response<ActorGetProfileResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.actor().getProfile(
							ActorGetProfileRequest.builder()
									.accessJwt(accessJwt)
									.actor(actor)
									.build());

			DumpHelper.print(response.get());
		} finally {
			System.out.println("■done.");
		}
	}
}
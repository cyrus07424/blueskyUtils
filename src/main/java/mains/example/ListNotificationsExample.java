package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.notification.NotificationListNotificationsRequest;
import bsky4j.api.entity.bsky.notification.NotificationListNotificationsResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import bsky4j.model.bsky.feed.FeedPost;
import constants.Configurations;
import utils.DumpHelper;

/**
 * ListNotificationsテスト.
 *
 * @author cyrus
 */
public class ListNotificationsExample {

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
			Response<NotificationListNotificationsResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.notification()
					.listNotifications(NotificationListNotificationsRequest
							.builder()
							.accessJwt(accessJwt)
							.build());

			response.get().getNotifications().forEach(it -> {
				System.out.println("|NOTIFICATION|-----------------------------------------");
				System.out.println("REASON> " + it.getReason());
				DumpHelper.print(it.getRecord());

				// リプライの場合
				if (StringUtils.equals(it.getReason(), "mention")) {
					// リプライ元のユーザー名を取得
					String handle = it.getAuthor().getHandle();
					System.out.println("HANDLE> " + handle);

					if (StringUtils.equals(it.getRecord().getType(), "app.bsky.feed.post")) {
						FeedPost feedPost = (FeedPost) it.getRecord();

						// テキストを取得
						String text = feedPost.getText();
						System.out.println("TEXT> " + text);
					}
				}
			});
		} finally {
			System.out.println("■done.");
		}
	}
}
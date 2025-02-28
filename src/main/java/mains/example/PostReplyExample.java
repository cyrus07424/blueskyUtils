package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedPostRequest;
import bsky4j.api.entity.bsky.feed.FeedPostResponse;
import bsky4j.api.entity.bsky.notification.NotificationListNotificationsRequest;
import bsky4j.api.entity.bsky.notification.NotificationListNotificationsResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import bsky4j.model.atproto.repo.RepoStrongRef;
import bsky4j.model.bsky.feed.FeedPost;
import bsky4j.model.bsky.feed.FeedPostReplyRef;
import constants.Configurations;

/**
 * リプライ投稿テスト.
 *
 * @author cyrus
 */
public class PostReplyExample {

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
			Response<NotificationListNotificationsResponse> response1 = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.notification()
					.listNotifications(NotificationListNotificationsRequest
							.builder()
							.accessJwt(accessJwt)
							.build());

			response1.get().getNotifications().forEach(notification -> {
				// リプライの場合
				if (StringUtils.equals(notification.getReason(), "mention")) {
					// リプライ元のユーザー名を取得
					String handle = notification.getAuthor().getHandle();
					System.out.println("HANDLE> " + handle);

					if (StringUtils.equals(notification.getRecord().getType(), "app.bsky.feed.post")) {
						FeedPost feedPost = (FeedPost) notification.getRecord();

						// テキストを取得
						String text = feedPost.getText();
						System.out.println("TEXT> " + text);

						// Scanner
						try (Scanner scanner = new Scanner(System.in)) {
							// 投稿内容を取得
							System.out.print("リプライ投稿内容を入力してください: ");
							String replyText = scanner.nextLine();

							RepoStrongRef rootRef = new RepoStrongRef(
									notification.getUri(),
									notification.getCid());

							RepoStrongRef parentRef = new RepoStrongRef(
									notification.getUri(),
									notification.getCid());

							FeedPostReplyRef reply = new FeedPostReplyRef();
							reply.setParent(parentRef);
							reply.setRoot(rootRef);

							// レスポンスを取得
							Response<FeedPostResponse> response2 = BlueskyFactory
									.getInstance(Service.BSKY_SOCIAL.getUri())
									.feed().post(
											FeedPostRequest.builder()
													.accessJwt(accessJwt)
													.text(replyText)
													.reply(reply)
													.build());
						}
					}
				}
			});
		} finally {
			System.out.println("■done.");
		}
	}
}
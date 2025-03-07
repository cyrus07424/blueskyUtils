package utils;

import bsky4j.model.bsky.actor.ActorDefsProfileView;
import bsky4j.model.bsky.actor.ActorDefsProfileViewDetailed;
import bsky4j.model.bsky.embed.EmbedImagesView;
import bsky4j.model.bsky.embed.EmbedViewUnion;
import bsky4j.model.bsky.feed.FeedDefsPostView;
import bsky4j.model.bsky.feed.FeedPost;
import bsky4j.model.share.RecordUnion;

/**
 * 出力ヘルパー.
 *
 * @author cyrus
 */
public class DumpHelper {

	/**
	 * 投稿を出力.
	 * 
	 * @param post
	 */
	public static void print(FeedDefsPostView post) {
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

	/**
	 * RecordUnionを出力.
	 * 
	 * @param post
	 */
	public static void print(RecordUnion record) {
		System.out.println("TYPE> " + record.getType());
	}

	/**
	 * ActorDefsProfileViewを出力.
	 * 
	 * @param post
	 */
	public static void print(ActorDefsProfileView user) {
		System.out.println("|USER|-----------------------------------------");
		System.out.println("DID> " + user.getDid());
		System.out.println("HANDLE> " + user.getHandle());
		System.out.println("NAME> " + user.getDisplayName());
	}

	/**
	 * ActorDefsProfileViewDetailedを出力.
	 * 
	 * @param post
	 */
	public static void print(ActorDefsProfileViewDetailed user) {
		System.out.println("|USER|-----------------------------------------");
		System.out.println("DID> " + user.getDid());
		System.out.println("HANDLE> " + user.getHandle());
		System.out.println("NAME> " + user.getDisplayName());
		System.out.println("FOLLOWS COUNT> " + user.getFollowsCount());
		System.out.println("FOLLOWERS COUNT> " + user.getFollowersCount());
	}
}
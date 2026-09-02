package mains.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.bsky.feed.FeedPostRequest;
import bsky4j.api.entity.bsky.feed.FeedPostResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import bsky4j.model.bsky.richtext.RichtextFacet;
import bsky4j.model.bsky.richtext.RichtextFacetByteSlice;
import bsky4j.model.bsky.richtext.RichtextFacetFeatureUnion;
import bsky4j.model.bsky.richtext.RichtextFacetLink;
import bsky4j.model.bsky.richtext.RichtextFacetTag;
import constants.Configurations;

/**
 * 投稿テスト.
 *
 * @author cyrus
 */
public class PostExample {

	/**
	 * テキスト中の文字(char)インデックスを、UTF-8バイト単位のインデックスに変換する.
	 * <p>
	 * AT ProtocolのFacetはbyteStart/byteEndにUTF-8バイトオフセットを要求するため、
	 * 日本語など複数バイト文字が混在するテキストではJavaのchar indexをそのまま使えない。
	 * </p>
	 *
	 * @param text       投稿テキスト
	 * @param charIndex  文字(char)インデックス
	 * @return UTF-8バイトインデックス
	 */
	private static int charIndexToByteIndex(String text, int charIndex) {
		return text.substring(0, charIndex).getBytes(StandardCharsets.UTF_8).length;
	}

	/**
	 * URLとハッシュタグを検出してFacetを作成.
	 *
	 * @param text 投稿テキスト
	 * @return Facetのリスト
	 */
	private static List<RichtextFacet> createFacets(String text) {
		List<RichtextFacet> facets = new ArrayList<>();

		// URLの正規表現パターン
		Pattern urlPattern = Pattern.compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");
		Matcher urlMatcher = urlPattern.matcher(text);

		// URLを検出
		while (urlMatcher.find()) {
			String url = urlMatcher.group();
			int byteStart = charIndexToByteIndex(text, urlMatcher.start());
			int byteEnd = charIndexToByteIndex(text, urlMatcher.end());

			// インデックスを作成(UTF-8バイトオフセット)
			RichtextFacetByteSlice index = new RichtextFacetByteSlice();
			index.setByteStart(byteStart);
			index.setByteEnd(byteEnd);

			// リンク機能を作成
			RichtextFacetLink linkFeature = new RichtextFacetLink();
			linkFeature.setUri(url);

			// Facetを作成
			RichtextFacet facet = new RichtextFacet();
			facet.setIndex(index);
			facet.setFeatures(new ArrayList<RichtextFacetFeatureUnion>() {
				{
					add(linkFeature);
				}
			});

			facets.add(facet);
		}

		// ハッシュタグの正規表現パターン
		Pattern hashtagPattern = Pattern.compile("#[\\w\\p{L}\\p{N}\\p{M}_-]+");
		Matcher hashtagMatcher = hashtagPattern.matcher(text);

		// ハッシュタグを検出
		while (hashtagMatcher.find()) {
			String hashtag = hashtagMatcher.group();
			int byteStart = charIndexToByteIndex(text, hashtagMatcher.start());
			int byteEnd = charIndexToByteIndex(text, hashtagMatcher.end());

			// インデックスを作成(UTF-8バイトオフセット)
			RichtextFacetByteSlice index = new RichtextFacetByteSlice();
			index.setByteStart(byteStart);
			index.setByteEnd(byteEnd);

			// タグ機能を作成
			RichtextFacetTag tagFeature = new RichtextFacetTag();
			tagFeature.setTag(hashtag.substring(1)); // #を除いたタグ名

			// Facetを作成
			RichtextFacet facet = new RichtextFacet();
			facet.setIndex(index);
			facet.setFeatures(new ArrayList<RichtextFacetFeatureUnion>() {
				{
					add(tagFeature);
				}
			});

			facets.add(facet);
		}

		return facets;
	}

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
			// 投稿内容を取得
			System.out.print("投稿内容を入力してください: ");
			String text = scanner.nextLine();

			// Facetを作成
			List<RichtextFacet> facets = createFacets(text);

			// FeedPostRequest を構築
			FeedPostRequest.FeedPostRequestBuilder builder = FeedPostRequest.builder()
					.accessJwt(accessJwt)
					.text(text);

			// facetsをサポートしているか確認して追加
			if (facets.size() > 0) {
				builder.facets(facets);
			}

			// レスポンスを取得
			Response<FeedPostResponse> response = BlueskyFactory
					.getInstance(Service.BSKY_SOCIAL.getUri())
					.feed().post(builder.build());

			System.out.println("投稿成功: " + response.get().getUri());
		} finally {
			System.out.println("■done.");
		}
	}
}
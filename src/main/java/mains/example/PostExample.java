package mains.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import bsky4j.Bluesky;
import bsky4j.BlueskyFactory;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobRequest;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobResponse;
import bsky4j.api.entity.bsky.feed.FeedPostRequest;
import bsky4j.api.entity.bsky.feed.FeedPostResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import bsky4j.model.bsky.embed.EmbedExternal;
import bsky4j.model.bsky.embed.EmbedExternalExternal;
import bsky4j.model.bsky.embed.EmbedUnion;
import bsky4j.model.bsky.richtext.RichtextFacet;
import bsky4j.model.bsky.richtext.RichtextFacetByteSlice;
import bsky4j.model.bsky.richtext.RichtextFacetFeatureUnion;
import bsky4j.model.bsky.richtext.RichtextFacetLink;
import bsky4j.model.bsky.richtext.RichtextFacetTag;
import bsky4j.model.share.Blob;
import constants.Configurations;

/**
 * 投稿テスト.
 *
 * @author cyrus
 */
public class PostExample {

	/** HTTP接続/読み取りのタイムアウト(ミリ秒). */
	private static final int HTTP_TIMEOUT_MILLIS = 10000;

	/** サムネイル画像の最大バイト数(Blueskyのアップロード上限に合わせる). */
	private static final int MAX_THUMBNAIL_BYTES = 1_000_000;

	/** OGPページ取得時に読み込む最大バイト数(過大なページを無制限に読み込まないため). */
	private static final int MAX_HTML_BYTES = 500_000;

	/** OGP等の取得時に付与するUser-Agent(bot判定でブロックされにくくするため). */
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; blueskyUtils/1.0; +https://github.com/cyrus07424/blueskyUtils)";

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

		// Blueskyクライアント(投稿・Blobアップロードで使い回す)
		Bluesky bluesky = BlueskyFactory.getInstance(Service.BSKY_SOCIAL.getUri());

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
			if (0 < facets.size()) {
				builder.facets(facets);
			}

			// テキストにURLが含まれる場合、リンクカード(external embed)を作成して追加
			String firstUrl = extractFirstUrl(text);
			if (firstUrl != null) {
				EmbedUnion embed = createExternalEmbed(bluesky, accessJwt, firstUrl);
				if (embed != null) {
					builder.embed(embed);
				}
			}

			// レスポンスを取得
			Response<FeedPostResponse> response = bluesky
					.feed().post(builder.build());

			System.out.println("投稿成功: " + response.get().getUri());
		} finally {
			System.out.println("■done.");
		}
	}

	/**
	 * URLのOGPメタデータ(タイトル・説明・画像URL).
	 */
	private static class OgpMetadata {
		private final String title;
		private final String description;
		private final String imageUrl;

		OgpMetadata(String title, String description, String imageUrl) {
			this.title = title;
			this.description = description;
			this.imageUrl = imageUrl;
		}
	}

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
	 * テキスト中から最初に出現するURLを取得する(リンクカード埋め込み対象).
	 *
	 * @param text 投稿テキスト
	 * @return 最初のURL。見つからない場合はnull
	 */
	private static String extractFirstUrl(String text) {
		Pattern urlPattern = Pattern.compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");
		Matcher urlMatcher = urlPattern.matcher(text);
		if (urlMatcher.find()) {
			return urlMatcher.group();
		}
		return null;
	}

	/**
	 * HTMLエンティティ参照を実体文字にデコードする(簡易版).
	 * <p>
	 * OGPのtitle/description等に含まれる代表的な数値参照・名前付き参照のみをデコードする。
	 * </p>
	 *
	 * @param text デコード対象文字列
	 * @return デコード後の文字列
	 */
	private static String unescapeHtmlEntities(String text) {
		if (text == null) {
			return null;
		}
		String result = text
				.replace("&amp;", "&")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#39;", "'")
				.replace("&apos;", "'")
				.replace("&nbsp;", " ");

		// 数値文字参照(&#12345; / &#x1F600;)をデコード
		Matcher numericMatcher = Pattern.compile("&#(x?)([0-9a-fA-F]+);").matcher(result);
		StringBuffer sb = new StringBuffer();
		while (numericMatcher.find()) {
			try {
				int codePoint = "x".equalsIgnoreCase(numericMatcher.group(1))
						? Integer.parseInt(numericMatcher.group(2), 16)
						: Integer.parseInt(numericMatcher.group(2));
				numericMatcher.appendReplacement(sb,
						Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
			} catch (RuntimeException e) {
				numericMatcher.appendReplacement(sb, Matcher.quoteReplacement(numericMatcher.group()));
			}
		}
		numericMatcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * HTML内から指定したmeta property/nameのcontent属性値を取得する.
	 * <p>
	 * 属性の並び順(property→content / content→property)どちらにも対応する。
	 * </p>
	 *
	 * @param html         HTML文字列
	 * @param propertyName og:title 等のproperty/name値
	 * @return content属性値。見つからない場合はnull
	 */
	private static String extractMetaContent(String html, String propertyName) {
		String quoted = Pattern.quote(propertyName);
		String[] patterns = {
				"<meta[^>]*?(?:property|name)=[\"']" + quoted + "[\"'][^>]*?content=[\"']([^\"']*)[\"'][^>]*?>",
				"<meta[^>]*?content=[\"']([^\"']*)[\"'][^>]*?(?:property|name)=[\"']" + quoted + "[\"'][^>]*?>"
		};

		for (String pattern : patterns) {
			Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html);
			if (matcher.find()) {
				return unescapeHtmlEntities(matcher.group(1));
			}
		}
		return null;
	}

	/**
	 * URLからHTMLを取得し、OGPメタデータ(og:title/og:description/og:image)を抽出する.
	 * <p>
	 * ogタグが無い場合は&lt;title&gt;タグをタイトルの代替として使用する。
	 * </p>
	 *
	 * @param url 対象URL
	 * @return OGPメタデータ
	 * @throws IOException 取得に失敗した場合
	 */
	private static OgpMetadata fetchOgpMetadata(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setConnectTimeout(HTTP_TIMEOUT_MILLIS);
		connection.setReadTimeout(HTTP_TIMEOUT_MILLIS);
		connection.setInstanceFollowRedirects(true);

		String html;
		try (InputStream in = connection.getInputStream()) {
			html = readLimited(in, MAX_HTML_BYTES, StandardCharsets.UTF_8);
		} finally {
			connection.disconnect();
		}

		String title = extractMetaContent(html, "og:title");
		if (title == null) {
			Matcher titleMatcher = Pattern
					.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
					.matcher(html);
			if (titleMatcher.find()) {
				title = unescapeHtmlEntities(titleMatcher.group(1).trim());
			}
		}

		String description = extractMetaContent(html, "og:description");
		if (description == null) {
			description = extractMetaContent(html, "description");
		}

		String imageUrl = extractMetaContent(html, "og:image");

		return new OgpMetadata(
				title != null ? title : url,
				description != null ? description : "",
				imageUrl);
	}

	/**
	 * InputStreamから、指定バイト数を上限として文字列を読み込む.
	 *
	 * @param in       入力ストリーム
	 * @param maxBytes 読み込む最大バイト数
	 * @param charset  文字コード
	 * @return 読み込んだ文字列
	 * @throws IOException 読み込みに失敗した場合
	 */
	private static String readLimited(InputStream in, int maxBytes, java.nio.charset.Charset charset)
			throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int read;
		int total = 0;
		while (total < maxBytes && (read = in.read(chunk)) != -1) {
			int toWrite = Math.min(read, maxBytes - total);
			buffer.write(chunk, 0, toWrite);
			total += toWrite;
		}
		return buffer.toString(charset.name());
	}

	/**
	 * 画像URLから画像本体を取得する(サムネイル用).
	 * <p>
	 * サイズが上限を超える場合や取得に失敗した場合はnullを返す(サムネイル無しで埋め込みを継続するため)。
	 * </p>
	 *
	 * @param imageUrl 画像URL
	 * @return 画像バイト列とMIMEタイプ。取得できない場合はnull
	 */
	private static Thumbnail downloadThumbnail(String imageUrl) {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(imageUrl).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setConnectTimeout(HTTP_TIMEOUT_MILLIS);
			connection.setReadTimeout(HTTP_TIMEOUT_MILLIS);
			connection.setInstanceFollowRedirects(true);

			try (InputStream in = connection.getInputStream()) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] chunk = new byte[8192];
				int read;
				int total = 0;
				while ((read = in.read(chunk)) != -1) {
					total += read;
					if (total > MAX_THUMBNAIL_BYTES) {
						// 上限超過。サムネイル無しで続行する
						return null;
					}
					buffer.write(chunk, 0, read);
				}

				String contentType = connection.getContentType();
				String extension = mimeTypeToExtension(contentType);

				return new Thumbnail(buffer.toByteArray(), extension);
			}
		} catch (IOException e) {
			// サムネイル取得失敗時は無しで続行する
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * Content-TypeヘッダからBlobアップロード用のファイル拡張子を決定する.
	 * <p>
	 * bsky4j 0.5.3のBlobアップロードは、送信するファイル名の拡張子(gif/png/jpg/jpeg)から
	 * Content-Typeを決定する実装になっているため、正しい拡張子を持つファイル名を使う必要がある。
	 * 認識できない場合はjpgとして扱う。
	 * </p>
	 *
	 * @param contentType HTTPレスポンスのContent-Typeヘッダ値
	 * @return 拡張子(gif/png/jpgのいずれか)
	 */
	private static String mimeTypeToExtension(String contentType) {
		if (contentType != null) {
			String lower = contentType.toLowerCase();
			if (lower.contains("image/png")) {
				return "png";
			}
			if (lower.contains("image/gif")) {
				return "gif";
			}
			if (lower.contains("image/jpeg") || lower.contains("image/jpg")) {
				return "jpg";
			}
		}
		return "jpg";
	}

	/**
	 * サムネイル画像のバイト列と拡張子.
	 */
	private static class Thumbnail {
		private final byte[] bytes;
		private final String extension;

		Thumbnail(byte[] bytes, String extension) {
			this.bytes = bytes;
			this.extension = extension;
		}
	}

	/**
	 * URLからリンクカード(external embed)を作成する.
	 * <p>
	 * OGPメタデータを取得し、サムネイル画像があればアップロードして埋め込みに含める。
	 * 取得に失敗した場合はnullを返し、呼び出し元は埋め込み無しで投稿を継続する。
	 * </p>
	 *
	 * @param bluesky   Blueskyクライアント(サムネイルアップロードに使用)
	 * @param accessJwt アクセストークン
	 * @param url       対象URL
	 * @return リンクカードのEmbedUnion。作成できない場合はnull
	 */
	private static EmbedUnion createExternalEmbed(Bluesky bluesky, String accessJwt, String url) {
		OgpMetadata metadata;
		try {
			metadata = fetchOgpMetadata(url);
		} catch (IOException e) {
			// OGP取得失敗時はリンクカードを付けずに投稿を継続する
			System.out.println("リンクカード用メタデータの取得に失敗しました: " + url + " (" + e.getMessage() + ")");
			return null;
		}

		Blob thumb = null;
		if (metadata.imageUrl != null) {
			Thumbnail thumbnail = downloadThumbnail(metadata.imageUrl);
			if (thumbnail != null) {
				try {
					// ファイル名の拡張子でContent-Typeが決まるため、実際の画像形式に合わせた拡張子を付与する
					Response<RepoUploadBlobResponse> uploadResponse = bluesky.repo().uploadBlob(
							RepoUploadBlobRequest.fromStreamBuilder()
									.stream(new java.io.ByteArrayInputStream(thumbnail.bytes))
									.name("thumbnail." + thumbnail.extension)
									.accessJwt(accessJwt)
									.build());
					thumb = uploadResponse.get().getBlob();
				} catch (Exception e) {
					// サムネイルアップロード失敗時はサムネイル無しで続行する
					System.out.println("サムネイルのアップロードに失敗しました: " + e.getMessage());
				}
			}
		}

		EmbedExternalExternal external = new EmbedExternalExternal();
		external.setUri(url);
		external.setTitle(metadata.title);
		external.setDescription(metadata.description);
		if (thumb != null) {
			external.setThumb(thumb);
		}

		EmbedExternal embed = new EmbedExternal();
		embed.setExternal(external);

		return embed;
	}
}
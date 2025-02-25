package mains.example;

import java.io.IOException;
import java.util.Scanner;

import bsky4j.PLCDirectoryFactory;
import bsky4j.api.entity.share.Response;
import bsky4j.model.plc.DIDDetails;

/**
 * PLC Directoryテスト.
 *
 * @author cyrus
 */
public class PlcDirectoryExample {

	/**
	 * メイン.
	 *
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("■start.");

		// Scanner
		try (Scanner scanner = new Scanner(System.in)) {
			// didを取得
			System.out.print("didを入力してください: ");
			String did = scanner.nextLine();

			Response<DIDDetails> response = PLCDirectoryFactory
					.getInstance().getDIDDetails(did);
			System.out.println(response.get().getAlsoKnownAs().get(0));
		} finally {
			System.out.println("■done.");
		}
	}
}
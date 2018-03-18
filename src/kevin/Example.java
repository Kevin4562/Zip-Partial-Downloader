package kevin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;

public class Example {

	public static void main(String[] args) throws FileNotFoundException {
		try {

			// Construct a ZipDownload with URL to direct download. Supports an optional
			// UserAgent string.
			ZipDownload onlineZip = new ZipDownload("https://www.dropbox.com/s/05tiv8j86f1tvns/example.zip?dl=1");

			// Files are listed from Zip's Central Directory and are not downloaded.
			for (ZippedFile file : onlineZip.getAllFiles()) {
				System.out.println(
						file.getFileName() + "-" + file.getCompressedSize() + "/" + file.getUncompressedSize());
			}

			ZippedFile fileDownloadExample = onlineZip.getFileByName("exampleDirectory/example2.txt");
			BufferedReader br = new BufferedReader(onlineZip.getFileInputStreamReader(fileDownloadExample));
			String input;
			while ((input = br.readLine()) != null) {
				System.out.println(input);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package kevin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

public class ZipDownload {

	ArrayList<ZippedFile> files = new ArrayList<ZippedFile>();

	String link;

	String userAgent = null;

	public ZipDownload(String url) {
		this.link = url;
		this.files = this.getFileList(url);
	}

	public ZipDownload(String url, String userAgent) {
		this.link = url;
		this.userAgent = userAgent;
		this.files = this.getFileList(url);
	}

	public ZippedFile getFileByName(String fileName) {
		for (ZippedFile file : files) {
			if (file.getFileName().equals(fileName)) {
				return file;
			}
		}
		return null;
	}
	
	public ZippedFile getFileByDisplayName(String fileName) {
		for (ZippedFile file : files) {
			if (file.getFileDisplayName().equalsIgnoreCase(fileName)) {
				return file;
			}
		}
		return null;
	}
	
	public ArrayList<ZippedFile> getFilesInDirectory(String dir) {
		ArrayList<ZippedFile> files = new ArrayList<ZippedFile>();
		for(ZippedFile file : this.files) {
			if(file.getFilePath().equalsIgnoreCase(dir)) {
				files.add(file);
			}
		}
		return files;
	}

	public ArrayList<ZippedFile> getAllFiles() {
		return this.files;
	}

	public ArrayList<ZippedFile> getFilesByType(String type) {
		ArrayList<ZippedFile> files = new ArrayList<ZippedFile>();
		for (ZippedFile file : this.files) {
			if (file.getFileName().contains(type)) {
				files.add(file);
			}
		}
		return files;
	}

	public byte[] downloadFile(ZippedFile z) {
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		if (this.files.contains(z)) {
			if (z.isProcessed()) {
				try {
					URL url = new URL(link);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Range", "bytes=" + z.getByteLower() + "-" + z.getByteUpper());
					if (this.userAgent != null) {
						connection.setRequestProperty("User-Agent", this.userAgent);
					}
					connection.connect();
					ZipInputStream zin = new ZipInputStream(connection.getInputStream());
					byte[] buffer = new byte[1024];
					zin.getNextEntry();
					int len;
					while ((len = zin.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			} else {
				System.out.println("ZippedFile is not processed: " + this.link);
			}
		} else {
			System.out.println("Zip does not contain file.");
		}
		return fos.toByteArray();
	}
	
	public InputStreamReader getFileInputStreamReader(ZippedFile z) {
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		if (this.files.contains(z)) {
			if (z.isProcessed()) {
				try {
					URL url = new URL(link);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Range", "bytes=" + z.getByteLower() + "-" + z.getByteUpper());
					if (this.userAgent != null) {
						connection.setRequestProperty("User-Agent", this.userAgent);
					}
					connection.connect();
					ZipInputStream zin = new ZipInputStream(connection.getInputStream());
					byte[] buffer = new byte[1024];
					zin.getNextEntry();
					int len;
					while ((len = zin.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			} else {
				System.out.println("ZippedFile is not processed: " + this.link);
			}
		} else {
			System.out.println("Zip does not contain file.");
		}
		return new InputStreamReader(new ByteArrayInputStream(fos.toByteArray()));
	}

	private ArrayList<ZippedFile> getFileList(String link) {
		ArrayList<ZippedFile> files = new ArrayList<ZippedFile>();
		try {
			if (link != null) {
				URL url = new URL(link);
				int length = getLength(url);
				if (length > 0) {
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Range", "bytes=-" + length);
					if (this.userAgent != null) {
						connection.setRequestProperty("User-Agent", this.userAgent);
					}
					byte[] bytes = IOUtils.toByteArray(connection.getInputStream());
					for (int i = 0; i < bytes.length; i++) {
						if (bytes[i] == 80 && bytes[i + 1] == 75 && bytes[i + 2] == 01 && bytes[i + 3] == 02) {
							ZippedFile file = new ZippedFile();
							file.process(bytes, i);
							files.add(file);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return files;
	}

	private int getLength(URL url) {
		int length = 0;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			int fileSize = url.openConnection().getContentLength();
			int firstDL = fileSize - 2000;
			connection.setRequestProperty("Range", "bytes=" + firstDL + "-");
			if (this.userAgent != null) {
				connection.setRequestProperty("User-Agent", this.userAgent);
			}
			connection.connect();
			byte[] bytes = IOUtils.toByteArray(connection.getInputStream());
			for (int i = 0; i < bytes.length; i++) {
				if (bytes[i] == 80 && bytes[i + 1] == 75 && bytes[i + 2] == 05 && bytes[i + 3] == 06) {
					int offset = getFourByteInt(bytes, i, 16);
					return (fileSize - offset);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return length;
	}
	
	private int getFourByteInt(byte[] bytes, int i, int u) {
		int f1 = bytes[i + u] & 0xff;
		int f2 = bytes[i + (u + 1)] & 0xff;
		int f3 = bytes[i + (u + 2)] & 0xff;
		int f4 = bytes[i + (u + 3)] & 0xff;
		return (f4 << 24) | (f3 << 16) | (f2 << 8) | (f1);
	}

}

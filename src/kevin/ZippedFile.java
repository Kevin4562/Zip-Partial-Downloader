package kevin;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ZippedFile {

	int generalFlag;

	int versionMade;
	int versionMin;

	int byteUpper;
	int byteLower;

	int diskNumber;

	int compressionMethod;

	long fileCRC;
	int compressedSize;
	int uncompressedSize;

	int fileLastModifiedTime;
	int fileLastModifiedDate;

	int fileExtraFieldLength;

	int fileNameLength;

	int offset;

	int fileCommentLength;
	String fileName;
	String fileComment;

	boolean processed;

	public ZippedFile() {
		this.processed = false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (processed) {
			sb.append("======================" + System.getProperty("line.separator"));
			sb.append("FILENAME >> " + this.fileName + System.getProperty("line.separator"));
			sb.append("FILE LOCATION >> (" + this.byteLower + "," + this.byteUpper + ")"
					+ System.getProperty("line.separator"));
			sb.append("COMPRESSED SIZE >> " + this.compressedSize + " ("
					+ humanReadableByteCount(this.compressedSize, false) + ")" + System.getProperty("line.separator"));
			sb.append("UNCOMPRESSED SIZE >> " + this.uncompressedSize + " ("
					+ humanReadableByteCount(this.uncompressedSize, false) + ")"
					+ System.getProperty("line.separator"));
			sb.append("COMPRESSION METHOD >> " + this.compressionMethod + System.getProperty("line.separator"));
			sb.append("CRC32 >> " + this.fileCRC + System.getProperty("line.separator"));
			sb.append("VERSIONMADE >> " + this.versionMade + System.getProperty("line.separator"));
			sb.append("VERSIONMIN >> " + this.versionMin + System.getProperty("line.separator"));
			sb.append("GENERALFLAG >> " + this.generalFlag + System.getProperty("line.separator"));
			sb.append("FILE LAST MODIFICATION TIME >> " + this.fileLastModifiedTime
					+ System.getProperty("line.separator"));
			sb.append("FILE LAST MODIFICATION DATE >> " + this.fileLastModifiedDate
					+ System.getProperty("line.separator"));
			sb.append("FILECOMMENT >> " + this.fileComment + System.getProperty("line.separator"));
			sb.append("DISK NUMBER >> " + this.diskNumber + System.getProperty("line.separator"));
			sb.append("OFFSET >> " + this.offset + System.getProperty("line.separator"));
			sb.append("======================");
		} else {
			sb.append("FILE HAS NOT BEEN PROCESSED.");
		}
		return sb.toString();
	}

	public void process(byte[] bytes, int i) {
		try {
			this.offset = i;

			this.fileNameLength = getTwoByteInt(bytes, i, 28);
			this.fileName = getByteStringWithLength(bytes, i, 46, this.fileNameLength);

			this.fileExtraFieldLength = getTwoByteInt(bytes, i, 30);

			this.fileCommentLength = getTwoByteInt(bytes, i, 32);
			this.fileComment = getByteStringWithLength(bytes, i, 46 + this.fileNameLength + this.fileExtraFieldLength,
					this.fileCommentLength);

			this.fileCRC = getFourByteLong(bytes, i, 16);

			this.compressedSize = getFourByteInt(bytes, i, 20);
			this.uncompressedSize = getFourByteInt(bytes, i, 24);
			this.compressionMethod = getTwoByteInt(bytes, i, 10);

			this.byteLower = getFourByteInt(bytes, i, 42);
			this.byteUpper = this.byteLower + this.compressedSize
					+ (29 + this.fileNameLength + this.fileExtraFieldLength);

			this.versionMade = getTwoByteInt(bytes, i, 4);
			this.versionMin = getTwoByteInt(bytes, i, 6);

			this.generalFlag = getTwoByteInt(bytes, i, 8);

			this.fileLastModifiedTime = getTwoByteInt(bytes, i, 12);
			this.fileLastModifiedDate = getTwoByteInt(bytes, i, 14);

			this.diskNumber = getTwoByteInt(bytes, i, 34);

			this.processed = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	private String getByteStringWithLength(byte[] bytes, int i, int t, int length) throws UnsupportedEncodingException {
		List<Byte> myBytesFin = new ArrayList<Byte>();
		int counter = 0;
		for (int j = i + t; j < (i + t + length); j++) {
			if (j == bytes.length) {
				break;
			}
			if (counter != length) {
				myBytesFin.add(bytes[j]);

				counter++;
			}
		}

		byte[] array = new byte[myBytesFin.size()];
		int x = 0;
		for (Byte p : myBytesFin) {
			array[x] = p;
			x++;
		}
		if (array.length > 1) {
			String decoded = new String(array, "UTF-8");
			return decoded;
		}

		return null;
	}

	private int getFourByteInt(byte[] bytes, int i, int u) {
		int f1 = bytes[i + u] & 0xff;
		int f2 = bytes[i + (u + 1)] & 0xff;
		int f3 = bytes[i + (u + 2)] & 0xff;
		int f4 = bytes[i + (u + 3)] & 0xff;
		return (f4 << 24) | (f3 << 16) | (f2 << 8) | (f1);
	}

	public static long byteAsULong(byte b) {
	    return ((long)b) & 0x00000000000000FFL; 
	}

	public static long getFourByteLong(byte[] bytes, int i, int u) {
	    long value = byteAsULong(bytes[i + u]) | (byteAsULong(bytes[i + (u + 1)]) << 8) | (byteAsULong(bytes[i + (u + 2)]) << 16) | (byteAsULong(bytes[i + (u + 3)]) << 24);
	    return value;
	}

	private int getTwoByteInt(byte[] bytes, int i, int u) {
		byte f1 = bytes[i + u];
		byte f2 = bytes[i + (u + 1)];

		int byteInt1 = f1 & 0xff;
		int byteInt2 = (f2 & 0xff) * 256;
		return byteInt1 + byteInt2;
	}

	public String getFilePath() {
		if (this.fileName.contains("/")) {
			return this.fileName.substring(0, this.fileName.lastIndexOf("/"));
		}
		return "";
	}

	public String getFileDisplayName() {
		if (this.fileName.contains("/")) {
			return this.fileName.substring(this.fileName.lastIndexOf("/") + 1, this.fileName.length());
		}
		return this.fileName;
	}

	public int getGeneralFlag() {
		return generalFlag;
	}

	public int getVersionMade() {
		return versionMade;
	}

	public int getVersionMin() {
		return versionMin;
	}

	public int getByteUpper() {
		return byteUpper;
	}

	public int getByteLower() {
		return byteLower;
	}

	public int getCompressionMethod() {
		return compressionMethod;
	}

	public long getFileCRC() {
		return fileCRC;
	}

	public int getCompressedSize() {
		return compressedSize;
	}

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	public int getFileLastModifiedTime() {
		return fileLastModifiedTime;
	}

	public int getFileLastModifiedDate() {
		return fileLastModifiedDate;
	}

	public int getFileExtraFieldLength() {
		return fileExtraFieldLength;
	}

	public int getFileNameLength() {
		return fileNameLength;
	}

	public int getFileCommentLength() {
		return fileCommentLength;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileComment() {
		return fileComment;
	}

	public boolean isProcessed() {
		return processed;
	}
}

package rocks.inspectit.statistics.downloads;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CSVSource implements IDataSource {
	private static final char CSV_SEPARATOR = ',';

	private final String fileName;

	/**
	 * @param fileName
	 */
	public CSVSource(String fileName) {
		this.fileName = fileName;
	}

	public String getCSVHeader() {
		String result = Constants.TIMESTAMP;
		result += CSV_SEPARATOR + Constants.ARTIFACT_TAG;
		result += CSV_SEPARATOR + Constants.OS_TAG;
		result += CSV_SEPARATOR + Constants.ARCHITECTURE_TAG;
		result += CSV_SEPARATOR + Constants.MAJOR_VERSION_TAG;
		result += CSV_SEPARATOR + Constants.MINOR_VERSION_TAG;
		result += CSV_SEPARATOR + Constants.BUILD_NR_TAG;
		result += CSV_SEPARATOR + Constants.COUNT_FIELD;

		return result;
	}

	public void store(Collection<DownloadStatistics> statistics) {
		File csvFile = new File(fileName);
		if (csvFile.exists()) {
			csvFile.delete();
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {

			writer.write(getCSVHeader());
			writer.newLine();

			for (DownloadStatistics stat : statistics) {

				writer.write(toCSVString(stat));

				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<DownloadStatistics> load() {
		File csvFile = new File(fileName);
		if (!csvFile.exists()) {
			throw new IllegalArgumentException("File " + fileName + " does not exist!");
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
			List<DownloadStatistics> resultList = new ArrayList<DownloadStatistics>();
			String line = reader.readLine();
			while (null != line) {
				if (!line.equals(getCSVHeader())) {
					DownloadStatistics stat = fromCSVString(line);
					resultList.add(stat);
				}
				line = reader.readLine();
			}
			return resultList;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public DownloadStatistics fromCSVString(String string) {

		String[] strArray = string.split(",");

		return new DownloadStatistics(Long.parseLong(strArray[0]), strArray[1], strArray[2], strArray[3], strArray[4], strArray[5], strArray[6], Integer.parseInt(strArray[7]));
	}

	public String toCSVString(DownloadStatistics statistics) {
		return String.valueOf(statistics.getTimestamp()) + CSV_SEPARATOR + statistics.getArtifactType() + CSV_SEPARATOR + statistics.getOs() + CSV_SEPARATOR + statistics.getArchitecture()
				+ CSV_SEPARATOR + statistics.getMajorVersion() + CSV_SEPARATOR + statistics.getMinorVersion() + CSV_SEPARATOR + statistics.getBuildNr() + CSV_SEPARATOR
				+ String.valueOf(statistics.getDownloadCount());
	}

	@Override
	public int getAbsoluteCounts(long since, Identifier identifier) {
		// TODO Auto-generated method stub
		return 0;
	}
}

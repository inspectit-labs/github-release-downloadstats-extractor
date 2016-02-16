package rocks.inspectit.statistics.source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;

public class CSVFTPSource<T extends AbstractStatisticsEntity> extends CSVSource<T> {
	private static final char CSV_SEPARATOR = ',';
	private static final String COMMA_REPLACEMENT = "#*&";

	private final String user;
	private final String password;
	private final String host;
	private final String directory;
	private final FTPClient ftpClient;

	/**
	 * @param fileName
	 * @param user
	 * @param password
	 * @param host
	 * @param directory
	 */
	public CSVFTPSource(String fileName, String user, String password, String host, String directory) {
		super(fileName);
		this.user = user;
		this.password = password;
		this.host = host;
		this.directory = directory;

		ftpClient = new FTPClient();
	}

	@Override
	public void store(Collection<T> statistics) {
		try {
			ftpClient.connect(host);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(directory);
			File emptyFile = new File("empty.txt");
			emptyFile.createNewFile();
			InputStream inStream = ftpClient.retrieveFileStream(getFileName());
			boolean writeHeader = false;

			if (null == inStream) {
				writeHeader = true;
				FileInputStream fis = new FileInputStream(emptyFile);
				ftpClient.storeFile(getFileName(), fis);
				fis.close();
			} else {
				while (inStream.read() >= 0) {
					// read, otherwise ftp hangs
				}
				inStream.close();

				ftpClient.completePendingCommand();
			}

			try (OutputStream outStream = ftpClient.appendFileStream(getFileName())) {
				if(null == outStream){
					System.out.println(ftpClient.getReplyString());
				}
				storeToStream(statistics, outStream, writeHeader);
				ftpClient.completePendingCommand();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private void storeToStream(Collection<T> statistics, OutputStream outStream, boolean writeHeader) {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream))) {

			boolean first = true;
			for (T stat : statistics) {
				if (first && writeHeader) {
					writer.write(getCSVHeader(stat));
					writer.newLine();
				}
				writer.write(toCSVString(stat));

				writer.newLine();
				first = false;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> load(T template) {
		try {
			ftpClient.connect(host);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(directory);

			InputStream inStream = ftpClient.retrieveFileStream(getFileName());

			if (null == inStream) {
				ftpClient.disconnect();
				return Collections.emptyList();
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(inStream))) {
				List<T> resultList = new ArrayList<T>();
				String line = reader.readLine();
				if (null != line && line.equals(getCSVHeader(template))) {
					line = reader.readLine();
				}
				int startKeysIdx = 1;
				int startFieldsIdx = startKeysIdx + template.getKeyNames().length;
				while (null != line) {
					if (!line.isEmpty()) {
						String[] values = line.split(String.valueOf(CSV_SEPARATOR));
						for (int i = 0; i < values.length; i++) {
							values[i] = values[i].replace(COMMA_REPLACEMENT, ",");
						}
						String[] keyValues = Arrays.copyOfRange(values, startKeysIdx, startFieldsIdx);
						String[] fieldValues = Arrays.copyOfRange(values, startFieldsIdx, values.length);

						T entity;
						try {
							entity = (T) template.getClass().getConstructor(String[].class, Object[].class, long.class).newInstance(keyValues, fieldValues, Long.parseLong(values[0]));
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							throw new RuntimeException(e);
						}
						resultList.add(entity);
					}
					line = reader.readLine();
				}
				return resultList;
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				ftpClient.completePendingCommand();
				ftpClient.disconnect();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

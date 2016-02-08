package rocks.inspectit.statistics.source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rocks.inspectit.statistics.Constants;
import rocks.inspectit.statistics.entities.AbstractStatisticsEntity;
import rocks.inspectit.statistics.entities.AbstractStatisticsEntity.Identifier;

public class CSVSource<T extends AbstractStatisticsEntity> implements IDataSource<T> {
	private static final char CSV_SEPARATOR = ',';
	private static final String COMMA_REPLACEMENT = "#*&";

	private final String fileName;

	/**
	 * @param fileName
	 */
	public CSVSource(String fileName) {
		this.fileName = fileName;
	}

	protected String getCSVHeader(AbstractStatisticsEntity template) {
		String result = Constants.TIMESTAMP;
		for (String keyName : template.getKeyNames()) {
			result += CSV_SEPARATOR + keyName;
		}
		for (String fieldName : template.getFieldNames()) {
			result += CSV_SEPARATOR + fieldName;
		}

		return result;
	}

	@Override
	public void store(Collection<T> statistics) {
		File csvFile = new File(fileName);
		if (csvFile.exists()) {
			csvFile.delete();
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {

			boolean first = true;
			for (T stat : statistics) {
				if (first) {
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
		File csvFile = new File(fileName);
		if (!csvFile.exists()) {
			throw new IllegalArgumentException("File " + fileName + " does not exist!");
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
			List<T> resultList = new ArrayList<T>();
			String line = reader.readLine();
			if (line.equals(getCSVHeader(template))) {
				line = reader.readLine();
			}
			int startKeysIdx = 1;
			int startFieldsIdx = startKeysIdx + template.getKeyNames().length;
			while (null != line) {
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

				line = reader.readLine();
			}
			return resultList;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Number> getAbsoluteCounts(long since, Identifier identifier, T template) {
		List<T> all = load(template);
		Map<String, Number> result = new HashMap<String, Number>();

		for (T entity : all) {
			if (entity.getTimestamp() > since && entity.getIdentifier().equals(identifier)) {
				for (Entry<String, Object> entry : entity.getFieldValues().entrySet()) {
					double sum = 0.0;
					if (result.containsKey(entry.getKey())) {
						sum = result.get(entry.getKey()).doubleValue();
					}
					if (entry.getValue() instanceof Number) {
						sum += ((Number) entry.getValue()).doubleValue();
					}
					result.put(entry.getKey(), sum);
				}

			}
		}
		return result;
	}

	protected String toCSVString(T entity) {

		StringBuilder builder = new StringBuilder();
		builder.append(entity.getTimestamp());

		for (String key : entity.getKeyValuesList()) {
			builder.append(CSV_SEPARATOR);
			builder.append(key.replace(",", COMMA_REPLACEMENT));
		}

		for (Object field : entity.getFieldValuesList()) {
			builder.append(CSV_SEPARATOR);
			if (field instanceof String) {
				builder.append(field.toString().replace(",", COMMA_REPLACEMENT));
			} else {
				builder.append(field.toString());
			}

		}

		return builder.toString();
	}

	@Override
	public long getLatestTimestamp(T template) {
		List<T> all = load(template);
		long max = Long.MIN_VALUE;
		for (T element : all) {
			if (element.getTimestamp() > max) {
				max = element.getTimestamp();
			}
		}
		return max;
	}

	public String getFileName() {
		return fileName;
	}

}

package rocks.inspectit.statistics.entities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rocks.inspectit.statistics.entities.EntityField.MetricType;

public abstract class AbstractStatisticsEntity {

	private final Identifier identifier;
	private final String measurementName;
	private long timestamp;

	public AbstractStatisticsEntity(String measurementName, long timestamp, String[] keys) {
		this.measurementName = measurementName;
		this.timestamp = timestamp;
		identifier = new Identifier(keys);
	}

	public abstract String[] getKeyNames();

	public abstract String[] getFieldNames();

	public abstract Object[] getFieldValuesList();

	public void setFields(Map<String, Object> fieldValues) {
		for (Field field : this.getClass().getDeclaredFields()) {
			EntityField annotation = field.getAnnotation(EntityField.class);
			if (null != annotation && fieldValues.containsKey(annotation.name())) {
				Object value;
				if (field.getType().isAssignableFrom(int.class)) {
					value = getIntValue(fieldValues.get(annotation.name()));
				} else if (field.getType().isAssignableFrom(double.class)) {
					value = getDoubleValue(fieldValues.get(annotation.name()));
				} else {
					value = fieldValues.get(annotation.name()).toString();
				}
				try {
					field.set(this, value);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public boolean hasNewInformation(AbstractStatisticsEntity before) {
		return !allRelativeMetricsZero() || !allAbsolutesAsBefore(before);
	}

	private boolean allRelativeMetricsZero() {
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				EntityField annotation = field.getAnnotation(EntityField.class);
				if (null != annotation && annotation.metricType().equals(MetricType.RELATIVE)) {
					if (field.getType().isAssignableFrom(int.class)) {
						if (field.getInt(this) != 0) {
							return false;
						}
					} else if (field.getType().isAssignableFrom(double.class)) {
						if (field.getDouble(this) != 0.0) {
							return false;
						}
					} else {
						return false;
					}
				}
			}

			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean allAbsolutesAsBefore(AbstractStatisticsEntity before) {
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				EntityField annotation = field.getAnnotation(EntityField.class);
				if(field.getType().isAssignableFrom(String.class)){
					return false;
				}
				if (null != annotation && annotation.metricType().equals(MetricType.ABSOLUTE) && !field.get(this).equals(field.get(before))) {
					return false;
				}
			}

			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String[] getKeyValuesList() {
		return getIdentifier().getKeys().toArray(new String[0]);
	}

	public Map<String, String> getKeyValues() {
		Map<String, String> result = new HashMap<String, String>();
		int i = 0;
		String[] keyLabels = getKeyNames();
		for (String key : getKeyValuesList()) {
			result.put(keyLabels[i], key);
			i++;
		}

		return result;
	}

	public Map<String, Object> getFieldValues() {
		Map<String, Object> result = new HashMap<String, Object>();
		int i = 0;
		String[] fieldLabels = getFieldNames();
		for (Object field : getFieldValuesList()) {
			result.put(fieldLabels[i], field);
			i++;
		}

		return result;
	}

	public Set<String> getFieldNames(MetricType type) {
		Set<String> names = new HashSet<String>();
		for (Field field : this.getClass().getDeclaredFields()) {
			EntityField annotation = field.getAnnotation(EntityField.class);
			if (null != annotation && annotation.metricType().equals(type)) {
				names.add(annotation.name());
			}
		}
		return names;
	}

	public Map<String, Object> getFieldValues(MetricType type) {
		Map<String, Object> allValues = getFieldValues();
		Set<String> names = getFieldNames(type);
		Map<String, Object> result = new HashMap<String, Object>();
		for (String name : names) {
			result.put(name, allValues.get(name));
		}
		return result;
	}

	/**
	 * @return the measurementName
	 */
	public String getMeasurementName() {
		return measurementName;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the identifier
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	protected int getIntValue(Object obj) {
		if (null == obj || !(obj instanceof Number || obj instanceof String)) {
			throw new IllegalArgumentException("Invalid field value!");
		}
		if (obj instanceof Number) {
			return ((Number) obj).intValue();
		} else if (obj instanceof String) {
			return Integer.parseInt(((String) obj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
	}

	protected double getDoubleValue(Object obj) {
		if (null == obj || !(obj instanceof Number || obj instanceof String)) {
			throw new IllegalArgumentException("Invalid field value!");
		}
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		} else if (obj instanceof String) {
			return Double.parseDouble(((String) obj));
		} else {
			throw new IllegalArgumentException("Invalid field value!");
		}
	}

	@Override
	public String toString() {
		String result = String.valueOf(getTimestamp());
		for (Map.Entry<String, String> entry : getKeyValues().entrySet()) {
			result += ", " + entry.getKey() + " = " + entry.getValue();
		}
		for (Map.Entry<String, Object> entry : getFieldValues().entrySet()) {
			result += ", " + entry.getKey() + " = " + entry.getValue().toString();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((measurementName == null) ? 0 : measurementName.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + Arrays.hashCode(getFieldValuesList());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractStatisticsEntity other = (AbstractStatisticsEntity) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (measurementName == null) {
			if (other.measurementName != null)
				return false;
		} else if (!measurementName.equals(other.measurementName))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (!Arrays.equals(getFieldValuesList(), other.getFieldValuesList()))
			return false;
		return true;
	}

	public static class Identifier {
		private final List<String> keys;

		/**
		 * @param keys
		 */
		public Identifier(String... keyArray) {
			if (null != keyArray) {
				keys = new ArrayList<String>(keyArray.length);
				for (String str : keyArray) {
					getKeys().add(str);
				}
			} else {
				keys = Collections.emptyList();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getKeys() == null) ? 0 : getKeys().hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Identifier other = (Identifier) obj;
			if (getKeys() == null) {
				if (other.getKeys() != null)
					return false;
			} else if (!getKeys().equals(other.getKeys()))
				return false;
			return true;
		}

		/**
		 * @return the keys
		 */
		public List<String> getKeys() {
			return keys;
		}

	}

}

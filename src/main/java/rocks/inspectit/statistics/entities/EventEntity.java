package rocks.inspectit.statistics.entities;

import java.util.Map;

public class EventEntity extends AbstractStatisticsEntity {
	public static final String EVENTS_MEASUREMENT = "events";
	public static final String EVENTS_TITLE_FIELD = "title";
	public static final String EVENTS_DESCRIPTION_FIELD = "description";
	public static final String EVENTS_ATTENDEES_FIELD = "attendees";
	public static final String EVENTS_LEADS_FIELD = "leads";
	public static final String EVENTS_TYPE_TAG = "type";

	public static final String[] KEY_NAMES = new String[] { EVENTS_TYPE_TAG };
	public static final String[] FIELD_NAMES = new String[] { EVENTS_TITLE_FIELD, EVENTS_DESCRIPTION_FIELD, EVENTS_ATTENDEES_FIELD, EVENTS_LEADS_FIELD };

	private String title;
	private String description;
	private int attendees;
	private int leads;

	private static EventEntity template;

	public static EventEntity getTemplate() {
		if (null == template) {
			template = new EventEntity();
		}
		return template;
	}

	private EventEntity() {
		super(EVENTS_MEASUREMENT, 0L, null);
	}

	public EventEntity(long timestamp, String type, String title, String description, int attendees, int leads) {
		super(EVENTS_MEASUREMENT, timestamp, new String[] { type });
		this.title = title;
		this.description = description;
		this.attendees = attendees;
		this.leads = leads;
	}

	public EventEntity(String[] keys, Object[] fields, long timestamp) {
		super(EVENTS_MEASUREMENT, timestamp, keys);

		if (fields.length < 2) {
			throw new IllegalArgumentException("Invalid amount of field values!");
		}

		title = fields[0].toString();
		description = fields[1].toString();
		attendees = getIntValue(fields[2]);
		leads = getIntValue(fields[3]);

	}

	@Override
	public String[] getKeyNames() {
		return KEY_NAMES;
	}

	@Override
	public String[] getFieldNames() {
		return FIELD_NAMES;
	}

	@Override
	public Object[] getFieldValuesList() {
		return new Object[] { getTitle(), getDescription(), getAttendees(), getLeads() };
	}

	@Override
	public void setFields(Map<String, Object> fieldValues) {
		title = fieldValues.get(EVENTS_TITLE_FIELD).toString();
		description = fieldValues.get(EVENTS_DESCRIPTION_FIELD).toString();
		attendees = getIntValue(fieldValues.get(EVENTS_ATTENDEES_FIELD));
		leads = getIntValue(fieldValues.get(EVENTS_LEADS_FIELD));
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the attendees
	 */
	public int getAttendees() {
		return attendees;
	}

	/**
	 * @param attendees
	 *            the attendees to set
	 */
	public void setAttendees(int attendees) {
		this.attendees = attendees;
	}

	/**
	 * @return the leads
	 */
	public int getLeads() {
		return leads;
	}

	/**
	 * @param leads
	 *            the leads to set
	 */
	public void setLeads(int leads) {
		this.leads = leads;
	}

}

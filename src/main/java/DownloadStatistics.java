public class DownloadStatistics {

	private static final char CSV_SEPARATOR = ',';

	public static String getCSVHeader() {
		return "Timestamp,ArtifactType,OS,Architecture,MajorVersion,MinorVersion,BuildNr,DownloadCount";
	}

	public static DownloadStatistics fromCSVString(String string) {

		String[] strArray = string.split(",");

		return new DownloadStatistics(Long.parseLong(strArray[0]), strArray[1], strArray[2], strArray[3], strArray[4], strArray[5], strArray[6], Integer.parseInt(strArray[7]));
	}

	private String majorVersion;
	private String minorVersion;
	private String buildNr;
	private int downloadCount;
	private String artifactType;
	private String os;
	private String architecture;
	private long timestamp;

	public DownloadStatistics() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param majorVersion
	 * @param minorVersion
	 * @param buildNr
	 * @param downloadCount
	 * @param artifactType
	 * @param os
	 * @param architecture
	 */
	public DownloadStatistics(long timestamp, String artifactType, String os, String architecture, String majorVersion, String minorVersion, String buildNr, int downloadCount) {
		this.timestamp = timestamp;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.buildNr = buildNr;
		this.downloadCount = downloadCount;
		this.artifactType = artifactType;
		this.os = os;
		this.architecture = architecture;
	}

	/**
	 * @return the artifactType
	 */
	public String getArtifactType() {
		return artifactType;
	}

	/**
	 * @param artifactType
	 *            the artifactType to set
	 */
	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	/**
	 * @return the majorVersion
	 */
	public String getMajorVersion() {
		return majorVersion;
	}

	/**
	 * @param majorVersion
	 *            the majorVersion to set
	 */
	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}

	/**
	 * @return the minorVersion
	 */
	public String getMinorVersion() {
		return minorVersion;
	}

	/**
	 * @param minorVersion
	 *            the minorVersion to set
	 */
	public void setMinorVersion(String minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * @return the buildNr
	 */
	public String getBuildNr() {
		return buildNr;
	}

	/**
	 * @param buildNr
	 *            the buildNr to set
	 */
	public void setBuildNr(String buildNr) {
		this.buildNr = buildNr;
	}

	/**
	 * @return the downloadCount
	 */
	public int getDownloadCount() {
		return downloadCount;
	}

	/**
	 * @param downloadCount
	 *            the downloadCount to set
	 */
	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	/**
	 * @return the majorVersion
	 */
	public String getVersion() {
		return getMajorVersion() + "." + getMinorVersion() + "." + getBuildNr();
	}

	@Override
	public String toString() {
		return "RELEASE." + getMajorVersion() + "." + getMinorVersion() + "." + getBuildNr() + "/" + getArtifactType() + " " + getDownloadCount();
	}

	public String toCSVString() {
		return String.valueOf(getTimestamp()) + CSV_SEPARATOR + getArtifactType() + CSV_SEPARATOR + getOs() + CSV_SEPARATOR + getArchitecture() + CSV_SEPARATOR + getMajorVersion() + CSV_SEPARATOR + getMinorVersion()
				+ CSV_SEPARATOR + getBuildNr() + CSV_SEPARATOR + String.valueOf(getDownloadCount());
	}

	/**
	 * @return the architecture
	 */
	public String getArchitecture() {
		return architecture;
	}

	/**
	 * @param architecture
	 *            the architecture to set
	 */
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	/**
	 * @return the os
	 */
	public String getOs() {
		return os;
	}

	/**
	 * @param os
	 *            the os to set
	 */
	public void setOs(String os) {
		this.os = os;
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

}

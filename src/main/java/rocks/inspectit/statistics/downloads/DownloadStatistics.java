package rocks.inspectit.statistics.downloads;
public class DownloadStatistics {

	private final Identifier identifier;
	private int downloadCount;
	private long timestamp;

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
		this(timestamp, new Identifier(artifactType, os, architecture, majorVersion, minorVersion, buildNr), downloadCount);

	}

	public DownloadStatistics(long timestamp, Identifier identifier, int downloadCount) {
		this.timestamp = timestamp;
		this.identifier = identifier;
		this.downloadCount = downloadCount;

	}
	
	
	

	/**
	 * @return the artifactType
	 */
	public String getArtifactType() {
		return getIdentifier().getArtifactType();
	}

	/**
	 * @return the majorVersion
	 */
	public String getMajorVersion() {
		return getIdentifier().getMajorVersion();
	}

	/**
	 * @return the minorVersion
	 */
	public String getMinorVersion() {
		return getIdentifier().getMinorVersion();
	}

	/**
	 * @return the buildNr
	 */
	public String getBuildNr() {
		return getIdentifier().getBuildNr();
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

	/**
	 * @return the architecture
	 */
	public String getArchitecture() {
		return getIdentifier().getArchitecture();
	}

	/**
	 * @return the os
	 */
	public String getOs() {
		return getIdentifier().getOs();
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

}

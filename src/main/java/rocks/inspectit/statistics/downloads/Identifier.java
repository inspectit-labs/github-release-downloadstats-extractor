package rocks.inspectit.statistics.downloads;
public class Identifier {
	private String majorVersion;
	private String minorVersion;
	private String buildNr;
	private String artifactType;
	private String os;
	private String architecture;

	/**
	 * @param majorVersion
	 * @param minorVersion
	 * @param buildNr
	 * @param artifactType
	 * @param os
	 * @param architecture
	 */
	public Identifier(String artifactType, String os, String architecture, String majorVersion, String minorVersion, String buildNr) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.buildNr = buildNr;
		this.artifactType = artifactType;
		this.os = os;
		this.architecture = architecture;
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((architecture == null) ? 0 : architecture.hashCode());
		result = prime * result + ((artifactType == null) ? 0 : artifactType.hashCode());
		result = prime * result + ((buildNr == null) ? 0 : buildNr.hashCode());
		result = prime * result + ((majorVersion == null) ? 0 : majorVersion.hashCode());
		result = prime * result + ((minorVersion == null) ? 0 : minorVersion.hashCode());
		result = prime * result + ((os == null) ? 0 : os.hashCode());
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
		if (architecture == null) {
			if (other.architecture != null)
				return false;
		} else if (!architecture.equals(other.architecture))
			return false;
		if (artifactType == null) {
			if (other.artifactType != null)
				return false;
		} else if (!artifactType.equals(other.artifactType))
			return false;
		if (buildNr == null) {
			if (other.buildNr != null)
				return false;
		} else if (!buildNr.equals(other.buildNr))
			return false;
		if (majorVersion == null) {
			if (other.majorVersion != null)
				return false;
		} else if (!majorVersion.equals(other.majorVersion))
			return false;
		if (minorVersion == null) {
			if (other.minorVersion != null)
				return false;
		} else if (!minorVersion.equals(other.minorVersion))
			return false;
		if (os == null) {
			if (other.os != null)
				return false;
		} else if (!os.equals(other.os))
			return false;
		return true;
	}

}

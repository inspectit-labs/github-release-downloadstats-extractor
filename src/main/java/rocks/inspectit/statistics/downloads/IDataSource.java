package rocks.inspectit.statistics.downloads;
import java.util.Collection;
import java.util.List;

public interface IDataSource {
	void store(Collection<DownloadStatistics> statistics);

	List<DownloadStatistics> load();
	 int getAbsoluteCounts(long since, Identifier identifier);
}

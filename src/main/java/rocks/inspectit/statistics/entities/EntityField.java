package rocks.inspectit.statistics.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for the fields of {@link AbstractStatisticsEntity} instances to denote abstract and relative metrics.
 * @author Alexander Wert
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntityField {
	public MetricType metricType() default MetricType.ABSOLUTE;

	public String name();

	public enum MetricType {
		/**
		 * Relative metric (e.g. per time interval).
		 */
		RELATIVE, 
		/**
		 * Absolute metric (e.g. counter)
		 */
		ABSOLUTE;
	}
}

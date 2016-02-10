package rocks.inspectit.statistics.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntityField {
	public MetricType metricType() default MetricType.ABSOLUTE;

	public String name();

	public enum MetricType {
		RELATIVE, ABSOLUTE;
	}
}

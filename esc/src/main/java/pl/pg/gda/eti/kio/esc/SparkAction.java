package pl.pg.gda.eti.kio.esc;

import org.apache.spark.api.java.JavaSparkContext;

public interface SparkAction {
	public void doAction(JavaSparkContext sc);
}

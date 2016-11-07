package pl.pg.gda.eti.kio.esc;

import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;

public interface SparkAction extends Serializable {
	public void doAction(JavaSparkContext sc);
}

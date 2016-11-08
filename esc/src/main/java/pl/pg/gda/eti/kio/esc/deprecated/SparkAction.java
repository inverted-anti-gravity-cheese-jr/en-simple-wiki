package pl.pg.gda.eti.kio.esc.deprecated;

import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;

@Deprecated
public interface SparkAction extends Serializable {
	public void doAction(JavaSparkContext sc);
}

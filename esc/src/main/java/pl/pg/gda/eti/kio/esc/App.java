package pl.pg.gda.eti.kio.esc;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	SparkConf conf = new SparkConf().setAppName("En Simple Wiki").setMaster("local");
    	JavaSparkContext sc = new JavaSparkContext(conf);
    	
    	new SparkTestAction().doAction(sc);
    	
    	sc.close();
    }
}

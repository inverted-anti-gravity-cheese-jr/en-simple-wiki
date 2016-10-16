package pl.pg.gda.eti.kio.esc;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

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
    	
    	JavaRDD<String> lines = sc.textFile("test.txt");
    	JavaRDD<Integer> lineLengths = lines.map(s -> s.length());
    	int totalLength = lineLengths.reduce((a, b) -> a + b);
    	
        System.out.println( "Hello World!" + totalLength );
    }
}

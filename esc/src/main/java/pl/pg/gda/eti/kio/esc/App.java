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
    	
    	JavaRDD<String> lista = sc.textFile("simple/temp-po_slowach-lista-simple-20120104");
    	JavaPairRDD<String, Iterable<String>> listaWithId = lista.groupBy(l -> l.substring(0, l.indexOf('#')));
    	
    	JavaRDD<String> nazwyArt = sc.textFile("simple/temp-po_slowach-articles_dict-simple-20120104");
    	JavaPairRDD<String, Tuple2<Iterable<String>, Iterable<String>>> artZeSlowami = nazwyArt.groupBy(f -> f.substring(f.indexOf('\t') + 1)).join(listaWithId);
    	
    	
    	JavaPairRDD<String, Tuple2<Iterable<String>, Iterable<String>>> artZeSlowem1 = artZeSlowami.filter(t -> t._2._2.iterator().next().contains("#1-"));
    	artZeSlowem1.foreach(f -> {
    		String namePair = f._2._1.iterator().next();
    		System.out.println(namePair.substring(0, namePair.indexOf('\t')));
    	});
    }
}

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
    	JavaPairRDD<String, Tuple2<Iterable<String>, Iterable<String>>> artZeSlowami = nazwyArt.groupBy(f -> f.substring(f.indexOf(' ') + 1)).join(listaWithId);
    	
    	
    	artZeSlowami.foreach(f -> System.out.println(f));
    	artZeSlowami.filter(t -> t._2._2.iterator().next().contains("#1-")).foreach(f -> System.out.println(f));
    	
    	
    	System.out.println("ehlo");
    }
}

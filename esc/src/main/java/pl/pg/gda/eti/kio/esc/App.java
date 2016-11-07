package pl.pg.gda.eti.kio.esc;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
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
		System.setProperty("hadoop.home.dir", "D:\\Studia\\IWI\\hadoop-2.7.3.tar\\hadoop-2.7.3");

    	SparkConf conf = new SparkConf().setAppName("En Simple Wiki").setMaster("local");
    	JavaSparkContext sc = new JavaSparkContext(conf);

		MergeWordDictionaryAction wordDictionaryMerger = new MergeWordDictionaryAction();
		wordDictionaryMerger.doAction(sc);

		// połączony słownik
		JavaPairRDD<String, Tuple2<String, String>> unifiedWordDictionary = wordDictionaryMerger.getUnifiedWordDictionary();

		// TODO: wyliczyć prawdopodobieństwo cosinusowe
		// https://janav.wordpress.com/2013/10/27/tf-idf-and-cosine-similarity/
		new CountCosineSimilarityAction().doAction(sc);
		// TODO: na podstawie najlepszego artykułu wyznaczyć główną kategorię dla niego (myślę że można założyć, że pierwsza na liście w "cats_dict" to jest główna kategoria)

		sc.close();
    }
}

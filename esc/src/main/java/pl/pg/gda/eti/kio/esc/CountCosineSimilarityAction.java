package pl.pg.gda.eti.kio.esc;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wojtek Stanisławski on 07.11.2016.
 */
public class CountCosineSimilarityAction implements SparkAction {
    private JavaPairRDD<String, Tuple2<String, String>> wordDictionary;

    @Override
    public void doAction(JavaSparkContext sc) {
        JavaRDD<String> tfidfEn = sc.textFile("en/en-po_slowach-lista-en-20111201");
        JavaRDD<String> tfidfSimple = sc.textFile("simple/temp-po_slowach-lista-simple-20120104");

        tfidfEn.foreach((article) -> {
            tfidfSimple.foreach( (simpleArticle) -> {
                List<Tuple3<String, Double, Double>> commonWords = new ArrayList<Tuple3<String, Double, Double>>();
                insertEnWords(article, commonWords);
                //TODO: dodac słowa z simple i pousuwać te, które nie są wspólne dla obu, wtedy będą znane już TF:IDF
                //TODO: na postawie słów TF:IDF wyliczyć podobieństwo cosinusowe dla każdego z artykułów
                //TODO: wybrać jeden najlepszy artykuł z simple dla każdego z EN
                //TODO: zapisac gdzies wynik, np do prywatnego pola
            });
        });


    }

    private void insertEnWords(String line, List<Tuple3<String, Double, Double>> commonWords) {
        String wordsLine = line.substring(line.indexOf('#') + 1);
        String[] words = wordsLine.split(" ");
        for (final String word: words) {
            JavaPairRDD<String, Tuple2<String, String>> filtered = wordDictionary.filter((t) ->
                    t._2._2.equals(word.substring(0, word.indexOf('-')))
            );
            commonWords.add(new Tuple3<String, Double, Double>(
                    filtered.first()._1,
                    0.0,
                    Double.parseDouble(word.substring(word.indexOf('-') + 1)))
            );
        }

    }
}

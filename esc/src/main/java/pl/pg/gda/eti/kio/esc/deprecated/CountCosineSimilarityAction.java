package pl.pg.gda.eti.kio.esc.deprecated;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import scala.Tuple3;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Wojtek Stanisławski on 07.11.2016.
 */
@Deprecated
public class CountCosineSimilarityAction implements SparkAction {
    private JavaPairRDD<String, Tuple2<String, String>> wordDictionary;

    public void setWordDictionary(JavaPairRDD<String, Tuple2<String, String>> wordDictionary) {
        this.wordDictionary = wordDictionary;
    }

    @Override
    public void doAction(JavaSparkContext sc) {
        JavaRDD<String> tfidfEn = sc.textFile("en/en-po_slowach-lista-en-20111201");
        JavaRDD<String> tfidfSimple = sc.textFile("simple/temp-po_slowach-lista-simple-20120104");

        JavaPairRDD<String, List<String>> tfidfEnWithKey = tfidfEn.mapToPair(line -> {
            List<String> words;
            String key = line.substring(0, line.indexOf('#'));
            words = Arrays.asList(line.substring(line.indexOf('#') + 1).split(" "));
            return new Tuple2<String, List<String>>(key, words);
        });
        /*
        tfidfEnWithKey.cartesian(wordDictionary).mapToPair((tfidf, dict) -> {

        });
        */

        /*
        tfidfEn.cartesian(tfidfSimple).foreach((articleSet) -> {
            String article = articleSet._1;
            String simpleArticle = articleSet._2;
            List<Tuple3<String, Double, Double>> commonWords = new ArrayList<Tuple3<String, Double, Double>>();
            insertEnWords(article, commonWords);
            //TODO: dodac słowa z simple i pousuwać te, które nie są wspólne dla obu, wtedy będą znane już TF:IDF
            insertSimpleWords(article, commonWords);
            removeMismatchedWords(commonWords);
            //TODO: na postawie słów TF:IDF wyliczyć podobieństwo cosinusowe dla każdego z artykułów
            //TODO: wybrać jeden najlepszy artykuł z simple dla każdego z EN
            //TODO: zapisac gdzies wynik, np do prywatnego pola
        });
        */


    }

    private void insertEnWords(String line, List<Tuple3<String, Double, Double>> commonWords) {
        String wordsLine = line.substring(line.indexOf('#') + 1);
        String[] words = wordsLine.split(" ");
        for (final String word: words) {
            Tuple2<String, Tuple2<String, String>> first = wordDictionary.filter((t) ->
                    t._2._2.equals(word.substring(0, word.indexOf('-')))
            ).first();
            if(first != null) {
                commonWords.add(new Tuple3<String, Double, Double>(
                        first._1,
                        0.0,
                        Double.parseDouble(word.substring(word.indexOf('-') + 1)))
                );
            }
        }
        
    }
    
    private void insertSimpleWords(String line, List<Tuple3<String, Double, Double>> commonWords) {
    	String wordsLine = line.substring(line.indexOf('#') + 1);
        String[] words = wordsLine.split(" ");
        for (final String word: words) {
            JavaPairRDD<String, Tuple2<String, String>> filtered = wordDictionary.filter((t) ->
                    t._2._1.equals(word.substring(0, word.indexOf('-')))
            );
            if(!filtered.isEmpty()) {
            	for(int i = 0; i< commonWords.size(); i++) {
            		Tuple3<String, Double, Double> commonWord = commonWords.get(i); 
            		if(commonWord._1() == filtered.first()._1) {
            			commonWords.set(i, new Tuple3<String, Double, Double>(
	            				commonWord._1(),
	                            Double.parseDouble(word.substring(word.indexOf('-') + 1)),
	        					commonWord._3())
    					);
            			break;
            		}
            	}
            }
        }
    }
    
    private void removeMismatchedWords(List<Tuple3<String, Double, Double>> commonWords) {
    	for(Tuple3<String, Double, Double> commonWord : commonWords ) {
    		if(commonWord._2() == 0.0) {
    			commonWords.remove(commonWord);
    		}
    	}
    }
}

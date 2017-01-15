package pl.pg.gda.eti.kio.esc;

import java.io.*;
import java.util.Map;

/**
 * @author Wojciech Stanisławski
 * @since 15.01.17
 */
public class DictionaryUtil {

	private static final KeyValueDictionaryFinder finder = new KeyValueDictionaryFinder();
	private static final KeyValueDictionaryFinder articleFinder = new KeyValueDictionaryFinder();

	public static void saveDictionary(String fileName, Map values) throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		values.entrySet().forEach(p -> {
			try {
				writer.write(p.toString());
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		writer.flush();
		writer.close();
	}


	public static void categoryFinderInit(String file) {
		finder.init(file);
	}

	public static String findCategoryName(String catId) {
		try {
			return finder.fetchCategoryName(catId);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void articleFinderInit(String file) {
		articleFinder.init(file);
	}

	public static String findArticleName(String artId) {
		try {
			return articleFinder.fetchCategoryName(artId);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static class KeyValueDictionaryFinder {

		private String fileName;

		public void init(String file) {
			fileName = file;
		}

		public String fetchCategoryName(String catId) throws IOException {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = reader.readLine()) != null) {
				if(line.contains("\t") && catId.equals(line.substring(line.indexOf('\t') + 1).trim())) {
					reader.close();
					return line.substring(0, line.indexOf('\t'));
				}
			}
			reader.close();
			return null;
		}

	}

}

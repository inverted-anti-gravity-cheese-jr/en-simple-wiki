package pl.pg.gda.eti.kio.esc.data;

/**
 * @author Wojciech Stanisławski
 * @since 14.01.17
 */
public class Tuple<K, V> {
	private K key;
	private V value;

	public Tuple() {
	}

	public Tuple(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Tuple{" +
				"key=" + key +
				", value=" + value +
				'}';
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
}

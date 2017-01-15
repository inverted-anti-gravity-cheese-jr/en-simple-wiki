package pl.pg.gda.eti.kio.esc;

/**
 * @author Wojciech Stanisławski
 * @since 15.01.17
 */
public class TimeCounter {
	private long st, en;

	public void start() {
		st = System.currentTimeMillis();
	}

	public void end() {
		en = System.currentTimeMillis();
	}

	public long diffMs() {
		return en - st;
	}

	public void printMessage(String task) {
		System.out.println("Performing task \"" + task + "\" took " + diffMs() + " ms.");
	}
}

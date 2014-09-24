package cz.artique.simpleStreamer;


/**
 * Hello world!
 *
 */
public class App {

	/**
	 * If any thread throws an exception, whole application should crash.
	 */
	private static void setupThreading() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.exit(1);
			}
		});
	}

	public static void main(String[] args) {
		setupThreading();
		AppArgs arguments = new AppArgs(args);
	}
}

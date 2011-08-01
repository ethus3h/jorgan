package jorgan.gui;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jorgan.UI;
import spin.over.SpinOverEvaluator;
import bias.Configuration;
import bias.swing.MessageBox;

/**
 * Graphical user interface implementation.
 */
public class GUI implements UI {

	private static Logger log = Logger.getLogger(GUI.class.getName());

	private static Configuration config = Configuration.getRoot()
			.get(GUI.class);

	private OrganFrame frame;

	private boolean showAboutOnStartup = true;

	private LAF lookAndFeel = LAF.DEFAULT;

	public GUI() {
		config.read(this);
	}

	public boolean getShowAboutOnStart() {
		return showAboutOnStartup;
	}

	public void setShowAboutOnStartup(boolean showAboutOnStartup) {
		this.showAboutOnStartup = showAboutOnStartup;
	}

	public LAF getLookAndFeel() {
		return lookAndFeel;
	}

	public void setLookAndFeel(LAF lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}

	/**
	 * Start the user interaction.
	 * 
	 * @param file
	 *            optional file that contains an organ
	 */
	public void display(final File file) {

		invokeOnSwing(new ExceptionContext());

		invokeOnSwing(new SwingContext());

		if (showAboutOnStartup) {
			AboutPanel.showSplash();
		}

		FrameContext frameContext = new FrameContext();
		invokeOnSwing(frameContext);

		AboutPanel.hideSplash();

		if (file != null) {
			invokeOnSwing(new FileContext(frame, file));
		}

		frameContext.waitAndDispose();
	}

	private void invokeOnSwing(Runnable runnable) {
		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InterruptedException e) {
			throw new Error(e);
		} catch (InvocationTargetException e) {
			throw new Error(e);
		}
	}

	private class SwingContext implements Runnable {
		public void run() {
			String plaf = lookAndFeel.getClassName();
			try {
				if (plaf != null) {
					log.log(Level.INFO, "setting plaf '" + plaf + "'");
					UIManager.setLookAndFeel(plaf);
				}
			} catch (Exception ex) {
				log.log(Level.WARNING, "unable to set plaf '" + plaf + "'", ex);
			}

			Toolkit.getDefaultToolkit().setDynamicLayout(true);

			// IMPORTANT:
			// Never wait for the result of a spin-over or we'll
			// run into deadlocks!! (player lock <-> Swing EDT)
			SpinOverEvaluator.setDefaultWait(false);
		}
	}

	public class ExceptionContext implements Runnable, UncaughtExceptionHandler {

		public void run() {
			Thread.currentThread().setUncaughtExceptionHandler(this);

			System.setProperty("sun.awt.exception.handler", getClass()
					.getName());
		}

		/**
		 * http://bugs.sun.com/view_bug.do?bug_id=4499199
		 */
		public void handle(Throwable throwable) {
			log.log(Level.SEVERE, throwable.getMessage(), throwable);

			MessageBox box = config.get("exception").read(
					new MessageBox(MessageBox.OPTIONS_OK_CANCEL));
			if (box.show(frame) == MessageBox.OPTION_OK) {
				System.exit(1);
			}
		}

		public void uncaughtException(final Thread thread,
				final Throwable throwable) {
			handle(throwable);
		}
	}

	private class FrameContext extends WindowAdapter implements Runnable {

		public void run() {
			frame = new OrganFrame();
			frame.addWindowListener(this);
			frame.setVisible(true);
			frame.requestFocus();
		}

		@Override
		public synchronized void windowClosed(WindowEvent e) {
			frame = null;

			notify();
		}

		public synchronized void waitAndDispose() {
			while (frame != null) {
				try {
					wait();
				} catch (InterruptedException ex) {
					throw new Error(ex);
				}
			}
		}
	}

	private static class FileContext implements Runnable {

		private OrganFrame frame;

		private File file;

		public FileContext(OrganFrame frame, File file) {
			this.frame = frame;
			this.file = file;
		}

		public void run() {
			frame.openOrgan(file);
		}
	}
}
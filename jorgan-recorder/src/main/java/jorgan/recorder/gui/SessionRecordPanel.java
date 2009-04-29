package jorgan.recorder.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import jorgan.recorder.SessionRecorder;
import jorgan.session.OrganSession;
import jorgan.session.SessionListener;
import jorgan.swing.StandardDialog;
import bias.Configuration;

/**
 * A dialog for recording.
 */
public class SessionRecordPanel extends JPanel {

	static Configuration config = Configuration.getRoot().get(
			SessionRecordPanel.class);

	public SessionRecordPanel(SessionRecorder recorder) {
		super(new BorderLayout());

		config.read(this);

		add(new RecorderPanel(recorder.getRecorder()), BorderLayout.CENTER);
	}

	public static void showInDialog(Component owner, OrganSession session) {
		final StandardDialog dialog = StandardDialog.create(owner);
		dialog.setModal(false);
		dialog.setResizable(false);
		
		config.get("dialog").read(dialog);

		final SessionRecorder recorder = new SessionRecorder(session);
		session.addListener(new SessionListener() {
			public void constructingChanged(boolean constructing) {
			}
			public void destroyed() {
				dialog.setVisible(false);
			}
		});

		SessionRecordPanel panel = new SessionRecordPanel(recorder);
		dialog.setBody(panel);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				recorder.dispose();
			}
		});

		dialog.setVisible(true);
	}
}
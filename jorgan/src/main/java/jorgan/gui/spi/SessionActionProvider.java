package jorgan.gui.spi;

import java.util.List;

import javax.swing.Action;

import jorgan.gui.OrganFrame;
import jorgan.session.OrganSession;

public interface SessionActionProvider {

	public List<Action> getActions(OrganSession session, OrganFrame frame);
}

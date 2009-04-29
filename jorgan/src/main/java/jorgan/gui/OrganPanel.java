/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jorgan.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jorgan.disposition.Console;
import jorgan.disposition.Element;
import jorgan.disposition.event.OrganAdapter;
import jorgan.gui.ConsolePanel.ConsoleStack;
import jorgan.gui.dock.BordererDockingPane;
import jorgan.gui.dock.ConsoleDockable;
import jorgan.gui.dock.OrganDockable;
import jorgan.gui.dock.spi.ProviderRegistry;
import jorgan.gui.play.MessagesMonitor;
import jorgan.play.event.PlayListener;
import jorgan.session.OrganSession;
import jorgan.session.SessionAware;
import jorgan.session.SessionListener;
import jorgan.session.problem.Problem;
import jorgan.session.problem.ProblemListener;
import jorgan.session.problem.Severity;
import jorgan.session.selection.SelectionEvent;
import jorgan.session.selection.SelectionListener;
import jorgan.session.undo.UndoListener;
import jorgan.swing.BaseAction;
import swingx.docking.Dockable;
import swingx.docking.Docked;
import swingx.docking.DockingPane;
import swingx.docking.persistence.XMLPersister;
import bias.Configuration;

/**
 * Panel for display and editing of an organ.
 */
public class OrganPanel extends JPanel implements SessionAware, ConsoleStack {

	private static final String DOCKING_VERSION = "1";

	private static Configuration config = Configuration.getRoot().get(
			OrganPanel.class);

	private static Logger logger = Logger.getLogger(OrganPanel.class.getName());

	private boolean constructing = false;

	/**
	 * The organ.
	 */
	private OrganSession session;

	/**
	 * The listener to events.
	 */
	private EventsListener eventsListener = new EventsListener();

	/*
	 * The outer dockingPane.
	 */
	private DockingPane docking = new BordererDockingPane();

	private Set<DockableAction> dockableActions = new HashSet<DockableAction>();

	/*
	 * The inner dockingPane that holds all consoles.
	 */
	private DockingPane consoleDocking = new BordererDockingPane();

	private BackAction backAction = new BackAction();

	private ForwardAction forwardAction = new ForwardAction();

	private UndoAction undoAction = new UndoAction();

	private RedoAction redoAction = new RedoAction();

	private MessagesMonitor messagesMonitor = new MessagesMonitor();

	private String playDocking;

	private String constructDocking;

	/**
	 * Create a new organPanel.
	 */
	public OrganPanel() {
		config.read(this);

		setLayout(new BorderLayout());

		docking.setBorder(new EmptyBorder(2, 2, 2, 2));
		add(docking, BorderLayout.CENTER);

		for (OrganDockable dockable : ProviderRegistry.getDockables()) {
			dockableActions.add(new DockableAction(dockable));
		}

		loadDocking();
	}

	/**
	 * Get widgets (i.e. actions or components) for the toolbar.
	 * 
	 * @return widgets
	 */
	public List<Object> getToolBarWidgets() {
		List<Object> widgets = new ArrayList<Object>();

		widgets.add(undoAction);
		widgets.add(redoAction);

		widgets.add(backAction);
		widgets.add(forwardAction);

		return widgets;
	}

	/**
	 * Get widgets (i.e. actions or components) for the status bar.
	 * 
	 * @return widgets
	 */
	public List<Object> getStatusBarWidgets() {
		List<Object> widgets = new ArrayList<Object>();

		widgets.add(messagesMonitor);

		return widgets;
	}

	/**
	 * Get widgets (i.e. actions or components) for the menu bar.
	 * 
	 * @return widgets
	 */
	public List<Object> getMenuWidgets() {
		return new ArrayList<Object>(dockableActions);
	}

	public void setSession(OrganSession session) {
		if (this.session != null) {
			this.session.removeOrganListener(eventsListener);
			this.session.removePlayerListener(eventsListener);
			this.session.removeProblemListener(eventsListener);
			this.session.removeSelectionListener(eventsListener);
			this.session.removeUndoListener(eventsListener);
			this.session.removeListener(eventsListener);

			for (DockableAction action : dockableActions) {
				action.getDockable().setSession(null);
			}

			for (Element element : this.session.getOrgan().getElements()) {
				if (element instanceof Console) {
					removeConsoleDockable((Console) element);
				}
			}
		}

		this.session = session;

		if (this.session != null) {
			setConstructing(!this.session.getPlay().isOpen());

			this.session.addListener(eventsListener);
			this.session.addUndoListener(eventsListener);
			this.session.addSelectionListener(eventsListener);
			this.session.addProblemListener(eventsListener);
			this.session.addPlayerListener(eventsListener);
			this.session.addOrganListener(eventsListener);

			for (DockableAction action : dockableActions) {
				action.getDockable().setSession(this.session);
			}

			for (Element element : this.session.getOrgan().getElements()) {
				if (element instanceof Console) {
					addConsoleDockable((Console) element);
				}
			}
		}

		undoAction.setEnabled(false);
		redoAction.setEnabled(false);

		updateHistory();
	}

	protected void updateHistory() {
		if (session == null || !constructing) {
			backAction.setEnabled(false);
			forwardAction.setEnabled(false);
		} else {
			backAction.setEnabled(session.getSelection().canBack());
			forwardAction.setEnabled(session.getSelection().canForward());
		}
	}

	protected void updateActions() {
		for (DockableAction action : dockableActions) {
			action.update();
		}
	}

	protected ConsoleDockable addConsoleDockable(Console console) {

		ConsoleDockable dockable = new ConsoleDockable(console) {
			@Override
			public void docked(Docked docked) {
				super.docked(docked);

				setSession(session);
			}

			@Override
			public void undocked() {
				setSession(null);

				super.undocked();
			}
		};

		consoleDocking.putDockable(console, dockable);

		return dockable;
	}

	protected void removeConsoleDockable(Console console) {
		ConsoleDockable dockable = (ConsoleDockable) consoleDocking
				.getDockable(console);
		if (dockable != null) {
			consoleDocking.removeDockable(console);
		}
	}

	/**
	 * Get the organ.
	 * 
	 * @return the organ
	 */
	public OrganSession getOrgan() {
		return session;
	}

	public void setConstructing(boolean constructing) {

		if (this.constructing != constructing) {
			saveDocking();

			this.constructing = constructing;

			loadDocking();

			config.write(this);
		}

		updateHistory();

		updateActions();
	}

	protected void loadDocking() {
		String docking;
		if (constructing) {
			docking = constructDocking;
		} else {
			docking = playDocking;
		}
		if (docking != null) {
			try {
				Reader reader = new StringReader(docking);
				OrganPanelPersister persister = new OrganPanelPersister(reader);
				persister.load();
				return;
			} catch (Exception ex) {
				logger.log(Level.WARNING, "unable to load docking", ex);
			}
		}

		String dockingXml;
		if (constructing) {
			dockingXml = "construct.xml";
		} else {
			dockingXml = "play.xml";
		}
		try {
			Reader reader = new InputStreamReader(getClass()
					.getResourceAsStream(dockingXml));
			OrganPanelPersister persister = new OrganPanelPersister(reader);
			persister.load();
		} catch (Exception error) {
			throw new Error("unable to load default docking");
		}
	}

	protected void saveDocking() {
		try {
			Writer writer = new StringWriter();
			OrganPanelPersister persister = new OrganPanelPersister(writer);
			persister.save();
			String docking = writer.toString();
			if (constructing) {
				constructDocking = docking;
			} else {
				playDocking = docking;
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, "unable to save docking", ex);
		}
	}

	public String getConstructDocking() {
		return constructDocking;
	}

	public void setConstructDocking(String constructDocking) {
		this.constructDocking = constructDocking;
	}

	public String getPlayDocking() {
		return playDocking;
	}

	public void setPlayDocking(String playDocking) {
		this.playDocking = playDocking;
	}

	public void closing() {
		saveDocking();
		config.write(this);
	}

	public void toFront(Console console) {
		Dockable dockable = consoleDocking.getDockable(console);
		if (dockable == null) {
			dockable = addConsoleDockable(console);
		}
		consoleDocking.putDockable(console, dockable);
	}

	/**
	 * The listener to events.
	 */
	private class EventsListener extends OrganAdapter implements PlayListener,
			ProblemListener, SelectionListener, UndoListener, SessionListener {

		public void received(int channel, int command, int data1, int data2) {
			messagesMonitor.input();
		}

		public void sent(int channel, int command, int data1, int data2) {
			messagesMonitor.output();
		}

		public void problemAdded(Problem problem) {
			if (problem.getSeverity() == Severity.ERROR) {
				// TODO open problems dock
			}
		}

		public void problemRemoved(Problem ev) {
		}

		public void constructingChanged(boolean constructing) {
			setConstructing(constructing);
		}

		public void destroyed() {
		}
		
		public void selectionChanged(SelectionEvent ev) {
			if (session.getSelection().getSelectionCount() == 1) {
				Element element = session.getSelection().getSelectedElement();
				if (element instanceof Console) {
					Console console = (Console) element;

					if (consoleDocking.getDockable(console) == null) {
						addConsoleDockable(console);
					}
				}
			}

			updateHistory();
		}

		public void changed() {
			undoAction.setEnabled(session.getUndoManager().canUndo());
			redoAction.setEnabled(session.getUndoManager().canRedo());
		}

		public void elementAdded(Element element) {
			if (element instanceof Console) {
				addConsoleDockable((Console) element);
			}
		}

		public void elementRemoved(Element element) {
			if (element instanceof Console) {
				removeConsoleDockable((Console) element);
			}
		}
	}

	private class DockableAction extends BaseAction {

		private OrganDockable dockable;

		public DockableAction(OrganDockable dockable) {
			this.dockable = dockable;

			setName(dockable.getTitle());
			setSmallIcon(dockable.getIcon());
		}

		public OrganDockable getDockable() {
			return dockable;
		}

		public void update() {
			if (constructing) {
				setEnabled(dockable.forConstruct());
			} else {
				setEnabled(dockable.forPlay());
			}
		}

		public void actionPerformed(ActionEvent ev) {
			docking.putDockable(dockable.getKey(), dockable);
		}
	}

	/**
	 * The action that steps back to the previous element.
	 */
	private class BackAction extends BaseAction {
		private BackAction() {
			config.get("back").read(this);

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			session.getSelection().back();
		}
	}

	/**
	 * The action that steps forward to the next element.
	 */
	private class ForwardAction extends BaseAction {
		private ForwardAction() {
			config.get("forward").read(this);

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			session.getSelection().forward();
		}
	}

	private class UndoAction extends BaseAction {
		private UndoAction() {
			config.get("undo").read(this);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			session.getUndoManager().undo();
		}
	}

	private class RedoAction extends BaseAction {
		private RedoAction() {
			config.get("redo").read(this);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			session.getUndoManager().redo();
		}
	}

	private class OrganPanelPersister extends XMLPersister {

		private OrganPanelPersister(Reader reader) {
			super(docking, reader, DOCKING_VERSION);
		}

		private OrganPanelPersister(Writer writer) {
			super(docking, writer, DOCKING_VERSION);
		}

		@Override
		protected JComponent resolveComponent(Object key) {
			if ("consoles".equals(key)) {
				return consoleDocking;
			} else {
				return null;
			}
		}

		@Override
		protected Dockable resolveDockable(Object key) {
			for (DockableAction action : dockableActions) {
				if (action.getDockable().getKey().equals(key)) {
					return action.getDockable();
				}
			}
			return null;
		}
	}
}
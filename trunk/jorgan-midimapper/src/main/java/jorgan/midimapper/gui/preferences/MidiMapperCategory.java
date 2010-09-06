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
package jorgan.midimapper.gui.preferences;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import jorgan.gui.preferences.category.AppCategory;
import jorgan.gui.preferences.category.JOrganCategory;
import jorgan.midimapper.MidiMapper;
import jorgan.midimapper.MidiMapperProvider;
import bias.Configuration;
import bias.swing.Category;
import bias.util.Property;

/**
 * {@link MidiMapper} category.
 */
public class MidiMapperCategory extends JOrganCategory {

	private static Configuration config = Configuration.getRoot().get(
			MidiMapperCategory.class);

	private Model mappings = getModel(new Property(MidiMapperProvider.class,
			"mappings"));

	private JList list;

	public MidiMapperCategory() {
		config.read(this);
	}

	@Override
	public Class<? extends Category> getParentCategory() {
		return AppCategory.class;
	}

	@Override
	protected JComponent createComponent() {
		JPanel panel = new JPanel(new BorderLayout());

		list = new JList();
		panel.add(list, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		panel.add(buttonPanel, BorderLayout.EAST);

		buttonPanel.add(config.get("add").read(new JButton()));
		buttonPanel.add(config.get("remove").read(new JButton()));

		return panel;
	}

	@Override
	protected void read() {
		list.setModel(new MappingsModel());
	}

	@Override
	protected void write() {
	}

	@SuppressWarnings("unchecked")
	private List<File> getMappings() {
		return (List<File>) mappings.getValue();
	}

	private class MappingsModel extends AbstractListModel {
		@Override
		public int getSize() {
			return getMappings().size();
		}

		@Override
		public Object getElementAt(int index) {
			return getMappings().get(index).getPath();
		}
	}
}
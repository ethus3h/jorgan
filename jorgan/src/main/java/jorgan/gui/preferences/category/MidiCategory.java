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
package jorgan.gui.preferences.category;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jorgan.gui.dock.MonitorDockable;
import jorgan.swing.GridBuilder;
import jorgan.swing.Separator;
import jorgan.swing.GridBuilder.Row;
import bias.Configuration;
import bias.swing.Category;
import bias.util.Property;

/**
 * Category.
 */
public class MidiCategory extends JOrganCategory {

	private static Configuration config = Configuration.getRoot().get(
			MidiCategory.class);

	private Model monitorMax = getModel(new Property(MonitorDockable.class,
			"max"));

	private JSpinner monitorMaxSpinner = new JSpinner(new SpinnerNumberModel(1,
			1, Integer.MAX_VALUE, 50));

	public MidiCategory() {
		config.read(this);
	}

	@Override
	protected JComponent createComponent() {
		JPanel panel = new JPanel();

		GridBuilder builder = new GridBuilder(panel);
		builder.column();
		builder.column().grow().fill();

		builder.row(config.get("monitor").read(new Separator.Label()));

		Row row = builder.row();

		row.cell(config.get("monitorMax").read(new JLabel()));
		row.cell(monitorMaxSpinner);

		return panel;
	}

	@Override
	public Class<? extends Category> getParentCategory() {
		return AppCategory.class;
	}

	@Override
	protected void read() {
		monitorMaxSpinner.setValue(monitorMax.getValue());
	}

	@Override
	protected void write() {
		monitorMax.setValue(monitorMaxSpinner.getValue());
	}
}
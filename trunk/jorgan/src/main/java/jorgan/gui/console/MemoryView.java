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
package jorgan.gui.console;

import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import jorgan.disposition.Memory;
import jorgan.skin.ButtonLayer;
import jorgan.skin.Fill;
import jorgan.skin.Layer;
import jorgan.swing.list.FilterList;

/**
 * A view that shows a {@link Memory}.
 */
public class MemoryView extends IndexedContinuousView<Memory> {

	/**
	 * Constructor.
	 * 
	 * @param memory
	 *            memory to view
	 */
	public MemoryView(Memory memory) {
		super(memory);
	}

	protected Layer createPressableLayer() {
		ButtonLayer layer = new ButtonLayer();
		layer.setBinding(BINDING_POPUP);
		layer.setFill(Fill.BOTH);
		layer.setPadding(new Insets(4, 4, 4, 4));

		return layer;
	}

	@Override
	protected JComponent createPopupContents() {

		FilterList<Integer> filterList = new FilterList<Integer>() {
			@Override
			protected List<Integer> getItems(String filter) {
				List<Integer> items = new ArrayList<Integer>();
				for (int i = 0; i < getElement().getSize(); i++) {
					String title = getElement().getTitle(i);
					if (title.contains(filter)) {
						items.add(i);
					}
				}
				return items;
			}

			@Override
			protected String toString(Integer item) {
				return (item.intValue() + 1) + " - "
						+ getElement().getTitle(item);
			}

			@Override
			protected void onSelectedItem(Integer item) {
				getElement().setIndex(item.intValue());

				closePopup();
			}
		};
		filterList.setOpaque(true);
		filterList.setBackground(new Color(255, 255, 225));

		if (getElement().getIndex() != -1) {
			filterList.setSelectedItem(getElement().getIndex());
		}

		return filterList;
	}
}

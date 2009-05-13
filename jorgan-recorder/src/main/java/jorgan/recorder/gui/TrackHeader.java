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
package jorgan.recorder.gui;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jorgan.disposition.Elements;
import jorgan.disposition.Keyboard;
import jorgan.recorder.SessionRecorder;

public class TrackHeader extends JPanel {

	private SessionRecorder recorder;

	private int track;

	public TrackHeader(SessionRecorder recorder, int track) {
		super(new GridLayout());

		this.recorder = recorder;
		this.track = track;

		JLabel label = new JLabel();
		label.setText(getTitle());
		label.setBorder(new EmptyBorder(2, 2, 2, 2));
		add(label);

		final JPopupMenu menu = new JPopupMenu();
		menu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				init(menu);
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		label.setComponentPopupMenu(menu);
	}

	private String getTitle() {
		Keyboard keyboard = recorder.getKeyboard(track);
		if (keyboard == null) {
			return "Track " + track;
		} else {
			return Elements.getDisplayName(keyboard);
		}
	}

	protected void init(JPopupMenu menu) {
		menu.removeAll();

		for (final Keyboard keyboard : recorder.getSession().getOrgan()
				.getElements(Keyboard.class)) {

			final JCheckBoxMenuItem item = new JCheckBoxMenuItem(Elements
					.getDisplayName(keyboard));
			if (recorder.getKeyboard(track) == keyboard) {
				item.setSelected(true);
			}
			item.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (item.isSelected()) {
						recorder.setKeyboard(track, keyboard);
					}
				}
			});
			menu.add(item);
		}
	}
}

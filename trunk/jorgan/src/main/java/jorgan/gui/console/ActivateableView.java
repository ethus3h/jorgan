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

import java.awt.Graphics2D;
import java.awt.Insets;

import jorgan.disposition.Activateable;
import jorgan.skin.ButtonLayer;
import jorgan.skin.Layer;
import jorgan.skin.Style;
import jorgan.skin.TextLayer;

/**
 * A view for an activateable.
 */
public class ActivateableView extends MomentaryView<Activateable> {

	public static final String BINDING_ACTIVE = "active";

	public static final String BINDING_ACTIVATED = "activated";

	/**
	 * Constructor.
	 * 
	 * @param activateable
	 *            the activateable to view
	 */
	public ActivateableView(Activateable activateable) {
		super(activateable);
	}

	@Override
	protected void initBindings() {
		super.initBindings();

		setBinding(BINDING_ACTIVE, new ButtonLayer.Binding() {
			public boolean isPressable() {
				return true;
			}

			public boolean isPressed() {
				return getElement().isActive();
			}

			public void pressed() {
				getElement().setActive(!getElement().isActive());
			}

			public void released() {
				if (!getElement().isLocking()) {
					getElement().setActive(false);
				}
			};
		});

		setBinding(BINDING_ACTIVATED, new ButtonLayer.Binding() {
			public boolean isPressable() {
				return false;
			}

			public boolean isPressed() {
				return getElement().isActivated();
			}

			public void pressed() {
			}

			public void released() {
			};
		});
	}

	@Override
	protected void shortcutPressed() {
		Activateable activateable = getElement();

		if (activateable.isLocking()) {
			// do nothing - activate/deactivate on release instead)
		} else {
			// umlauts do not trigger KeyEvent.KEY_PRESSED, so these keys cannot
			// be used for non-locking activateables :(
			activateable.setActive(true);
		}
	}

	@Override
	protected void shortcutReleased() {
		Activateable activateable = getElement();

		if (activateable.isLocking()) {
			activateable.setActive(!activateable.isActive());
		} else {
			activateable.setActive(false);
		}
	}

	@Override
	protected Style createDefaultStyle() {
		Style style = new Style();

		style.addChild(createTextLayer());

		style.addChild(createButtonLayer());

		return style;
	}

	private Layer createTextLayer() {
		TextLayer layer = new TextLayer();
		layer.setBinding(CONTROL_NAME);
		layer.setPadding(new Insets(4, 4 + 13 + 4, 4, 4));
		layer.setAnchor(TextLayer.LEFT);
		layer.setFont(getDefaultFont());
		layer.setColor(getDefaultColor());

		return layer;
	}

	private Layer createButtonLayer() {
		ButtonLayer layer = new ButtonLayer();
		layer.setBinding(BINDING_ACTIVE);
		layer.setFill(ButtonLayer.BOTH);

		layer.addChild(createCheckLayer(false));

		layer.addChild(createCheckLayer(true));

		return layer;
	}

	private Layer createCheckLayer(final boolean activated) {
		Layer layer = new Layer() {
			@Override
			protected void draw(Graphics2D g, int x, int y, int width,
					int height) {
				g.setColor(getDefaultColor());

				g.drawRect(x, y, width - 1, height - 1);

				if (activated) {
					g.drawLine(x + 3, y + 5, x + 3, y + 7);
					g.drawLine(x + 4, y + 6, x + 4, y + 8);
					g.drawLine(x + 5, y + 7, x + 5, y + 9);
					g.drawLine(x + 6, y + 6, x + 6, y + 8);
					g.drawLine(x + 7, y + 5, x + 7, y + 7);
					g.drawLine(x + 8, y + 4, x + 8, y + 6);
					g.drawLine(x + 9, y + 3, x + 9, y + 5);
				}
			}
		};
		layer.setWidth(13);
		layer.setHeight(13);
		layer.setPadding(new Insets(4, 4, 4, 4));
		layer.setAnchor(Layer.LEFT);

		return layer;
	}
}
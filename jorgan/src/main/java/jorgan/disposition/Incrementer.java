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
package jorgan.disposition;

/**
 * An incrementer of {@link IndexedContinuous}.
 */
public class Incrementer extends Switch {

	private int delta = 1;
	
	public Incrementer() {
		setLocking(false);
	}

	@Override
	protected boolean canReference(Class<? extends Element> clazz) {
		return IndexedContinuous.class.isAssignableFrom(clazz);
	}

	@Override
	protected void onActivated(boolean active) {
		if (active) {
			increment();		
		}
	}	

// increment on engaged no longer supported for simplicity
//	@Override
//	protected void onEngaged(boolean engaged) {
//		// no need to increment if onActivated() already did it
//		if (engaged && !isActive()) {
//			increment();
//		}
//	}

	private void increment() {
		for (IndexedContinuous continuous : getReferenced(IndexedContinuous.class)) {
			continuous.increment(delta);
		}
	}

	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}
}
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
package jorgan.memory.store;

import jorgan.disposition.Combination;
import jorgan.disposition.Element;
import jorgan.disposition.Reference;

public abstract class ReferenceStore<T extends Element> {

	private T element;

	public ReferenceStore(T element) {
		this.element = element;
	}

	public Element getElement() {
		return element;
	}

	protected abstract void ensureIndex(int index);

	public void read(Combination combination, int index) {
		ensureIndex(index);

		try {
			Reference<?> reference = combination.getReference(getElement());

			read(reference, index);
		} catch (IllegalArgumentException e) {
		}
	}

	protected abstract void read(Reference<?> reference, int index);

	public void write(Combination combination, int index) {
		ensureIndex(index);

		Reference<?> reference = combination.getReference(getElement());

		write(reference, index);
	}

	protected abstract void write(Reference<?> reference, int index);

	public boolean isFor(Object element) {
		return element == this.element;
	}
}
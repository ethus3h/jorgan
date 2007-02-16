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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jorgan.disposition.event.OrganEvent;
import jorgan.disposition.event.OrganListener;

/**
 * The container for all elements of an organ.
 */
public class Organ {

	/**
	 * Registered listeners.
	 */
	private transient List<OrganListener> listeners;

	private List<Element> elements = new ArrayList<Element>();

	private static Class[] elementClasses = new Class[] { Console.class,
			Label.class, Keyboard.class, SoundSource.class, Stop.class,
			Coupler.class, Combination.class, Captor.class, Swell.class,
			Tremulant.class, Variation.class, Sequence.class, Activator.class,
			Regulator.class, Keyer.class, Memory.class, Incrementer.class };

	/**
	 * Get all known element classes.
	 * 
	 * @return element classes
	 */
	public static Class<?>[] getElementClasses() {
		Class<?>[] copy = new Class[elementClasses.length];
		System.arraycopy(elementClasses, 0, copy, 0, elementClasses.length);
		return copy;
	}

	/**
	 * Add a listener to this organ.
	 * 
	 * @param listener
	 *            listener to add
	 */
	public void addOrganListener(OrganListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<OrganListener>();
		}
		listeners.add(listener);
	}

	/**
	 * Remove the given listener.
	 * 
	 * @param listener
	 *            listener to remove
	 * @see #addOrganListener(OrganListener)
	 */
	public void removeOrganListener(OrganListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<OrganListener>();
		}
		listeners.remove(listener);
	}

	/**
	 * Get count of elements.
	 * 
	 * @return element count
	 */
	public int getElementCount() {
		return elements.size();
	}

	public Element getElement(int index) {
		return elements.get(index);
	}

	public List<Element> getElements() {
		return Collections.unmodifiableList(elements);
	}

	public void addElement(Element element) {
		elements.add(element);
		element.setOrgan(this);

		fireElementAdded(element);
	}

	public void removeElement(Element element) {

		elements.remove(element);
		element.setOrgan(null);

		fireElementRemoved(element);
	}

	protected void fireElementChanged(Element element, boolean dispositionChange) {
		if (listeners != null) {
			OrganEvent event = new OrganEvent(this, element, dispositionChange);
			for (int l = 0; l < listeners.size(); l++) {
				OrganListener listener = listeners.get(l);

				listener.elementChanged(event);
			}
		}
	}

	protected void fireElementAdded(Element element) {
		if (listeners != null) {
			OrganEvent event = new OrganEvent(this, element, true);
			for (int l = 0; l < listeners.size(); l++) {
				OrganListener listener = listeners.get(l);

				listener.elementAdded(event);
			}
		}
	}

	protected void fireElementRemoved(Element element) {
		if (listeners != null) {
			OrganEvent event = new OrganEvent(this, element, true);
			for (int l = 0; l < listeners.size(); l++) {
				OrganListener listener = listeners.get(l);

				listener.elementRemoved(event);
			}
		}
	}

	protected void fireReferenceChanged(Element element, Reference reference,
			boolean dispositionChange) {
		if (listeners != null) {
			OrganEvent event = new OrganEvent(this, element, reference,
					dispositionChange);
			for (int l = 0; l < listeners.size(); l++) {
				OrganListener listener = listeners.get(l);

				listener.referenceChanged(event);
			}
		}
	}

	protected void fireReferenceAdded(Element element, Reference reference) {
		if (listeners != null) {
			OrganEvent event = new OrganEvent(this, element, reference, true);
			for (int l = 0; l < listeners.size(); l++) {
				OrganListener listener = listeners.get(l);

				listener.referenceAdded(event);
			}
		}
	}

	protected void fireReferenceRemoved(Element element, Reference reference) {
		if (listeners != null) {
			OrganEvent event = new OrganEvent(this, element, reference, true);
			for (int l = 0; l < listeners.size(); l++) {
				OrganListener listener = listeners.get(l);

				listener.referenceRemoved(event);
			}
		}
	}

	/**
	 * Get candidates to reference from the given element.
	 * 
	 * @param element
	 *            element to get candidates for
	 * @return candidates
	 */
	public List<Element> getReferenceToCandidates(Element element) {

		List<Element> candidates = new ArrayList<Element>();

		for (int c = 0; c < this.elements.size(); c++) {
			Element candidate = this.elements.get(c);

			if (element.canReference(candidate)) {
				candidates.add(candidate);
			}
		}

		return candidates;
	}

	/**
	 * Get candidates which can reference the given elements.
	 * 
	 * @param element
	 *            element to find candidates for
	 * @return candidates, never null
	 */
	public List<Element> getReferencedFromCandidates(Element element) {

		List<Element> candidates = new ArrayList<Element>();

		for (int c = 0; c < this.elements.size(); c++) {
			Element candidate = this.elements.get(c);

			if (candidate.canReference(element)) {
				candidates.add(candidate);
			}
		}

		return candidates;
	}

	/**
	 * Get elements of the given class.
	 * 
	 * @param clazz
	 *            class to give elements for
	 * @return elements
	 */
	@SuppressWarnings("unchecked")
	public <E> List<E> getElements(Class<E> clazz) {
		List<E> list = new ArrayList<E>();

		for (int c = 0; c < this.elements.size(); c++) {
			Element element = this.elements.get(c);

			if (clazz.isInstance(element)) {
				list.add((E) element);
			}
		}

		return list;
	}
}
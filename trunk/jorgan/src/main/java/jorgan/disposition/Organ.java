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

import java.util.*;

import jorgan.disposition.event.*;

/**
 * The container for all elements of an organ.
 */
public class Organ {

  private Class[] elementClasses = new Class[]{Console.class,
                                               Label.class,
                                               Keyboard.class,
                                               SoundSource.class,
                                               Stop.class,
                                               Coupler.class,
                                               Piston.class,
                                               Swell.class,
                                               Tremulant.class,
                                               Variation.class}; 
  
  /**
   * Registered listeners.
   */
  private transient List listeners;

  private List elements = new ArrayList();

  public Class[] getElementClasses() {
    return elementClasses;
  }
  
  public void addOrganListener(OrganListener listener) {
    if (listeners == null){
      listeners = new ArrayList();
    }
    listeners.add(listener);
  }

  public void removeOrganListener(OrganListener listener) {
    if (listeners == null){
      listeners = new ArrayList();
    }
    listeners.remove(listener);
  }

  public int getElementCount() {
      return elements.size();
  }
  
  public Element getElement(int index) {
      return (Element)elements.get(index);
  }

  public List getElements() {
      return new ArrayList(elements);
  }

  public void addElement(Element element) {
      addElement(elements.size(), element);
  }
  
  public void addElement(int index, Element element) {
      elements.add(index, element);
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
        OrganListener listener = (OrganListener)listeners.get(l);

        listener.elementChanged(event);
      }
    }
  }

  protected void fireElementAdded(Element element) {
    if (listeners != null) {
      OrganEvent event = new OrganEvent(this, element, true);
      for (int l = 0; l < listeners.size(); l++) {
        OrganListener listener = (OrganListener)listeners.get(l);

        listener.elementAdded(event);
      }
    }
  }

  protected void fireElementRemoved(Element element) {
    if (listeners != null) {
      OrganEvent event = new OrganEvent(this, element, true);
      for (int l = 0; l < listeners.size(); l++) {
        OrganListener listener = (OrganListener)listeners.get(l);

        listener.elementRemoved(event);
      }
    }
  }
  
  protected void fireReferenceChanged(Element element, Reference reference, boolean dispositionChange) {
    if (listeners != null) {
      OrganEvent event = new OrganEvent(this, element, reference, dispositionChange);
      for (int l = 0; l < listeners.size(); l++) {
        OrganListener listener = (OrganListener)listeners.get(l);

        listener.referenceChanged(event);
      }
    }
  }

  protected void fireReferenceAdded(Element element, Reference reference) {
    if (listeners != null) {
      OrganEvent event = new OrganEvent(this, element, reference, true);
      for (int l = 0; l < listeners.size(); l++) {
        OrganListener listener = (OrganListener)listeners.get(l);

        listener.referenceAdded(event);
      }
    }
  }

  protected void fireReferenceRemoved(Element element, Reference reference) {
    if (listeners != null) {
      OrganEvent event = new OrganEvent(this, element, reference, true);
      for (int l = 0; l < listeners.size(); l++) {
        OrganListener listener = (OrganListener)listeners.get(l);

        listener.referenceRemoved(event);
      }
    }
  }

  public List getCandidates(Class clazz) {
      List candidates = new ArrayList();
      
      for (int e = 0; e < elements.size(); e++) {
          Element element = (Element)elements.get(e);
          if (clazz.isInstance(element)) {
              candidates.add(element);
          }
      }
      
      return candidates;
  }
  
  public List getReferenceToCandidates(List elements) {

    List candidates = new ArrayList();

    candidates:
    for (int c = 0; c < this.elements.size(); c++) {
      Element candidate = (Element)this.elements.get(c);     
      int already = 0;

      for (int e = 0; e < elements.size(); e++) {
        Element element = (Element)elements.get(e);
        
        if (!element.canReference(candidate)) {
          if (element.getReference(candidate) == null) {
            continue candidates;
          }
          already++;
        }
      }

      if (already < elements.size()) {      
        candidates.add(candidate);          
      }
    }
    return candidates;
  }

  public List getReferencedFromCandidates(List elements) {

    List candidates = new ArrayList();

    candidates:
    for (int c = 0; c < this.elements.size(); c++) {
      Element candidate = (Element)this.elements.get(c);
      int already = 0;
        
      for (int e = 0; e < elements.size(); e++) {
        Element element = (Element)elements.get(e);
        if (!candidate.canReference(element)) {
          if (candidate.getReference(element) == null) {
            continue candidates;
          }
          already++;
        }
      }

      if (already < elements.size()) {      
        candidates.add(candidate);          
      }
    }    
    return candidates;
  }
}
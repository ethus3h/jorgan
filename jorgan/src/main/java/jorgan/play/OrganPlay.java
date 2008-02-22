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
package jorgan.play;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import jorgan.disposition.Element;
import jorgan.disposition.Organ;
import jorgan.disposition.event.OrganAdapter;
import jorgan.disposition.event.OrganEvent;
import jorgan.midi.DevicePool;
import jorgan.midi.Direction;
import jorgan.midi.ReceiverWrapper;
import jorgan.midi.TransmitterWrapper;
import jorgan.play.event.PlayListener;
import jorgan.play.spi.ProviderRegistry;
import jorgan.session.ElementProblems;

/**
 * A play of an organ.
 */
public class OrganPlay {

	/**
	 * Only one thread is allowed to change players at a time.
	 */
	private final Object CHANGE_LOCK = new Object();

	/**
	 * Only one receiver is allowed to send a message into players at a time.
	 */
	private final Object RECEIVER_LOCK = new Object();

	private boolean open;

	/**
	 * Element to player mapping.
	 */
	private Map<Element, Player<? extends Element>> players = new HashMap<Element, Player<? extends Element>>();

	/**
	 * The handler of organ and configuration events.
	 */
	private EventHandler eventHandler = new EventHandler();

	/**
	 * All registered playerListeners.
	 */
	private List<PlayListener> listeners = new ArrayList<PlayListener>();

	private ElementProblems problems = new ElementProblems();

	private Organ organ;

	/**
	 * Creates a new organ player.
	 * 
	 * @param organ
	 *            the organ to play
	 */
	public OrganPlay(Organ organ, ElementProblems problems) {
		this.organ = organ;
		this.problems = problems;

		organ.addOrganListener(eventHandler);

		for (Element element : organ.getElements()) {
			createPlayer(element);
		}
	}

	public void destroy() {
		if (isOpen()) {
			close();
		}

		for (Player<? extends Element> player : players.values()) {
			player.setOrganPlay(null);
		}
	}

	public Organ getOrgan() {
		return organ;
	}

	public void addPlayerListener(PlayListener listener) {
		listeners.add(listener);
	}

	public void removePlayerListener(PlayListener listener) {
		listeners.remove(listener);
	}

	protected void fireClosed() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.closed();
			}
		}
	}

	protected void fireOpened() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.opened();
			}
		}
	}

	/**
	 * Fire input.
	 */
	protected void fireInputAccepted() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.inputAccepted();
			}
		}
	}

	/**
	 * Fire output.
	 */
	protected void fireOutputProduced() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.outputProduced();
			}
		}
	}

	protected Player<? extends Element> getPlayer(Element element) {
		return players.get(element);
	}

	public void open() {
		if (open) {
			throw new IllegalStateException("already open");
		}

		synchronized (CHANGE_LOCK) {
			Iterator<Player<? extends Element>> iterator = players.values()
					.iterator();
			while (iterator.hasNext()) {
				Player<? extends Element> player = iterator.next();
				player.open();
			}

			iterator = players.values().iterator();
			while (iterator.hasNext()) {
				Player<? extends Element> player = iterator.next();
				player.elementChanged(null);
			}
		}

		synchronized (RECEIVER_LOCK) {
			open = true;
		}

		fireOpened();
	}

	public boolean isOpen() {
		return open;
	}

	public void close() {
		if (!open) {
			throw new IllegalStateException("not open");
		}

		synchronized (RECEIVER_LOCK) {
			open = false;
		}

		synchronized (CHANGE_LOCK) {
			Iterator<Player<? extends Element>> iterator = players.values()
					.iterator();
			while (iterator.hasNext()) {
				Player<? extends Element> player = iterator.next();
				player.close();
			}
		}

		fireClosed();
	}

	protected void createPlayer(Element element) {
		Player<? extends Element> player = ProviderRegistry
				.createPlayer(element);

		if (player != null) {
			player.setOrganPlay(this);
			players.put(element, player);

			player.elementChanged(null);
		}
	}

	protected void dropPlayer(Element element) {
		Player<? extends Element> player = players.get(element);
		if (player != null) {
			players.remove(element);

			player.setOrganPlay(null);
		}
	}

	private class EventHandler extends OrganAdapter {

		@Override
		public void changed(OrganEvent event) {
			synchronized (CHANGE_LOCK) {
				if (event.self()) {
					Player<? extends Element> player = getPlayer(event
							.getElement());
					if (player != null) {
						player.elementChanged(event);
					}
				}
			}
		}

		@Override
		public void added(OrganEvent event) {
			synchronized (CHANGE_LOCK) {
				if (event.self()) {
					createPlayer(event.getElement());
				}
			}
		}

		@Override
		public void removed(OrganEvent event) {
			synchronized (CHANGE_LOCK) {
				if (event.self()) {
					dropPlayer(event.getElement());
				}
			}
		}
	}

	protected ElementProblems getProblems() {
		return problems;
	}

	/**
	 * Create a transmitter for the device with the given. The returned
	 * transmitter will automatically close the device when
	 * {@link Transmitter#close()} is called on it.<br>
	 * Note: The receiver set on the created transmitter ({@link Transmitter#setReceiver(Receiver)})
	 * will be synchronized, see {@link #RECEIVER_LOCK} and {@link #CHANGE_LOCK}.
	 * 
	 * @param deviceName
	 *            the name of the device
	 * @return transmitter
	 * @throws MidiUnavailableException
	 */
	public Transmitter createTransmitter(String deviceName)
			throws MidiUnavailableException {
		final MidiDevice device = DevicePool.instance().getMidiDevice(
				deviceName, Direction.IN);
		device.open();

		final Transmitter transmitter = device.getTransmitter();

		return new TransmitterWrapper(transmitter) {
			public void close() {
				super.close();

				device.close();
			}

			public void setReceiver(final Receiver receiver) {
				super.setReceiver(new ReceiverWrapper(receiver) {
					public void send(MidiMessage message, long timestamp) {
						synchronized (RECEIVER_LOCK) {
							if (open) {
								synchronized (CHANGE_LOCK) {
									super.send(message, timestamp);
								}
							}
						}
					}
				});
			}
		};
	}

	/**
	 * Create a receiver for the device with the given name. The returned
	 * receiver will automatically close the device when
	 * {@link Receiver#close()} is called on it.
	 * 
	 * @param deviceName
	 *            the name of the device
	 * @return transmitter
	 * @throws MidiUnavailableException
	 */
	public Receiver createReceiver(String deviceName)
			throws MidiUnavailableException {
		final MidiDevice device = DevicePool.instance().getMidiDevice(
				deviceName, Direction.OUT);
		device.open();

		return new ReceiverWrapper(device.getReceiver()) {
			@Override
			public void close() {
				super.close();

				device.close();
			}
		};
	}
}
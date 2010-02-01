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
package jorgan.sams;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import jorgan.midi.DevicePool;
import jorgan.midi.Direction;
import jorgan.midi.Loopback;
import bias.Configuration;

/**
 */
public class SamsDevice extends Loopback {

	private static Configuration config = Configuration.getRoot().get(
			SamsDevice.class);

	private String device = "VirMIDI [hw:1,0]";

	private long maxDelay = 1000;

	private Encoding encoding = new NoteOnOffEncoding();

	private SamsReceiver receiver;

	private SamsTransmitter transmitter;

	private Tab[] tabs = new Tab[128];

	private Thread autoOffThread;

	/**
	 * Create a new midiMerger.
	 * 
	 * @param info
	 *            info to use
	 */
	public SamsDevice(MidiDevice.Info info) {
		super(info, true, true);

		config.read(this);

		for (int t = 0; t < tabs.length; t++) {
			tabs[t] = new Tab(t);
		}
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public long getMaxDelay() {
		return maxDelay;
	}

	public void setMaxDelay(long maxDelay) {
		this.maxDelay = maxDelay;
	}

	public Encoding getEncoding() {
		return encoding;
	}

	public void setEncoding(Encoding encoding) {
		if (encoding == null) {
			throw new IllegalArgumentException("must not be null");
		}

		this.encoding = encoding;
	}

	/**
	 * Overriden to create receivers for all devices to merge.
	 */
	@Override
	public void open() throws MidiUnavailableException {
		super.open();

		try {
			receiver = new SamsReceiver();
			transmitter = new SamsTransmitter();
		} catch (MidiUnavailableException ex) {
			close();

			throw ex;
		}

		autoOffThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (autoOffThread == Thread.currentThread()) {
					long now = System.currentTimeMillis();

					long next = checkAutoOff(now);
					try {
						Thread.sleep(Math.max(0, next - now));
					} catch (InterruptedException interrupted) {
					}
				}
			}
		}, "jOrgan SAMs");
		autoOffThread.start();
	}

	protected long checkAutoOff(long time) {
		long next = Long.MAX_VALUE;

		for (Tab tab : tabs) {
			next = Math.min(tab.checkAutoOff(time), next);
		}

		return next;
	}

	@Override
	public synchronized void close() {
		super.close();

		autoOffThread = null;

		if (receiver != null) {
			receiver.close();
			receiver = null;
		}

		if (transmitter != null) {
			transmitter.close();
			transmitter = null;
		}
	}

	@Override
	protected void onLoopIn(MidiMessage message) {
		if (message instanceof ShortMessage) {
			ShortMessage shortMessage = (ShortMessage) message;

			encoding.decodeChangeTab(this, shortMessage);
		}
	}

	public Tab getTab(int index) {
		return tabs[index];
	}

	public class Tab {

		private int index;

		private Magnet onMagnet = new Magnet();

		private Magnet offMagnet = new Magnet();

		public Tab(int index) {
			this.index = index;
		}

		public synchronized long checkAutoOff(long time) {
			return Math.min(onMagnet.checkAutoOff(time), offMagnet
					.checkAutoOff(time));
		}

		public synchronized void change(boolean on) {
			if (on) {
				offMagnet.off();
				onMagnet.on();
			} else {
				onMagnet.off();
				offMagnet.on();
			}
		}

		public synchronized void onChanged(boolean on) {
			if (on) {
				onMagnet.off();

				loopOut(encoding.encodeTabChanged(index, on));
			} else {
				offMagnet.off();

				loopOut(encoding.encodeTabChanged(index, on));
			}
		}

		private class Magnet {
			private long autoOff = Long.MAX_VALUE;

			private boolean isOn() {
				return autoOff < Long.MAX_VALUE;
			}

			public void on() {
				if (!isOn()) {
					autoOff = System.currentTimeMillis() + maxDelay;
					autoOffThread.interrupt();

					ShortMessage message;
					if (onMagnet == this) {
						message = encoding.encodeOnMagnet(index, true);
					} else {
						message = encoding.encodeOffMagnet(index, true);
					}
					transmitter.transmit(message);
				}
			}

			public void off() {
				if (isOn()) {
					autoOff = Long.MAX_VALUE;

					ShortMessage message;
					if (onMagnet == this) {
						message = encoding.encodeOnMagnet(index, false);
					} else {
						message = encoding.encodeOffMagnet(index, false);
					}
					transmitter.transmit(message);
				}
			}

			public long checkAutoOff(long time) {
				if (isOn()) {
					if (time > autoOff) {
						off();
					}
				}

				return autoOff;
			}
		}
	}

	private class SamsReceiver implements Receiver {

		private MidiDevice device;

		private Transmitter transmitter;

		public SamsReceiver() throws MidiUnavailableException {

			this.device = DevicePool.instance().getMidiDevice(getDevice(),
					Direction.IN);
			this.device.open();

			try {
				transmitter = this.device.getTransmitter();
				transmitter.setReceiver(this);
			} catch (MidiUnavailableException ex) {
				this.device.close();

				throw ex;
			}
		}

		@Override
		public void send(MidiMessage message, long timeStamp) {
			if (message instanceof ShortMessage) {
				ShortMessage shortMessage = (ShortMessage) message;

				encoding.decodeTabChanged(SamsDevice.this, shortMessage);
			}
		}

		@Override
		public void close() {
			transmitter.close();
			device.close();
		}
	}

	private class SamsTransmitter {

		private MidiDevice device;

		private Receiver receiver;

		public SamsTransmitter() throws MidiUnavailableException {
			this.device = DevicePool.instance().getMidiDevice(getDevice(),
					Direction.OUT);
			this.device.open();

			try {
				this.receiver = this.device.getReceiver();
			} catch (MidiUnavailableException ex) {
				this.device.close();

				throw ex;
			}
		}

		public void close() {
			device.close();
		}

		public void transmit(MidiMessage message) {
			receiver.send(message, -1);
		}
	}
}
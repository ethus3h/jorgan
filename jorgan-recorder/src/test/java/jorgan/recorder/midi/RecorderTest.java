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
package jorgan.recorder.midi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import junit.framework.TestCase;

public class RecorderTest extends TestCase {

	public void test() throws Exception {
		Recorder recorder = new Recorder(1);
		recorder.addListener(new RecorderAdapter() {

			public void played(int track, MidiMessage message) {
				trace("played ", track, message);
			}

			@Override
			public void starting() {
				System.out.println("Starting");
			}

			@Override
			public void stopping() {
				System.out.println("Stopping");
			}

			@Override
			public void end(long millis) {
				synchronized (RecorderTest.this) {
					RecorderTest.this.notify();
				}
			}
		});

		recorder.start();

		keyPressed(recorder, 60, 100);

		Thread.sleep(1000);

		keyReleased(recorder, 60);
		keyPressed(recorder, 62, 100);

		recorder.stop();

		Thread.sleep(2000);

		recorder.start();

		Thread.sleep(1000);

		keyReleased(recorder, 62);
		keyPressed(recorder, 64, 100);

		Thread.sleep(1000);

		keyReleased(recorder, 64);

		Thread.sleep(2000);

		recorder.stop();

		OutputStream output = new FileOutputStream("recorder.mid");
		try {
			recorder.save(output);
		} finally {
			output.close();
		}

		InputStream input = new FileInputStream("recorder.mid");
		try {
			recorder.load(input);
		} finally {
			input.close();
		}

		recorder.start();

		synchronized (this) {
			wait();
		}
	}

	private void keyPressed(Recorder recorder, int pitch, int velocity) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(144, pitch, velocity);
		} catch (InvalidMidiDataException ex) {
			throw new IllegalArgumentException(ex);
		}
		recorder.record(0, message);
	}

	private void keyReleased(Recorder recorder, int pitch) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(128, pitch, 0);
		} catch (InvalidMidiDataException ex) {
			throw new IllegalArgumentException(ex);
		}
		recorder.record(0, message);
	}

	private void trace(String event, int track, MidiMessage message) {
		System.out.print(event);
		System.out.print("[" + track + "] ");

		byte[] bytes = message.getMessage();
		for (int b = 0; b < message.getLength(); b++) {
			System.out.print(' ');
			System.out.print(bytes[b] & 0xFF);
		}
		System.out.println();
	}
}
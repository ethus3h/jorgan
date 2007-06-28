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

import javax.sound.midi.ShortMessage;

import jorgan.disposition.Initiator;
import jorgan.disposition.Message;
import jorgan.disposition.event.OrganEvent;

/**
 * A player for an {@link jorgan.disposition.Initiator}.
 */
public class InitiatorPlayer extends Player<Initiator> {

	private static final Problem warningMessage = new Problem(Problem.WARNING,
			"message");

	public InitiatorPlayer(Initiator initiator) {
		super(initiator);
	}

	public void messageReceived(ShortMessage shortMessage) {
		Initiator initiator = getElement();

		Message message = initiator.getMessage();
		if (message != null
				&& message.match(shortMessage.getStatus(), shortMessage.getData1(),
						shortMessage.getData2())) {

			fireInputAccepted();

			initiator.initiate();
		}
	}

	public void elementChanged(OrganEvent event) {

		Initiator initiator = getElement();

		if (initiator.getMessage() == null && getWarnMessage()) {
			addProblem(warningMessage.value(null));
		} else {
			removeProblem(warningMessage);
		}
	}
}
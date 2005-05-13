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
package jorgan.sound.midi;

/**
 * A channel.
 */
public interface Channel {

  /**
   * Get the MIDI number of this channel (1 - 16).
   * 
   * @return MIDI number
   */
  public int getNumber();
  
  /**
   * Send a MIDI message.
   *
   * @param command   command
   * @param data1     data1
   * @param data2     data2
   */
  public void sendMessage(int command, int data1, int data2);

  public void release();
}
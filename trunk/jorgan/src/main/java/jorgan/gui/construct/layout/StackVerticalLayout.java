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
package jorgan.gui.construct.layout;

import java.util.*;

import jorgan.gui.construct.Configuration;
import jorgan.gui.console.View;

public class StackVerticalLayout extends ViewLayout {

  private int grid;
  private int x;
  private int y;
  
  public StackVerticalLayout(int x, int y) {
    super(null, null);
    
    grid = Configuration.instance().getGrid();

    this.x = x - (x % grid);
    this.y = y - (y % grid);    
  }

  protected void init(View notUsed, List views) {    
  }
  
  protected void visit(View view, int index) {
    view.setPosition(x, y);
    
    int height = view.getHeight(); 
    y += (height - (height % grid)) + grid;
  }
}

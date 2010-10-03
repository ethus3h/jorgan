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
package jorgan.swing.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * Utility method for trees.
 */
public class TreeUtils {

	/**
	 * Expand all nodes.
	 * 
	 * @param tree
	 */
	public static <T> void expand(JTree tree, T t) {
		for (TreePath path : getModel(tree).getPaths(t)) {
			tree.expandPath(path);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getSelection(JTree tree) {
		List<T> selection = new ArrayList<T>();

		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null) {
			for (TreePath path : paths) {
				selection.add((T) path.getLastPathComponent());
			}
		}

		return selection;
	}

	public static <T> void addSelection(JTree tree, T t) {
		for (TreePath path : getModel(tree).getPaths(t)) {
			tree.addSelectionPath(path);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> BaseTreeModel<T> getModel(JTree tree) {
		return (BaseTreeModel<T>) tree.getModel();
	}

	public static <T> void setSelection(JTree tree, List<T> selection) {
		BaseTreeModel<T> model = getModel(tree);

		List<TreePath> paths = new ArrayList<TreePath>();
		for (T t : selection) {
			paths.addAll(model.getPaths(t));
		}
		tree.setSelectionPaths(paths.toArray(new TreePath[paths.size()]));
	}

	public static <T> void scrollPathToVisible(JTree tree, T t) {
		for (TreePath path : getModel(tree).getPaths(t)) {
			tree.scrollPathToVisible(path);
		}
	}

	public static void collapseAll(JTree tree) {
		for (int row = tree.getRowCount() - 1; row >= 0; row--) {
			tree.collapseRow(row);
		}
	}

	public static <T> Selection<T> selection(JTree tree) {
		return new Selection<T>(tree);
	}

	public static <T> Expansion<T> expansion(JTree tree) {
		return new Expansion<T>(tree);
	}

	public static class Selection<T> {

		private JTree tree;

		private List<T> selected = new ArrayList<T>();

		@SuppressWarnings("unchecked")
		public Selection(JTree tree) {
			this.tree = tree;

			TreePath[] paths = tree.getSelectionPaths();
			if (paths != null) {
				for (TreePath path : paths) {
					selected.add((T) path.getLastPathComponent());
				}
			}
		}

		public void recall() {
			BaseTreeModel<T> model = getModel(tree);

			boolean scrolled = false;
			for (T node : selected) {
				for (TreePath path : model.getPaths(node)) {
					tree.addSelectionPath(path);

					if (!scrolled) {
						scrolled = true;
						tree.scrollPathToVisible(path);
					}
				}
			}
		}
	}

	public static class Expansion<T> {

		private JTree tree;

		private List<T> expanded = new ArrayList<T>();

		@SuppressWarnings("unchecked")
		public Expansion(JTree tree) {
			this.tree = tree;

			Enumeration<TreePath> paths = tree
					.getExpandedDescendants(new TreePath(BaseTreeModel.ROOT));
			while (paths.hasMoreElements()) {
				TreePath path = paths.nextElement();
				if (path.getPathCount() > 1) {
					expanded.add((T) path.getLastPathComponent());
				}
			}
		}

		public void recall() {
			BaseTreeModel<T> model = getModel(tree);

			for (T node : expanded) {
				for (TreePath path : model.getPaths(node)) {
					tree.expandPath(path);
				}
			}
		}
	}
}
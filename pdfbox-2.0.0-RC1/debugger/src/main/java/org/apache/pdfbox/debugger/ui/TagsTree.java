package org.apache.pdfbox.debugger.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.pdfbox.debugger.ui.tags.MarkedContentNode;
import org.apache.pdfbox.debugger.ui.tags.StructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;

public class TagsTree extends JTree {

	private static final long serialVersionUID = 1L;

	private final JPopupMenu treePopupMenu;

	public TagsTree(Component parentComponent) {
		this.treePopupMenu = new JPopupMenu();
		setComponentPopupMenu(this.treePopupMenu);
	}

	@Override
	public Point getPopupLocation(MouseEvent event) {
		if (event != null) {
			TreePath path = getClosestPathForLocation(event.getX(), event.getY());
			if (path == null) {
				return null;
			}
			setSelectionPath(path);
			treePopupMenu.removeAll();
			for (JMenuItem menuItem : getPopupMenuItems(path)) {
				treePopupMenu.add(menuItem);
			}
			return event.getPoint();
		}
		return null;
	}

	private List<JMenuItem> getPopupMenuItems(TreePath nodePath) {
		Object obj = nodePath.getLastPathComponent();
		List<JMenuItem> menuItems = new ArrayList<JMenuItem>();

		menuItems.add(getTreePathMenuItem(obj));
		return menuItems;
	}

	private JMenuItem getTreePathMenuItem(final Object obj) {
		JMenuItem menuItem = new JMenuItem("Properties...");
		
		if (obj instanceof MarkedContentNode) {
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					showProperty(obj.toString());
				}
			});
		} else if (obj instanceof StructureNode) {
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					showProperty(obj.toString());
				}
			});
		} else if (obj instanceof PDStructureTreeRoot) {
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					showProperty(obj.toString());
				}
			});
		}
		
		return menuItem;
	}

	private void showProperty(String prop) {
		JOptionPane.showMessageDialog(this, prop);
	}
}

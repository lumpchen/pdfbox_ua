package org.apache.pdfbox.debugger.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.pdfbox.debugger.treestatus.TreeStatus;

public class TagsTree extends JTree {

	private static final long serialVersionUID = 1L;

	private final JPopupMenu treePopupMenu;
	private final Component parent;
	private final Object rootNode;

	public TagsTree(Component parentComponent) {
		this.treePopupMenu = new JPopupMenu();
		setComponentPopupMenu(this.treePopupMenu);
		this.parent = parentComponent;
		this.rootNode = this.getModel().getRoot();
	}

	@Override
	public Point getPopupLocation(MouseEvent event) {
		if (event != null) {
			TreePath path = getClosestPathForLocation(event.getX(),
					event.getY());
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

		menuItems.add(getTreePathMenuItem(nodePath));

		if (obj instanceof MapEntry) {
			obj = ((MapEntry) obj).getValue();
		} else if (obj instanceof ArrayEntry) {
			obj = ((ArrayEntry) obj).getValue();
		}

		return menuItems;
	}

	private JMenuItem getTreePathMenuItem(final TreePath path) {
		JMenuItem copyPathMenuItem = new JMenuItem("Copy Tree Path");
		copyPathMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				clipboard.setContents(new StringSelection(new TreeStatus(
						rootNode).getStringForPath(path)), null);
			}
		});
		return copyPathMenuItem;
	}

}

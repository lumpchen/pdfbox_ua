package org.apache.pdfbox.debugger.ui.tags;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PDFPageTreeModel  implements TreeModel {
	
	private PDDocument document;
	private int pageCount;
	private PageIndexNode root;
	private List<PageIndexNode> children;
	
	public PDFPageTreeModel(PDDocument document) {
		this.document = document;
		this.pageCount = document.getNumberOfPages();
		this.root = new PageIndexNode();
		
		this.children = new ArrayList<PageIndexNode>();
		for (int i = 0; i < this.pageCount; i++) {
			this.children.add(new PageIndexNode(i));
		}
	}
	
	public PDPage getPDPage(PageIndexNode node) {
		if (node.isRoot()) {
			return null;
		}
		return this.document.getPage(node.getIndex());
	}
	
	@Override
	public Object getRoot() {
		return this.root;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == this.root) {
			return this.children.get(index);
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == this.root) {
			return this.children.size();
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == this.root) {
			return false;
		}
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

}

package org.apache.pdfbox.debugger.pagepane;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.pdfbox.debugger.ui.TagsTree;
import org.apache.pdfbox.debugger.ui.tags.PDFTagsTreeModel;
import org.apache.pdfbox.pdmodel.PDDocument;

public class TagsLoaderWorker extends SwingWorker<PDFTagsTreeModel, Integer> {

	private PDDocument pdf;
	private TagsTree tagsTree;

	public TagsLoaderWorker(PDDocument pdf, TagsTree tagsTree) {
		this.pdf = pdf;
		this.tagsTree = tagsTree;
	}

	@Override
	protected PDFTagsTreeModel doInBackground() throws Exception {
		tagsTree.setModel(new TagsLoadingTreeModel("Loading..."));

		PDFTagsTreeModel treeModel = new PDFTagsTreeModel(this.pdf);
		return treeModel;
	}

	@Override
	protected void done() {
		try {
			this.tagsTree.setModel(this.get());
			this.tagsTree.repaint();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public static class TagsLoadingTreeModel implements TreeModel {

		private String root;
		public TagsLoadingTreeModel(String root) {
			this.root = root;
		}
		
		@Override
		public Object getRoot() {
			return this.root;
		}

		@Override
		public Object getChild(Object parent, int index) {
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			return 0;
		}

		@Override
		public boolean isLeaf(Object node) {
			return false;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			return 0;
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
		}
		
	}
}

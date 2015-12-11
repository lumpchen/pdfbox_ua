package org.apache.pdfbox.debugger.ui.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.rendering.DocumentStructureExtractor;

public class PDFTagsTreeModel implements TreeModel {

	private PDStructureTreeRoot structureRoot;
	private DocumentStructureExtractor pageStructureExtractor;
	private List<StructureNode> documentNodes;
	private String lang;
	
	public PDFTagsTreeModel(PDDocument document) {
		this.structureRoot = document.getDocumentCatalog().getStructureTreeRoot();
		this.lang = document.getDocumentCatalog().getLanguage();
		this.pageStructureExtractor = new DocumentStructureExtractor(document);
		this.loadTags();
	}
	
	public void loadTags() {
		this.documentNodes = new ArrayList<StructureNode>();
		List<Object> kids = this.structureRoot.getKids();
		if (kids != null && !kids.isEmpty()) {
			for (Object kid : kids) {
				if (kid instanceof PDStructureElement) {
					try {
						this.documentNodes.add(new StructureNode((PDStructureElement) kid, null, this));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public List<MarkedContentNode> getPageMarkedContents(PDPage page) throws IOException {
		List<MarkedContentNode> ret = new ArrayList<MarkedContentNode>();
		for (StructureNode node : this.documentNodes) {
			List<MarkedContentNode> mcNodes = node.getAllMarkedContentNode();
			for (MarkedContentNode mcNode : mcNodes) {
				if (mcNode.getPage() != null && mcNode.getPage().getCOSObject() == page.getCOSObject()) { // equals?
					ret.add(mcNode);
				}
			}
		}
		return ret;
	}
	
	public List<ReadingText> getReadingText(PDPage page) throws IOException {
		List<ReadingText> textList = new ArrayList<ReadingText>();
		
		List<MarkedContentNode> mcNodes = this.getPageMarkedContents(page);
		for (MarkedContentNode mcNode : mcNodes) {
			textList.add(new ReadingText(mcNode.getContentString(), mcNode.getLang()));
		}
		
		return textList;
	}
	
	public String getLang() {
		return this.lang;
	}
	
	@Override
	public Object getRoot() {
		return this.structureRoot;
	}

	public DocumentStructureExtractor getPageStructureExtractor() {
		return this.pageStructureExtractor;
	}
	
	@Override
	public Object getChild(Object parent, int index) {
		try {
			Object ret = null;
			if (parent instanceof PDStructureTreeRoot) {
				ret = this.documentNodes.get(index);
			} else if (parent instanceof StructureNode) {
				StructureNode entry = (StructureNode) parent;
				ret = entry.getChildNode(index);
			} else {
				// should not be here
			}
			return ret;
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public int getChildCount(Object parent) {
		int count = 0;
		if (parent instanceof PDStructureTreeRoot) {
			List<Object> kids = ((PDStructureTreeRoot) parent).getKids();
			count = kids.size();
		} else if (parent instanceof StructureNode) {
			count = ((StructureNode) parent).getChildCount();
		} 
		return count;
	}

	@Override
	public boolean isLeaf(Object node) {
		boolean isLeaf = false;
		if (node instanceof PDStructureTreeRoot) {
			List<Object> kids = ((PDStructureTreeRoot) node).getKids();
			isLeaf = kids.isEmpty();
		} else if (node instanceof StructureNode) {
			isLeaf = ((StructureNode) node).getChildCount() == 0;
		} else {
			isLeaf = true;
		}
		return isLeaf;
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

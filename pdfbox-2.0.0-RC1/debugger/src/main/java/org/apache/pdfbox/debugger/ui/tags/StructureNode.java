package org.apache.pdfbox.debugger.ui.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkedContentReference;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDObjectReference;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;

public class StructureNode {

	private PDStructureElement structure;
	private PDFTagsTreeModel model;
	private List<Object> children;

	public StructureNode(PDStructureElement structure, PDFTagsTreeModel model) throws IOException {
		this.structure = structure;
		this.model = model;
		this.init();
	}
	
	private void init() throws IOException {
		if ( this.structure.getKids() == null ||  this.structure.getKids().isEmpty()) {
			return;
		}
		int count =  this.structure.getKids().size();
		this.children = new ArrayList<Object>(count);
		for (int i = 0; i < count; i++) {
			Object item =  this.structure.getKids().get(i);
			this.children.add(this.createEntry(item));
		}
	}
	
	private Object createEntry(Object item) throws IOException {
		if (item instanceof PDStructureElement) {
			return new StructureNode((PDStructureElement) item, this.model);
		} else if (item instanceof Integer) { // MCID
			MarkedContentNode mcEntry = new MarkedContentNode((Integer) item, this, this.model);
			return mcEntry;
		} else if (item instanceof PDMarkedContentReference) {
			// MCR
			MarkedContentNode mcEntry = new MarkedContentNode((PDMarkedContentReference) item, this, this.model);
			return mcEntry;
		} else if (item instanceof PDObjectReference) {
			MarkedContentNode mcEntry = new MarkedContentNode((PDObjectReference) item, this, this.model);
			return mcEntry;
		}
		return null;
	}
	
	public Object getChildNode(int index) throws IOException {
		if (this.children == null || this.children.isEmpty()) {
			return null;
		}
		if (index < 0 || index >= this.children.size()) {
			return null;
		}
		return this.children.get(index);
	}
	
	public PDPage getPage() {
		return this.structure.getPage();
	}
	
	public int getChildCount() {
		return this.structure.getKids().size();
	}
	
	public String getAlt() {
		if (this.structure.getAlternateDescription() != null) {
			return this.structure.getAlternateDescription();
		}
		String type = this.structure.getStandardStructureType();
		if ("Link".equalsIgnoreCase(type)) {
			for (Object obj : this.children) {
				if (obj instanceof MarkedContentNode) {
					MarkedContentNode mcNode = (MarkedContentNode) obj;
					if (mcNode.isAnnotRef() && mcNode.getAnnotContents() != null) {
						return mcNode.getAnnotContents();
					}
				}
			}
		} else if ("Form".equalsIgnoreCase(type)) {
			for (Object obj : this.children) {
				if (obj instanceof MarkedContentNode) {
					MarkedContentNode mcNode = (MarkedContentNode) obj;
					if (mcNode.isAnnotRef() && mcNode.getAnnotContents() != null) {
						return mcNode.getAnnotContents();
					}
				}
			}
		}
		return null;
	}
	
	public List<MarkedContentNode> getAllMarkedContentNode() throws IOException {
		int count = this.getChildCount();
		List<MarkedContentNode> ret = new ArrayList<MarkedContentNode>();
		for (int i = 0; i < count; i++) {
			Object obj = this.getChildNode(i);
			if (obj instanceof StructureNode) {
				ret.addAll(((StructureNode) obj).getAllMarkedContentNode());
			} else if (obj instanceof MarkedContentNode) {
				ret.add((MarkedContentNode) obj);
			}
		}
		return ret;
	}
	
	public String getReadingText() throws IOException {
		int count = this.getChildCount();
		StringBuilder buf = new StringBuilder();
		if (this.getAlt() != null) {
			buf.append(this.getAlt());
			buf.append(" ");
			return buf.toString();
		}
		
		for (int i = 0; i < count; i++) {
			Object obj = this.getChildNode(i);
			if (obj instanceof StructureNode) {
				buf.append(((StructureNode) obj).getReadingText());
			} else if (obj instanceof MarkedContentNode) {
				buf.append(((MarkedContentNode) obj).getContentString());
				buf.append(" ");
			}
		}
		return buf.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		String type = this.structure.getType();
		if (type != null && type.equalsIgnoreCase("StructElem")) {
//			buf.append(this.structure.getStandardStructureType());
    	} 
		
		if (this.structure.getStandardStructureType() != null) {
			buf.append(this.structure.getStandardStructureType());
		}
		
		if (this.structure.getTitle() != null) {
			buf.append(" " + this.structure.getTitle());
		}
		
		return buf.toString();
	}
}

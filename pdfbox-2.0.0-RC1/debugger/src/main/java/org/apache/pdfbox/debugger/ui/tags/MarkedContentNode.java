package org.apache.pdfbox.debugger.ui.tags;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkedContentReference;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDObjectReference;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

public class MarkedContentNode {

	private int mcid = -1;
	private PDPage page;
	private String stmRef;
	private StructureNode parent;
	
	private String contentString;
	private PDMarkedContent markedContent;
	
	private boolean isAnnotRef = false;
	private String annotSubType;
	private String annotContents;
	private Area annotOutline;
	
	private PDFTagsTreeModel model;

	public MarkedContentNode(int mcid, StructureNode parent, PDFTagsTreeModel model) throws IOException {
		this.mcid = mcid;
		this.page = parent.getPage();
		this.parent = parent;
		this.model = model;
		this.findMarkedContent();
	}

	public MarkedContentNode(PDMarkedContentReference mcRef, StructureNode parent, PDFTagsTreeModel model) throws IOException {
		this.mcid = mcRef.getMCID();
		this.page = mcRef.getPage();
		if (mcRef.getStm() != null) {
			this.stmRef = PDMarkedContent.createXObjectRefTag(mcRef.getStm());			
		}
		
		this.parent = parent;
		this.model = model;
		this.findMarkedContent();
	}
	
	public MarkedContentNode(PDObjectReference objRef, StructureNode parent, PDFTagsTreeModel model) throws IOException {
		this.mcid = -1;
		this.parent = parent;
		this.model = model;
		COSObject obj = (COSObject) objRef.getCOSObject().getItem(COSName.OBJ);
		COSObjectable refObj = objRef.getReferencedObject();
		if (refObj != null) {
			this.isAnnotRef = true;
			if (refObj instanceof PDAnnotation) {
				PDAnnotation annot = (PDAnnotation) refObj;
				this.page = annot.getPage();
				this.annotContents = annot.getContents();

				if (refObj instanceof PDAnnotationLink) {
					this.annotContents = annot.getContents();
				} else if (refObj instanceof PDAnnotationWidget) {
					this.annotContents = ((PDAnnotationWidget) refObj).getTU();
				}
				
				this.annotSubType = annot.getSubtype();
				PDRectangle rect = annot.getRectangle();
				if (rect != null) {
					this.annotOutline = new Area(rect.toGeneralPath());					
				}
			}
		}
		
		this.contentString = "Object Reference - " + obj.getObjectNumber() + " - " 
				+ (this.annotSubType == null ? PDObjectReference.TYPE : this.annotSubType);
	}
	
	private void findMarkedContent() throws IOException {
		List<PDMarkedContent> contents = this.model.getPageStructureExtractor().extract(this.page);
		
		for (PDMarkedContent content : contents) {
			if (content.isArtifact()) {
				// process Artifact
				continue;
			}
			
			if (this.stmRef != null) {
				if (this.findMarkedContent(content)) {
					break;
				}
			} else {
				if (content.getMCID() >= 0 && content.getMCID() == this.mcid) {
					this.markedContent = content;
					this.contentString = content.getContentString();
					break;
				}
			}
		}
	}
	
	private boolean findMarkedContent(PDMarkedContent node) {
		if (node.getContents() == null || node.getContents().isEmpty()) {
			return false;
		}
		for (Object content : node.getContents()) {
			if (content instanceof PDMarkedContent) {
				if (this.stmRef.equals(((PDMarkedContent) content).getXObjectRefTag())) {
					this.contentString = "xobject: " + this.parent.getAlt();
					this.markedContent = (PDMarkedContent) content;
					return true;
				}
			}
		}
		return false;
	}
	
	public PDMarkedContent getMarkedContent() {
		return this.markedContent;
	}

	public String getContentString() {
		return this.contentString;
	}
	
	public int getMCID() {
		return this.mcid;
	}
	
	public PDPage getPage() {
		return this.page;
	}

	public StructureNode getParent() {
		return this.parent;
	}

	public Area getOutlineArea() {
		if (this.markedContent != null) {
			return this.markedContent.getOutlineArea();
		}
		if (this.isAnnotRef) {
			return this.annotOutline;
		}
		return null;
	}
	
	public String getTooltip() {
		return this.parent.getAlt();
	}

	public boolean isAnnotRef() {
		return this.isAnnotRef;
	}
	
	public String getAnnotContents() {
		return this.annotContents;
	}
	
	public String getAnnotSubType() {
		return this.annotSubType;
	}
	
	public String getLang() {
		StructureNode parent = this.getParent();
		while (parent != null) {
			if (parent.getLang() != null) {
				return parent.getLang();
			}
			parent = parent.getParent();
		}
		return this.model.getLang();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (this.mcid >= 0) {
			buf.append(mcid + " - ");			
		}
		
		if (this.contentString != null && !this.contentString.isEmpty()) {
			buf.append(this.contentString);
		} else if (this.parent.getAlt() != null) {
			buf.append(this.parent.getAlt());
		}
		return buf.toString();
	}
}

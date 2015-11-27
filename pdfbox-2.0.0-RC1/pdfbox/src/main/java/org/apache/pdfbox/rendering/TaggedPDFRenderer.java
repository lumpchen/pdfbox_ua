package org.apache.pdfbox.rendering;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;

public class TaggedPDFRenderer extends PDFRenderer {
	
	public TaggedPDFRenderer(PDDocument document) {
		super(document);
	}
	
	private void loadTags() {
		if (this.document == null) {
			return;
		}
		PDStructureTreeRoot treeRoot = this.document.getDocumentCatalog().getStructureTreeRoot();
		if (treeRoot == null) {
			// not a tagged PDF
			return;
		}
		this.loadTags(treeRoot);
	}
	
	private void loadTags(PDStructureTreeRoot treeRoot) {
		
	}
}

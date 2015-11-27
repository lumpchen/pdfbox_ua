package org.apache.pdfbox.rendering;

import java.util.List;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequence;
import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequenceWithProperties;
import org.apache.pdfbox.contentstream.operator.markedcontent.EndMarkedContentSequence;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

public abstract class StructuredPDFStreamEngine extends PDFGraphicsStreamEngine {

	public StructuredPDFStreamEngine(PDPage page) {
		super(page);

		addOperator(new BeginMarkedContentSequence());
		addOperator(new BeginMarkedContentSequenceWithProperties());
		addOperator(new EndMarkedContentSequence());
	}

	
	abstract public void beginMarkedContentSequence(COSName tag, COSDictionary properties);

	abstract public void endMarkedContentSequence();

	abstract public void beginXObject(COSObject xobject);
	
	abstract public void endXObject();
	
	abstract public List<PDMarkedContent> getMarkedContentList();
}

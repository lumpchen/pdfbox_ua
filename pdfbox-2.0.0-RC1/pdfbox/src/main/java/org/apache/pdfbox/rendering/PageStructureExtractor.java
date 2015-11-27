package org.apache.pdfbox.rendering;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType0;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

public class PageStructureExtractor extends StructuredPDFStreamEngine {

	private List<PDMarkedContent> contents = new ArrayList<PDMarkedContent>();
	private Map<Integer, PDMarkedContent> markedContentMap = new HashMap<Integer, PDMarkedContent>();
	private Stack<PDMarkedContent> runtimeMarkedContentStack = new Stack<PDMarkedContent>();
	private Stack<COSObject> xobjectStack = new Stack<COSObject>();
	private StringBuilder textBuffer = new StringBuilder();
	
    // clipping winding rule used for the clipping path
    private int clipWindingRule = -1;
    // glyph cache
    private final Map<PDFont, Glyph2D> fontGlyph2D = new HashMap<PDFont, Glyph2D>();
    private GeneralPath linePath = new GeneralPath();

	public PageStructureExtractor(PDPage page) {
		super(page);
	}

	public void extract() throws IOException {
		this.processPage(this.getPage());
		
        for (PDAnnotation annotation : getPage().getAnnotations()) {
            showAnnotation(annotation);
        }
	}
	
    @Override
    public void showAnnotation(PDAnnotation annotation) throws IOException {
    	this.beginAnnot(annotation);
        super.showAnnotation(annotation);
        this.endAnnot(annotation);
    }
    
	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        // to ensure that the path is created in the right direction, we have to create
        // it by combining single lines instead of creating a simple rectangle
        linePath.moveTo((float) p0.getX(), (float) p0.getY());
        linePath.lineTo((float) p1.getX(), (float) p1.getY());
        linePath.lineTo((float) p2.getX(), (float) p2.getY());
        linePath.lineTo((float) p3.getX(), (float) p3.getY());

        // close the subpath instead of adding the last line so that a possible set line
        // cap style isn't taken into account at the "beginning" of the rectangle
        linePath.closePath();
	}

	@Override
	public void drawImage(PDImage pdImage) throws IOException {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();		
        
        AffineTransform imageTransform = new AffineTransform(at);
        Rectangle rect = new Rectangle(0, 0, 1, 1);
        Shape outline = imageTransform.createTransformedShape(rect);
        this.markImage(new GeneralPath(outline));
	}

	@Override
	public void clip(int windingRule) throws IOException {
		this.clipWindingRule = windingRule;
	}

	@Override
	public void moveTo(float x, float y) throws IOException {
		this.linePath.moveTo(x, y);		
	}

	@Override
	public void lineTo(float x, float y) throws IOException {
		this.linePath.lineTo(x, y);
	}

	@Override
	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
		this.linePath.curveTo(x1, y1, x2, y2, x3, y3);
	}

	@Override
	public Point2D getCurrentPoint() throws IOException {
		return this.linePath.getCurrentPoint();
	}

	@Override
	public void closePath() throws IOException {
		this.linePath.closePath();
	}

	@Override
	public void endPath() throws IOException {
        if (this.clipWindingRule != -1) {
            this.linePath.setWindingRule(this.clipWindingRule);
            getGraphicsState().intersectClippingPath(this.linePath);
            this.clipWindingRule = -1;
        }
        this.linePath.reset();		
	}

	@Override
	public void shadingFill(COSName shadingName) throws IOException {
	}
	
    protected final GeneralPath getLinePath() {
        return linePath;
    }
    
	@Override
	public void strokePath() throws IOException {
		this.strokePath((GeneralPath) this.getLinePath().clone(), true);
	}
	
	void strokePath(GeneralPath path, boolean mark) throws IOException {
		if (mark) {
			this.markPath((GeneralPath) this.getLinePath().clone());			
		}
		this.linePath.reset();
	}
	
	@Override
	public void fillPath(int windingRule) throws IOException {
		this.fillPath((GeneralPath) this.getLinePath().clone(), true, windingRule);
	}
	
	void fillPath(GeneralPath path, boolean mark, int windingRule) throws IOException {
		if (mark) {
			this.markPath((GeneralPath) this.getLinePath().clone());
		}
		this.linePath.reset();
	}
	
	@Override
	public void fillAndStrokePath(int windingRule) throws IOException {
        GeneralPath path = (GeneralPath) this.getLinePath().clone();
        this.fillPath(path, false, windingRule);
        this.strokePath(path, true);
	}

    @Override
    protected void showText(byte[] string) throws IOException {
    	this.textBuffer = new StringBuilder();
    	super.showText(string);
    	
    	this.markText(this.textBuffer.toString());
    	this.textBuffer = null;
    }
    
    @Override
    protected void showFontGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                 Vector displacement) throws IOException {
        AffineTransform at = textRenderingMatrix.createAffineTransform();
        at.concatenate(font.getFontMatrix().createAffineTransform());

        Glyph2D glyph2D = createGlyph2D(font);
        this.textBuffer.append(unicode);
        drawGlyph2D(glyph2D, font, code, displacement, at);
    }
    
    protected void drawGlyph2D(Glyph2D glyph2D, PDFont font, int code, Vector displacement,
            AffineTransform at) throws IOException {
    	GeneralPath path = glyph2D.getPathForCharacterCode(code);
    	if (path != null) {
    		// stretch non-embedded glyph if it does not match the width contained in the PDF
    		if (!font.isEmbedded()) {
    			float fontWidth = font.getWidthFromFont(code);
    			if (fontWidth > 0 && // ignore spaces
    					Math.abs(fontWidth - displacement.getX() * 1000) > 0.0001) {
    				float pdfWidth = displacement.getX() * 1000;
    				at.scale(pdfWidth / fontWidth, 1);
    			}
    		}

    		// render glyph
    		Shape glyph = at.createTransformedShape(path);
    		this.markPath(glyph.getBounds());
    	}
    }
    
    private Glyph2D createGlyph2D(PDFont font) throws IOException
    {
        // Is there already a Glyph2D for the given font?
        if (fontGlyph2D.containsKey(font))
        {
            return fontGlyph2D.get(font);
        }

        Glyph2D glyph2D = null;
        if (font instanceof PDTrueTypeFont)
        {
            PDTrueTypeFont ttfFont = (PDTrueTypeFont)font;
            glyph2D = new TTFGlyph2D(ttfFont);  // TTF is never null
        }
        else if (font instanceof PDType1Font)
        {
            PDType1Font pdType1Font = (PDType1Font)font;
            glyph2D = new Type1Glyph2D(pdType1Font); // T1 is never null
        }
        else if (font instanceof PDType1CFont)
        {
            PDType1CFont type1CFont = (PDType1CFont)font;
            glyph2D = new Type1Glyph2D(type1CFont);
        }
        else if (font instanceof PDType0Font)
        {
            PDType0Font type0Font = (PDType0Font) font;
            if (type0Font.getDescendantFont() instanceof PDCIDFontType2)
            {
                glyph2D = new TTFGlyph2D(type0Font); // TTF is never null
            }
            else if (type0Font.getDescendantFont() instanceof PDCIDFontType0)
            {
                // a Type0 CIDFont contains CFF font
                PDCIDFontType0 cidType0Font = (PDCIDFontType0)type0Font.getDescendantFont();
                glyph2D = new CIDType0Glyph2D(cidType0Font); // todo: could be null (need incorporate fallback)
            }
        }
        else
        {
            throw new IllegalStateException("Bad font type: " + font.getClass().getSimpleName());
        }

        // cache the Glyph2D instance
        if (glyph2D != null)
        {
            fontGlyph2D.put(font, glyph2D);
        }

        if (glyph2D == null)
        {
            // todo: make sure this never happens
            throw new UnsupportedOperationException("No font for " + font.getName());
        }

        return glyph2D;
    }
    
	private void markImage(Shape outline) {
		if (this.runtimeMarkedContentStack.isEmpty()) {
    		return;
    	}
		PDMarkedContent content = this.runtimeMarkedContentStack.peek();
		content.addOutlineShape(outline);
	}
	
	private void markPath(Shape gpath) {
    	if (this.runtimeMarkedContentStack.isEmpty()) {
    		return;
    	}
    	PDMarkedContent content = this.runtimeMarkedContentStack.peek();
    	content.addOutlineShape(gpath);
	}
	
    private void markText(String text) {
    	if (this.runtimeMarkedContentStack.isEmpty()) {
    		return;
    	}
    	PDMarkedContent content = this.runtimeMarkedContentStack.peek();
    	content.appendContentString(text);
    }
    
    @Override
	public void beginMarkedContentSequence(COSName tag, COSDictionary properties) {
		PDMarkedContent markedContent = PDMarkedContent.create(tag, properties);
		
		if (!this.runtimeMarkedContentStack.isEmpty()) {
			PDMarkedContent parent = this.runtimeMarkedContentStack.peek();
			if (parent != null) {
				parent.addMarkedContent(markedContent);
				this.runtimeMarkedContentStack.push(markedContent);
				return;
			}			
		}
			
		this.contents.add(markedContent);
		
		if (!markedContent.isArtifact()) {
			int mcid = markedContent.getMCID();
			if (mcid >= 0) {
				this.markedContentMap.put(mcid, markedContent);
			}
		}
		
		this.runtimeMarkedContentStack.push(markedContent);
	}
	
	public void endMarkedContentSequence() {
		if (this.runtimeMarkedContentStack.isEmpty()) {
			return;
		}
		PDMarkedContent last = this.runtimeMarkedContentStack.pop();
		if (!this.xobjectStack.isEmpty()) {
			last.setXObjectRefTag(this.xobjectStack.peek());
		}
	}
	
	public List<PDMarkedContent> getMarkedContentList() {
		return this.contents;
	}

	public void beginXObject(COSObject xobject) {
		this.xobjectStack.push(xobject);
	}
	
	public void endXObject() {
		if (this.xobjectStack.isEmpty()) {
			return;
		}
		this.xobjectStack.pop();
	}

	public void beginAnnot(PDAnnotation annot) {
		int structParent = annot.getStructParent();
	}

	public void endAnnot(PDAnnotation annot) {
		int structParent = annot.getStructParent();
	}
}

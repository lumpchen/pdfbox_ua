package org.apache.pdfbox.debugger.pagepane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.pdfbox.debugger.ui.ImageUtil;
import org.apache.pdfbox.debugger.ui.tags.MarkedContentNode;
import org.apache.pdfbox.debugger.ui.tags.PDFTagsTreeModel.ArtifactNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PageViewPane implements ActionListener, AncestorListener {
    private JPanel panel;
    private int pageIndex = -1;
    private PDDocument document;
    private ImageLabel label;

    private PDPage pdPage;
    private List<MarkedContentNode> pageMarkedContentList;
    private List<TooltipArea> tooltipArea;
    
    private float zoom = 1;
    private int rotation = 0;
    
    private PDFRenderer renderer;
    private List<MarkedContentNode> selectedStructureList;
    
    class TooltipArea {
    	Area shape;
    	String tooltip;
    }
    
    public PageViewPane(PDDocument document) {
    	this.document = document;
    	this.renderer = new PDFRenderer(this.document);
    }
    
    public void setPage(PDPage pdPage, List<MarkedContentNode> pageMarkedContentList) {
    	this.pdPage = pdPage;
        this.pageIndex = this.document.getPages().indexOf(pdPage);
        this.pageMarkedContentList = pageMarkedContentList;
        this.initUI();
    }
    
    public PDPage getPage() {
    	return this.pdPage;
    }
    
    public void setPage(PDPage page) {
    	this.setPage(page, null);
    }
    
    public void zoom(int zoom) {
    	this.zoom = zoom / 100f;
    }
    
    public void rotation(int rotation) {
    	this.rotation = rotation;
    }
    
    private List<ArtifactNode> artifactList;
    public void setArtifactList(List<ArtifactNode> artifactList) {
    	this.artifactList = artifactList;
    }
    
    private void initUI() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String pageLabelText = pageIndex < 0 ? "Page number not found" : "Page " + (pageIndex + 1);
        
        JLabel pageLabel = new JLabel(pageLabelText);
        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        pageLabel.setBackground(Color.GREEN);
        panel.add(pageLabel);
        
        label = new ImageLabel();
        label.setBackground(panel.getBackground());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
    	if (this.pageMarkedContentList != null && !this.pageMarkedContentList.isEmpty()) {
    		this.tooltipArea = new ArrayList<TooltipArea>();
    		for (MarkedContentNode mc : this.pageMarkedContentList) {
    			Area outline = mc.getOutlineArea();
    			String tooltip = mc.getTooltip();
    			
    			if (outline != null && tooltip != null) {
    				TooltipArea item = new TooltipArea();
   					item.shape = outline;
   					item.tooltip = tooltip;
   					this.tooltipArea.add(item);
    			}
    		}
    	}
        
        panel.add(label);
        panel.addAncestorListener(this);
    }
    
    public void executePage() {
    	if (this.pdPage == null) {
    		return;
    	}
    	
        // render in a background thread: rendering is read-only, so this should be ok, despite
        // the fact that PDDocument is not officially thread safe
    	RenderWorker worker = new RenderWorker(this.zoom, this.rotation, this.pdPage, this.pageIndex,
    			this.selectedStructureList, this.tooltipArea);
    	worker.execute();
    }

    public Component getPanel() {
        return panel;
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent) {
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent) {
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent) {
    }

    public void addMarkedContent(PDPage page, MarkedContentNode... markedContentEntry) {
    	if (markedContentEntry == null) {
    		return;
    	}
    	
    	int pageIndex = this.document.getPages().indexOf(page);
    	if (pageIndex < 0) {
    		return;
    	}
    	this.selectedStructureList = new ArrayList<MarkedContentNode>(markedContentEntry.length);
    	for (int i = 0; i < markedContentEntry.length; i++) {
    		PDPage container = markedContentEntry[i].getParent().getPage();
    		if (container != null) {
    			int mcPageIndex = this.document.getPages().indexOf(container);
    			if (mcPageIndex != pageIndex) {
        			continue;
        		}
    		}
    		this.selectedStructureList.add(markedContentEntry[i]);
    	}
    }
    
    public void clearMarkedContent() {
    	this.selectedStructureList = null;
    	this.artifactList = null;
    }
    
    /**
     * Note that PDDocument is not officially thread safe, caution advised.
     */
    private final class RenderWorker extends SwingWorker<BufferedImage, Integer>
    {
        private final float scale;
        private final int rotation;
        private int pageIndex;
        
        private List<MarkedContentNode> selectedStructureList = null;
        private PDPage pdPage;
        private List<TooltipArea> tooltipArea;
        
        private final Color ARTIFACT_COLOR = Color.DARK_GRAY;

        private RenderWorker(float scale, int rotation, PDPage pdPage, int pageIndex) {
            this.scale = scale;
            this.rotation = rotation;
            this.pdPage = pdPage;
            this.pageIndex = pageIndex;
        }
        
        private RenderWorker(float scale, int rotation, PDPage pdPage, int pageIndex, 
        		List<MarkedContentNode> selectedStructureList, List<TooltipArea> tooltipArea) {
            this.scale = scale;
            this.rotation = rotation;
            this.pdPage = pdPage;
            this.pageIndex = pageIndex;
            this.selectedStructureList = selectedStructureList;
            this.tooltipArea = tooltipArea;
        }

        @Override
        protected BufferedImage doInBackground() throws IOException
        {
            label.setIcon(null);
            label.setText("Loading...");
            BufferedImage bim = renderer.renderImage(this.pageIndex, scale);
            return ImageUtil.getRotatedImage(bim, rotation);
        }

        @Override
        protected void done()
        {
            try
            {
            	BufferedImage pageImage = this.get();
        		Graphics2D g2 = pageImage.createGraphics();
        		this.initUserSpaceGraphics(g2);
        		
            	if (this.tooltipArea != null && !this.tooltipArea.isEmpty()) {
            		for (TooltipArea item : this.tooltipArea) {
            			Shape ts = g2.getTransform().createTransformedShape(item.shape);
            			label.addArea(ts.getBounds2D(), item.tooltip);
            		}
            	}
            	
        		if (this.selectedStructureList != null && !this.selectedStructureList.isEmpty()) {
            		for (MarkedContentNode mc : this.selectedStructureList) {
            			Area outline = mc.getOutlineArea();
            			if (outline != null) {
            				g2.draw(outline.getBounds2D());
            			}
            		}
            	}
        		
        		if (artifactList != null) {
        			g2.setPaint(ARTIFACT_COLOR);
        			for (ArtifactNode artifact : artifactList) {
        				Rectangle2D rect = artifact.outline.getBounds2D();
        				g2.draw(rect);
        				int x1 = (int) rect.getX();
        				int y1 = (int) rect.getY();
        				int x2 = x1 + (int) rect.getWidth();
        				int y2 = y1 + (int) rect.getHeight();
        				g2.drawLine(x1, y1, x2, y2);
        				
        				x1 = (int) rect.getX();
        				y2 = (int) rect.getY();
        				y1 = y2 + (int) rect.getHeight();
        				x2 = x1 + (int) rect.getWidth();
        				g2.drawLine(x1, y1, x2, y2);
        			}
        		}
            	
                label.setIcon(new ImageIcon(get()));
                label.setText(null);
            }
            catch (InterruptedException e)
            {
                label.setText(e.getMessage());
                throw new RuntimeException(e);
            }
            catch (ExecutionException e)
            {
                label.setText(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        
        private void initUserSpaceGraphics(Graphics2D g) {
        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        	
            PDRectangle cropBox = this.pdPage.getCropBox();
            int rotationAngle = this.pdPage.getRotation() + this.rotation;
            
            g.scale(this.scale, this.scale);
            
            if (rotationAngle != 0) {
                float translateX = 0;
                float translateY = 0;
                switch (rotationAngle) {
                    case 90:
                        translateX = cropBox.getHeight();
                        break;
                    case 270:
                        translateY = cropBox.getWidth();
                        break;
                    case 180:
                        translateX = cropBox.getWidth();
                        translateY = cropBox.getHeight();
                        break;
                }
                g.translate(translateX, translateY);
                g.rotate((float) Math.toRadians(rotationAngle));
            }
            
            g.translate(0, cropBox.getHeight());
            g.scale(1, -1); // Flip vertical

            // TODO use getStroke() to set the initial stroke
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g.setPaint(Color.RED);
            g.setColor(Color.RED);
            
            // adjust for non-(0,0) crop box
            g.translate(-cropBox.getLowerLeftX(), -cropBox.getLowerLeftY());
        }
    }
    
    class ImageLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		private List<Shape> outline = new ArrayList<Shape>();
		private Map<Shape, String> tooltipAreaMap = new HashMap<Shape, String>();
		
		public ImageLabel() {
    		super();
    	}
		
		public void addArea(Shape shape, String tooltip) {
			this.outline.add(shape);
			this.tooltipAreaMap.put(shape, tooltip);
		}
		
		@Override
		public boolean contains(int x, int y) {
			if (this.outline == null || this.outline.isEmpty()) {
				return false;
			}
			Point p = new Point(x, y);
			for (Shape s : this.outline) {
				if (s.contains(p)) {
					this.setToolTipText(this.tooltipAreaMap.get(s));
					return true;
				}
			}
			return false;
		}
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}

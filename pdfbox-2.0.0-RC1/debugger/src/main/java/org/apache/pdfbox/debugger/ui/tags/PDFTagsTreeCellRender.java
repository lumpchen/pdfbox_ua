package org.apache.pdfbox.debugger.ui.tags;

import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.pdfbox.debugger.ui.PDFTreeCellRenderer;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;

public class PDFTagsTreeCellRender extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private static final ImageIcon ICON_ARRAY = new ImageIcon(getImageUrl("array"));
	private static final ImageIcon ICON_PDF = new ImageIcon(getImageUrl("pdf"));

	private static final String ROOT_TAG_NAME = "Tags";
	
	private static URL getImageUrl(String name) {
		String fullName = "/org/apache/pdfbox/debugger/" + name + ".png";
		return PDFTreeCellRenderer.class.getResource(fullName);
	}
	
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object nodeValue,
            boolean isSelected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean componentHasFocus)
    {
        Component component = super.getTreeCellRendererComponent(tree, toTreeObject(nodeValue),
                isSelected, expanded, leaf, row, componentHasFocus);
        
        setIcon(lookupIconWithOverlay(nodeValue));
        return component;
    }
    
    private Object toTreeObject(Object nodeValue) {
        Object result = nodeValue;
        if (nodeValue instanceof PDStructureTreeRoot) {
        	result = ROOT_TAG_NAME;
        } else if (nodeValue instanceof StructureNode) {
        	result = ((StructureNode) nodeValue).toString();
        } else {
        	result = nodeValue.toString();
        }
        
        return result;
    }
    
    private ImageIcon lookupIconWithOverlay(Object nodeValue) {
        if (nodeValue instanceof PDStructureTreeRoot) {
        	return ICON_PDF;
        }
    	return ICON_ARRAY;
    }
}

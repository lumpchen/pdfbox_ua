/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.debugger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.pdfbox.debugger.pagepane.PageViewPane;
import org.apache.pdfbox.debugger.pagepane.ReadingWorker;
import org.apache.pdfbox.debugger.pagepane.TagsLoaderWorker;
import org.apache.pdfbox.debugger.ui.ErrorDialog;
import org.apache.pdfbox.debugger.ui.ExtensionFileFilter;
import org.apache.pdfbox.debugger.ui.FileOpenSaveDialog;
import org.apache.pdfbox.debugger.ui.OSXAdapter;
import org.apache.pdfbox.debugger.ui.RecentFiles;
import org.apache.pdfbox.debugger.ui.TagsTree;
import org.apache.pdfbox.debugger.ui.Tree;
import org.apache.pdfbox.debugger.ui.tags.MarkedContentNode;
import org.apache.pdfbox.debugger.ui.tags.PDFPageTreeModel;
import org.apache.pdfbox.debugger.ui.tags.PDFTagsTreeCellRender;
import org.apache.pdfbox.debugger.ui.tags.PDFTagsTreeModel;
import org.apache.pdfbox.debugger.ui.tags.PageIndexNode;
import org.apache.pdfbox.debugger.ui.tags.ReadingText;
import org.apache.pdfbox.debugger.ui.tags.StructureNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;

/**
 * PDF Debugger.
 * 
 * @author wurtz
 * @author Ben Litchfield
 * @author Khyrul Bashar
 */
public class PDFViewer extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static final String PASSWORD = "-password";
	private static final String VIEW_STRUCTURE = "-viewstructure";

	private static final int SHORCUT_KEY_MASK = Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask();

	private RecentFiles recentFiles;

	private PDDocument document;
	private String currentFilePath;

	private static final String OS_NAME = System.getProperty("os.name")
			.toLowerCase();
	private static final boolean IS_MAC_OS = OS_NAME.startsWith("mac os x");

	private JScrollPane jScrollPanePage;
	private JScrollPane jScrollPaneTags;
	private JScrollPane jScrollPaneView;
	private javax.swing.JSplitPane jSplitPaneMain;
	private javax.swing.JTextPane jTextPane1;
	private Tree pageTree;
	private TagsTree tagsTree;

	private final JPanel documentPanel = new JPanel();

	// file menu
	private JMenu recentFilesMenu;

	private PageViewPane pageViewPane;
	/**
	 * Constructor.
	 */
	public PDFViewer() {
		this(false);
	}

	/**
	 * Constructor.
	 */
	public PDFViewer(boolean viewPages) {
		initComponents();
		
		this.readingWorker.start();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		jSplitPaneMain = new javax.swing.JSplitPane();
		final JTabbedPane navigationsTab = new JTabbedPane();

		jScrollPanePage = new JScrollPane();
		pageTree = new Tree(this);
		pageTree.setModel(null);
		jScrollPaneView = new JScrollPane();
		jTextPane1 = new javax.swing.JTextPane();

		setTitle("PDF viewer");

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent windowEvent) {
				pageTree.requestFocusInWindow();
				super.windowOpened(windowEvent);
			}

			@Override
			public void windowClosing(WindowEvent evt) {
				exitForm(evt);
			}
		});

		jScrollPanePage.setBorder(new BevelBorder(BevelBorder.RAISED));
		jScrollPanePage.setPreferredSize(new Dimension(300, 500));
		pageTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent evt) {
				jPageTreeValueChanged(evt);
			}
		});

		jScrollPanePage.setViewportView(pageTree);
		navigationsTab.addTab("Pages", jScrollPanePage);
		
		// Tags panel
		this.jScrollPaneTags = new JScrollPane();
		navigationsTab.addTab("Tags", this.jScrollPaneTags);
		navigationsTab.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (navigationsTab.getSelectedIndex() == 1) { // Tags tab
																// selected
				}
			}
		});
		this.tagsTree = new TagsTree(this);
		this.tagsTree.setCellRenderer(new PDFTagsTreeCellRender());
		this.tagsTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent evt) {
				jTagsTreeValueChanged(evt);
			}
		});
		this.tagsTree.setModel(null);

		this.jScrollPaneTags.setViewportView(this.tagsTree);
		// Tags panel

		jSplitPaneMain.setLeftComponent(navigationsTab);
		jSplitPaneMain.setRightComponent(jScrollPaneView);
		jSplitPaneMain.setDividerSize(3);

		jScrollPaneView.setBorder(new BevelBorder(BevelBorder.RAISED));
		jScrollPaneView.setPreferredSize(new Dimension(300, 500));
		jScrollPaneView.setViewportView(jTextPane1);

		JScrollPane documentScroller = new JScrollPane();
		documentScroller.setViewportView(documentPanel);

		getContentPane().add(jSplitPaneMain, BorderLayout.CENTER);

		// create menus
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createViewMenu());
		setJMenuBar(menuBar);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 700) / 2, (screenSize.height - 600) / 2,
				700, 600);

		// drag and drop to open files
		setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(TransferSupport transferSupport) {
				return transferSupport
						.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean importData(TransferSupport transferSupport) {
				try {
					Transferable transferable = transferSupport
							.getTransferable();
					List<File> files = (List<File>) transferable
							.getTransferData(DataFlavor.javaFileListFlavor);
					readPDFFile(files.get(0), "");
					return true;
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (UnsupportedFlavorException e) {
					throw new RuntimeException(e);
				}
			}
		});

		// Mac OS X file open/quit handler
		if (IS_MAC_OS) {
			try {
				Method osxOpenFiles = getClass().getDeclaredMethod(
						"osxOpenFiles", String.class);
				osxOpenFiles.setAccessible(true);
				OSXAdapter.setFileHandler(this, osxOpenFiles);

				Method osxQuit = getClass().getDeclaredMethod("osxQuit");
				osxQuit.setAccessible(true);
				OSXAdapter.setQuitHandler(this, osxQuit);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private JMenu createFileMenu() {
		JMenuItem openMenuItem = new JMenuItem("Open...");
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORCUT_KEY_MASK));
		openMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				openMenuItemActionPerformed(evt);
			}
		});

		JMenu fileMenu = new JMenu("File");
		fileMenu.add(openMenuItem);

		JMenuItem openUrlMenuItem = new JMenuItem("Open URL...");
		openUrlMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
				SHORCUT_KEY_MASK));
		openUrlMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String urlString = JOptionPane.showInputDialog("Enter an URL");
				try {
					readPDFurl(urlString, "");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		fileMenu.add(openUrlMenuItem);

		try {
			recentFiles = new RecentFiles(this.getClass(), 5);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		recentFilesMenu = new JMenu("Open Recent");
		recentFilesMenu.setEnabled(false);
		addRecentFileItems();
		fileMenu.add(recentFilesMenu);

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				exitMenuItemActionPerformed(evt);
			}
		});

		if (!IS_MAC_OS) {
			fileMenu.addSeparator();
			fileMenu.add(exitMenuItem);
		}

		return fileMenu;
	}

	public static final String ZOOM_100_PERCENT = "100%";
	private static final int[] ZOOMS = new int[] { 25, 50, 100, 200, 400 };
    public static final String ROTATE_0_DEGREES = "0°";
    public static final String ROTATE_90_DEGREES = "90°";
    public static final String ROTATE_180_DEGREES = "180°";
    public static final String ROTATE_270_DEGREES = "270°";
    private static final int[] ROTATION = {0, 90, 180, 270};
	private int zoom = 100;
	private int rotation = 0;
	private ReadingWorker readingWorker = new ReadingWorker();
	private boolean highlightArtifact = false;
	private TagsLoaderWorker worker;
	
	private JMenu createViewMenu() {
		JMenu viewMenu = new JMenu("View");

		JMenu menu = new JMenu("Zoom");
		ButtonGroup bg = new ButtonGroup();
		for (int zoom : ZOOMS) {
			JRadioButtonMenuItem zoomItem = new JRadioButtonMenuItem(zoom + "%");
			if (zoom == 100) {
				zoomItem.setSelected(true);
			}
			this.addZoomActionListener(zoomItem);
			bg.add(zoomItem);
			menu.add(zoomItem);
		}
		viewMenu.add(menu);

		menu = new JMenu("Rotation");
		bg = new ButtonGroup();
		
		for (int rot : ROTATION) {
			JRadioButtonMenuItem rotItem = new JRadioButtonMenuItem(rot + "°");
			if (rot == 0) {
				rotItem.setSelected(true);
			}
			this.addRotActionListener(rotItem);
			bg.add(rotItem);
			menu.add(rotItem);
		}
		viewMenu.add(menu);

		viewMenu.addSeparator();
		
		menu = new JMenu("Read Out Loud");
		bg = new ButtonGroup();
		JRadioButtonMenuItem startItem = new JRadioButtonMenuItem("Read This Page");
		startItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startReadPage();
			}
		});
		bg.add(startItem);
		menu.add(startItem);
		
		startItem = new JRadioButtonMenuItem("Read Highlight Content");
		startItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startReadStructure();
			}
		});
		bg.add(startItem);
		menu.add(startItem);
		
		JRadioButtonMenuItem stopItem = new JRadioButtonMenuItem("Stop");
		stopItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stopReading();
			}
		});
		bg.add(stopItem);
		menu.add(stopItem);
		viewMenu.add(menu);
		
		viewMenu.addSeparator();
		
		JCheckBoxMenuItem aftifactItem = new JCheckBoxMenuItem("Highlight Artifact");
		aftifactItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean flag = ((JCheckBoxMenuItem) e.getSource()).isSelected();
				highlightArtifact(flag);	
			}
		});
		viewMenu.add(aftifactItem);
		
		return viewMenu;
	}
	
	private void startReadPage() {
		try {
			List<ReadingText> textList = this.getPageReadingText();
			this.readText(textList);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void startReadStructure() {
		List<ReadingText> textList = this.getHighlightText();
		this.readText(textList);
	}
	
	private void readText(List<ReadingText> textList) {
		this.readingWorker.stopReading();
		
		textList = ReadingText.mergeTextByLang(textList);
		if (textList == null) {
			return;
		}
		for (ReadingText text : textList) {
			this.readingWorker.add(text);
		}
	}
	
	private List<ReadingText> getHighlightText() {
		TreePath path = this.tagsTree.getSelectionPath();
		if (path != null) {
			try {
				Object selectedNode = path.getLastPathComponent();
				List<ReadingText> textList = new ArrayList<ReadingText>();
				if (selectedNode instanceof MarkedContentNode) {
					MarkedContentNode mcNode = (MarkedContentNode) selectedNode;
					textList.add(new ReadingText(mcNode.getContentString(), mcNode.getLang()));
				} else if (selectedNode instanceof StructureNode) {
					StructureNode selStructure = (StructureNode) selectedNode;
					return selStructure.getReadingText();
				}
				return textList;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private List<ReadingText> getPageReadingText() {
		if (this.pageViewPane == null || this.pageViewPane.getPage() == null) {
			return null;
		}
		
		try {
			PDPage page = this.pageViewPane.getPage();
			PDFTagsTreeModel treeModel = (PDFTagsTreeModel) this.tagsTree.getModel();
			return treeModel.getReadingText(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void stopReading() {
		try {
			if (this.readingWorker != null) {
				this.readingWorker.stopReading();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addZoomActionListener(JRadioButtonMenuItem zoomItem) {
		zoomItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				cmd = cmd.substring(0, cmd.indexOf("%"));
				zoom = Integer.parseInt(cmd);
				pageViewPane.zoom(zoom);
				pageViewPane.executePage();
				replaceRightComponent(new JScrollPane(pageViewPane.getPanel()));
			}
		});
	}
	
	private void addRotActionListener(JRadioButtonMenuItem rotItem) {
		rotItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				cmd = cmd.substring(0, cmd.indexOf("°"));
				rotation = Integer.parseInt(cmd);
				pageViewPane.rotation(rotation);
				pageViewPane.executePage();
				replaceRightComponent(new JScrollPane(pageViewPane.getPanel()));
			}
		});
	}
	
	private void highlightArtifact(boolean selected) {
		this.highlightArtifact = selected;
	}
	

	/**
	 * This method is called via reflection on Mac OS X.
	 */
	private void osxOpenFiles(String filename) {
		try {
			readPDFFile(filename, "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method is called via reflection on Mac OS X.
	 */
	private void osxQuit() {
		exitMenuItemActionPerformed(null);
	}

	private void openMenuItemActionPerformed(ActionEvent evt) {
		try {
			if (IS_MAC_OS) {
				FileDialog openDialog = new FileDialog(this, "Open");
				openDialog.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(File file, String s) {
						return file.getName().toLowerCase().endsWith(".pdf");
					}
				});
				openDialog.setVisible(true);
				if (openDialog.getFile() != null) {
					readPDFFile(openDialog.getFile(), "");
				}
			} else {
				String[] extensions = new String[] { "pdf", "PDF" };
				FileFilter pdfFilter = new ExtensionFileFilter(extensions,
						"PDF Files (*.pdf)");
				FileOpenSaveDialog openDialog = new FileOpenSaveDialog(this,
						pdfFilter);

				File file = openDialog.openFile();
				if (file != null) {
					readPDFFile(file, "");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void jPageTreeValueChanged(TreeSelectionEvent evt) {
		TreePath path = pageTree.getSelectionPath();
		if (path != null) {
			try {
				PageIndexNode selectedNode = (PageIndexNode) path.getLastPathComponent();
				if (selectedNode.isRoot()) {
					return;
				}
				
				PDFPageTreeModel treeModel = (PDFPageTreeModel) this.pageTree.getModel();
				PDPage page = treeModel.getPDPage(selectedNode);
				if (page == null) {
					return;
				}
				this.pageViewPane.setPage(page);
				this.pageViewPane.zoom(this.zoom);
				this.pageViewPane.rotation(this.rotation);
				this.pageViewPane.clearMarkedContent();
				this.pageViewPane.executePage();
				replaceRightComponent(new JScrollPane(this.pageViewPane.getPanel()));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private void jTagsTreeValueChanged(TreeSelectionEvent evt) {
		if (this.worker != null && !this.worker.isDone()) {
			return;
		}
		TreePath path = this.tagsTree.getSelectionPath();
		if (path != null) {
			try {
				Object selectedNode = path.getLastPathComponent();

				if (selectedNode instanceof MarkedContentNode) {
					PDPage page = ((MarkedContentNode) selectedNode).getPage();
					if (page == null) {
						return;
					}
					PDFTagsTreeModel treeModel = (PDFTagsTreeModel) this.tagsTree.getModel();
					this.pageViewPane.setPage(page, treeModel.getPageMarkedContents(page));
					if (this.highlightArtifact) {
						this.pageViewPane.setArtifactList(treeModel.getArtifacts(page));
					} else {
						this.pageViewPane.setArtifactList(null);
					}
					this.pageViewPane.zoom(this.zoom);
					this.pageViewPane.rotation(this.rotation);
					this.pageViewPane.addMarkedContent(page, (MarkedContentNode) selectedNode);
				} else if (selectedNode instanceof StructureNode) {
					StructureNode selStructure = (StructureNode) selectedNode;
					PDPage page = selStructure.getPage();
					List<MarkedContentNode> entries = selStructure.getAllMarkedContentNode();
					if (page == null) {
						if (!entries.isEmpty()) {
							MarkedContentNode firstChild = entries.get(0);
							page = firstChild.getPage();
						}
					}
					if (page == null) {
						return;
					}
					
					MarkedContentNode[] children = entries.toArray(new MarkedContentNode[entries.size()]);
					PDFTagsTreeModel treeModel = (PDFTagsTreeModel) this.tagsTree.getModel();
					this.pageViewPane.setPage(page, treeModel.getPageMarkedContents(page));
					if (this.highlightArtifact) {
						this.pageViewPane.setArtifactList(treeModel.getArtifacts(page));
					} else {
						this.pageViewPane.setArtifactList(null);
					}
					this.pageViewPane.zoom(this.zoom);
					this.pageViewPane.rotation(this.rotation);
					this.pageViewPane.addMarkedContent(page, children);
				} else if (selectedNode instanceof PDStructureTreeRoot) {
					if (this.pageViewPane != null) {
						this.pageViewPane.clearMarkedContent();
					} else {
						return;
					}
				}
				this.pageViewPane.executePage();
				replaceRightComponent(new JScrollPane(this.pageViewPane.getPanel()));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	// replace the right component while keeping divider position
	private void replaceRightComponent(Component pane) {
		int div = jSplitPaneMain.getDividerLocation();
		jSplitPaneMain.setRightComponent(pane);
		jSplitPaneMain.setDividerLocation(div);
	}

	private void exitMenuItemActionPerformed(ActionEvent evt) {
		if (document != null) {
			try {
				document.close();
				if (!currentFilePath.startsWith("http")) {
					recentFiles.addFile(currentFilePath);
				}
				recentFiles.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		System.exit(0);
	}

	/**
	 * Exit the Application.
	 */
	private void exitForm(WindowEvent evt) {
		if (document != null) {
			try {
				document.close();
				if (!currentFilePath.startsWith("http")) {
					recentFiles.addFile(currentFilePath);
				}
				recentFiles.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		System.exit(0);
	}

	/**
	 * Entry point.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// handle uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				new ErrorDialog(throwable).setVisible(true);
			}
		});

		// open file, if any
		String filename = null;
		String password = "";
		boolean viewPages = true;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(PASSWORD)) {
				i++;
				if (i >= args.length) {
					usage();
				}
				password = args[i];
			} else if (args[i].equals(VIEW_STRUCTURE)) {
				viewPages = false;
			} else {
				filename = args[i];
			}
		}
		final PDFViewer viewer = new PDFViewer(viewPages);

		if (filename != null) {
			File file = new File(filename);
			if (file.exists()) {
				viewer.readPDFFile(filename, password);
			}
		}
		viewer.setVisible(true);
	}

	private void readPDFFile(String filePath, String password)
			throws IOException {
		File file = new File(filePath);
		readPDFFile(file, password);
	}

	private void readPDFFile(File file, String password) throws IOException {
		if (document != null) {
			document.close();
			if (!currentFilePath.startsWith("http")) {
				recentFiles.addFile(currentFilePath);
			}
		}
		currentFilePath = file.getPath();
		recentFiles.removeFile(file.getPath());
		parseDocument(file, password);

		initTree();

		if (IS_MAC_OS) {
			setTitle(file.getName());
			getRootPane().putClientProperty("Window.documentFile", file);
		} else {
			setTitle("PDF Debugger - " + file.getAbsolutePath());
		}
		addRecentFileItems();
	}

	private void readPDFurl(String urlString, String password)
			throws IOException {
		if (document != null) {
			document.close();
			if (!currentFilePath.startsWith("http")) {
				recentFiles.addFile(currentFilePath);
			}
		}
		currentFilePath = urlString;
		URL url = new URL(urlString);
		document = PDDocument.load(url.openStream(), password);

		initTree();

		if (IS_MAC_OS) {
			setTitle(urlString);
		} else {
			setTitle("PDF Debugger - " + urlString);
		}
		addRecentFileItems();
	}

	private void initTree() {
		if (this.document.isTaggedPDF()) {
			this.initTagsTree();
		} else {
			this.tagsTree.setModel(new TagsLoaderWorker.TagsLoadingTreeModel("No tags"));
			this.tagsTree.repaint();
		}
		
		this.pageViewPane = new PageViewPane(this.document);		
		this.pageTree.setModel(new PDFPageTreeModel(this.document));
		this.pageTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.pageTree.setSelectionPath(this.pageTree.getPathForRow(1));
	}

	private void initTagsTree() {
		PDStructureTreeRoot structureRoot = this.document.getDocumentCatalog().getStructureTreeRoot();
		if (structureRoot == null) {
			return;
		}
		
		this.tagsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		this.worker = new TagsLoaderWorker(this.document, this.tagsTree);
		this.worker.execute();
	}

	/**
	 * This will parse a document.
	 * 
	 * @param file
	 *            The file addressing the document.
	 * 
	 * @throws IOException
	 *             If there is an error parsing the document.
	 */
	private void parseDocument(File file, String password) throws IOException {
		document = PDDocument.load(file, password);
	}

	private void addRecentFileItems() {
		Action recentMenuAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String filePath = (String) ((JComponent) actionEvent
						.getSource()).getClientProperty("path");
				try {
					readPDFFile(filePath, "");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		if (!recentFiles.isEmpty()) {
			recentFilesMenu.removeAll();
			List<String> files = recentFiles.getFiles();
			for (int i = files.size() - 1; i >= 0; i--) {
				String path = files.get(i);
				String name = new File(path).getName();
				JMenuItem recentFileMenuItem = new JMenuItem(name);
				recentFileMenuItem.putClientProperty("path", path);
				recentFileMenuItem.addActionListener(recentMenuAction);
				recentFilesMenu.add(recentFileMenuItem);
			}
			recentFilesMenu.setEnabled(true);
		}
	}

	/**
	 * This will print out a message telling how to use this utility.
	 */
	private static void usage() {
		String message = "Usage: java -jar pdfbox-app-x.y.z.jar PDFViewer\n"
				+ "\nOptions:\n"
				+ "  -password <password> : Password to decrypt the document\n"
				+ "  <inputfile>          : The PDF document to be loaded\n";

		System.err.println(message);
		System.exit(1);
	}
}

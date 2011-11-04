package edu.umd.isr.seil.brian;

import edu.umd.isr.seil.brian.treesearch.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.Paint;
import javax.swing.Box;
import javax.swing.BoxLayout;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.util.Animator;

import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.Dimension;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import javax.swing.JList;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class TreeSearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable funcTable;
	private CustomizedTableModel funcTableModel;
	private CustomizedColumnModel columnModel;
	private ParamsDB paramsDB = new ParamsDB();
	private VisualizationViewer<String,String> graphViewer;
	private VisualizationViewer<String,String> treeViewer;
	private CrossoverScalingControl graphScaler = new CrossoverScalingControl();
	private CrossoverScalingControl treeScaler = new CrossoverScalingControl();
	private File curDir = new File(".");
	private DefaultListModel finishedListModel;
	private DefaultListModel restListModel;
	private JTextField twResult;
	private JList restNodeList;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		/* try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException e1) {
			System.err.println("Fail to initiate the Windows Look and Feel.");
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			System.err.println("Fail to initiate the Windows Look and Feel.");
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			System.err.println("Fail to initiate the Windows Look and Feel.");
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			System.err.println("Fail to initiate the Windows Look and Feel.");
			e1.printStackTrace();
		} */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TreeSearchFrame frame = new TreeSearchFrame();
					frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
					frame.setVisible(true);
					frame.setLocationRelativeTo(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TreeSearchFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 477, 468);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JSplitPane overallPane = new JSplitPane();
		overallPane.setOneTouchExpandable(true);
		overallPane.setContinuousLayout(true);
		contentPane.add(overallPane, BorderLayout.CENTER);
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>(){
			public Paint transform(String node){
				return Color.green;
			}
		};
		DefaultModalGraphMouse<String,String> gm = new DefaultModalGraphMouse<String,String>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		
		DefaultModalGraphMouse<String,String> gmTree = new DefaultModalGraphMouse<String,String>();
		gmTree.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		
		JSplitPane leftSplitPane = new JSplitPane();
		leftSplitPane.setOneTouchExpandable(true);
		leftSplitPane.setContinuousLayout(true);
		leftSplitPane.setResizeWeight(0.5);
		leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		overallPane.setLeftComponent(leftSplitPane);
		
		JPanel userInputPanel = new JPanel();
		leftSplitPane.setLeftComponent(userInputPanel);
		userInputPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel editPanel = new JPanel();
		editPanel.setBorder(new TitledBorder(null, "Table Edit Panel", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		userInputPanel.add(editPanel, BorderLayout.SOUTH);
		editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.X_AXIS));
		
		JButton btnOpen = new JButton("Open");
		btnOpen.setToolTipText("Load data from a file");
		editPanel.add(btnOpen);
		
		JButton btnSave = new JButton("Save");
		btnSave.setToolTipText("Save data to a file");
		editPanel.add(btnSave);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		editPanel.add(horizontalGlue);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setToolTipText("Add a new function");
		editPanel.add(btnAdd);
		
		JButton btnDel = new JButton("Delete");
		btnDel.setToolTipText("Delete selected functions");
		editPanel.add(btnDel);
		
		JButton btnExe = new JButton("Parse");
		btnExe.setToolTipText("Begin to analyze");
		editPanel.add(btnExe);
		
		JScrollPane functionPane = new JScrollPane();
		userInputPanel.add(functionPane, BorderLayout.CENTER);
		
		/*********************************************************
		 * Function definition table
		 *********************************************************/
		funcTable = new JTable();
		funcTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		funcTable.setToolTipText("Doubld click to edit. Press \"Enter\" to save changes.");
		funcTableModel = new CustomizedTableModel();
		columnModel = new CustomizedColumnModel();
		
		funcTable.setModel(funcTableModel);
		funcTable.setColumnModel(columnModel);
		funcTableModel.addRow(new String[]{"1", null, null});
		funcTable.getColumnModel().getColumn(0).setMinWidth(30);
		funcTable.getColumnModel().getColumn(0).setMaxWidth(30);
		funcTable.getColumnModel().getColumn(1).setMinWidth(75);
		funcTable.getColumnModel().getColumn(1).setMaxWidth(100);
		funcTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		funcTable.getColumnModel().getColumn(2).setMinWidth(75);
		funcTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		functionPane.setViewportView(funcTable);
		
		JLabel lblFuncTabInfo = new JLabel("<html>Please enter the name and parameters for each function. Parameters are <FONT COLOR=BLUE><B>case sensitive</B></FONT> and must be separated by <FONT COLOR=BLUE><B>commas</B></FONT>.</html>");
		userInputPanel.add(lblFuncTabInfo, BorderLayout.NORTH);
		
		JPanel algorithmPanel = new JPanel();
		leftSplitPane.setRightComponent(algorithmPanel);
		algorithmPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel algCtrlPanel = new JPanel();
		algCtrlPanel.setBorder(new TitledBorder(null, "Algorithm Control Panel", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		algorithmPanel.add(algCtrlPanel, BorderLayout.SOUTH);
		algCtrlPanel.setLayout(new BoxLayout(algCtrlPanel, BoxLayout.X_AXIS));
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		algCtrlPanel.add(horizontalGlue_3);
		
		JLabel lblTWInfo = new JLabel("Tree Width: ");
		algCtrlPanel.add(lblTWInfo);
		
		twResult = new JTextField();
		twResult.setEditable(false);
		algCtrlPanel.add(twResult);
		twResult.setColumns(10);
		
		Component horizontalStrut_8 = Box.createHorizontalStrut(20);
		algCtrlPanel.add(horizontalStrut_8);
		
		JButton btnRollBack = new JButton("Roll Back");
		btnRollBack.setToolTipText("Roll back to the last processed parameter");
		algCtrlPanel.add(btnRollBack);
		
		Component horizontalStrut_7 = Box.createHorizontalStrut(20);
		algCtrlPanel.add(horizontalStrut_7);
		
		JButton btnContinue = new JButton("Continue");
		btnContinue.setToolTipText("Continue to process the selected parameter");
		algCtrlPanel.add(btnContinue);
		
		JPanel listPanel = new JPanel();
		algorithmPanel.add(listPanel, BorderLayout.CENTER);
		
		JList finishedNodeList = new JList();
		finishedListModel = new DefaultListModel();
		listPanel.setLayout(new GridLayout(0, 2, 0, 0));
		finishedNodeList.setModel(finishedListModel);
		finishedNodeList.setToolTipText("Parameters that have been processed");
		finishedNodeList.setVisibleRowCount(15);
		finishedNodeList.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Process Finished", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 70, 213)));
		listPanel.add(new JScrollPane(finishedNodeList));
		
		restNodeList = new JList();
		restListModel = new DefaultListModel();
		restNodeList.setToolTipText("Parameters to be processed");
		restNodeList.setModel(restListModel);
		restNodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		restNodeList.setVisibleRowCount(15);
		restNodeList.setBorder(new TitledBorder(null, "To be Processed", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		listPanel.add(new JScrollPane(restNodeList));
		
		JTabbedPane rightPane = new JTabbedPane(JTabbedPane.TOP);
		overallPane.setRightComponent(rightPane);
		
		// Customize the graph paint panel
		graphViewer = new VisualizationViewer<String,String>(new FRLayout<String,String>(paramsDB.getGraph()));
		graphViewer.setToolTipText("Use mouse scroll or drag to zoom in/out or move the whole graph.");
		graphViewer.setGraphMouse(gm);
		graphViewer.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		graphViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		graphViewer.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);
		
		treeViewer = new VisualizationViewer<String,String>(new TreeLayout<String,String>(paramsDB.getTree()));
		treeViewer.setToolTipText("Use mouse scroll or drag to zoom in/out or move the whole tree.");
		treeViewer.setGraphMouse(gmTree);
		treeViewer.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		treeViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		treeViewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<String, String>());
		treeViewer.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.LIGHT_GRAY));
		treeViewer.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);
		
		GraphZoomScrollPane graphShowPane = new GraphZoomScrollPane(graphViewer);
		GraphZoomScrollPane treeShowPane = new GraphZoomScrollPane(treeViewer);
		JPanel graphPane = new JPanel();
		rightPane.addTab("Remaining Graph", null, graphPane, null);
		JPanel treePane = new JPanel();
		rightPane.addTab("Resulted Tree", null, treePane, null);
		graphPane.setLayout(new BorderLayout(0, 0));
		graphPane.add(graphShowPane, BorderLayout.CENTER);
		treePane.setLayout(new BorderLayout(0, 0));
		treePane.add(treeShowPane, BorderLayout.CENTER);
		
		JPanel graphCtrlPanel = new JPanel();
		graphCtrlPanel.setBorder(new TitledBorder(null, "Graph Control Panel", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		graphPane.add(graphCtrlPanel, BorderLayout.SOUTH);
		graphCtrlPanel.setLayout(new BoxLayout(graphCtrlPanel,BoxLayout.X_AXIS));
		
		JPanel treeCtrlPanel = new JPanel();
		treeCtrlPanel.setBorder(new TitledBorder(null, "Tree Control Panel", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		treePane.add(treeCtrlPanel, BorderLayout.SOUTH);
		treeCtrlPanel.setLayout(new BoxLayout(treeCtrlPanel,BoxLayout.X_AXIS));
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		graphCtrlPanel.add(horizontalGlue_1);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		treeCtrlPanel.add(horizontalGlue_2);
		
		JComboBox lineShapeOption = new JComboBox();
		lineShapeOption.setToolTipText("Select the shape of edges");
		lineShapeOption.setModel(new DefaultComboBoxModel(new String[] {"QuadCurve", "CubicCurve", "Line"}));
		lineShapeOption.setSelectedIndex(0);
		lineShapeOption.setMaximumSize(new Dimension(50, 32767));
		graphCtrlPanel.add(lineShapeOption);
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		graphCtrlPanel.add(horizontalStrut_3);
		
		JComboBox layoutOption = new JComboBox();
		layoutOption.setToolTipText("Select the layout to place vertices");
		layoutOption.setMaximumSize(new Dimension(100, 32767));
		layoutOption.setModel(new DefaultComboBoxModel(new String[] {"FRLayout", "KKLayout", "SpringLayout", "ISOMLayout", "CircleLayout"}));
		layoutOption.setSelectedIndex(0);
		graphCtrlPanel.add(layoutOption);
		
		JComboBox treeLayoutOption = new JComboBox();
		treeLayoutOption.setToolTipText("Select the layout to place vertices");
		treeLayoutOption.setMaximumSize(new Dimension(100, 32767));
		treeLayoutOption.setModel(new DefaultComboBoxModel(new String[] {"TreeLayout","BalloonLayout","RadialTreeLayout"}));
		treeCtrlPanel.add(treeLayoutOption);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		graphCtrlPanel.add(horizontalStrut_2);
		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		treeCtrlPanel.add(horizontalStrut_4);
		
		JComboBox opMode = new JComboBox();
		opMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox evSrc = (JComboBox)e.getSource();
				String modeStr = (String)evSrc.getSelectedItem();
				@SuppressWarnings("unchecked")
				DefaultModalGraphMouse<String,String> gm = (DefaultModalGraphMouse<String,String>)graphViewer.getGraphMouse();
				if(modeStr.equalsIgnoreCase("Drag Graph")){
					gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
				}else if(modeStr.equalsIgnoreCase("Pick Node")){
					gm.setMode(ModalGraphMouse.Mode.PICKING);
				}
			}
		});
		opMode.setMaximumSize(new Dimension(50, 1800));
		opMode.setToolTipText("Select to use the mouse to move the whole graph, or pick on node");
		opMode.setModel(new DefaultComboBoxModel(new String[] {"Drag Graph", "Pick Node"}));
		opMode.setSelectedIndex(0);
		graphCtrlPanel.add(opMode);
		
		JComboBox opTreeMode = new JComboBox();
		opTreeMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox evSrc = (JComboBox)e.getSource();
				String modeStr = (String)evSrc.getSelectedItem();
				@SuppressWarnings("unchecked")
				DefaultModalGraphMouse<String,String> gm = (DefaultModalGraphMouse<String,String>)treeViewer.getGraphMouse();
				if(modeStr.equalsIgnoreCase("Drag Graph")){
					gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
				}else if(modeStr.equalsIgnoreCase("Pick Node")){
					gm.setMode(ModalGraphMouse.Mode.PICKING);
				}
			}
		});
		opTreeMode.setMaximumSize(new Dimension(50, 1800));
		opTreeMode.setToolTipText("Select to use the mouse to move the whole graph, or pick on node");
		opTreeMode.setModel(new DefaultComboBoxModel(new String[] {"Drag Graph", "Pick Node"}));
		opTreeMode.setSelectedIndex(0);
		treeCtrlPanel.add(opTreeMode);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		graphCtrlPanel.add(horizontalStrut);
		Component horizontalStrut_5 = Box.createHorizontalStrut(20);
		treeCtrlPanel.add(horizontalStrut_5);
		
		JButton btnZoomOut = new JButton("-");
		btnZoomOut.setToolTipText("Zoom Out");
		btnZoomOut.setMargin(new Insets(2, 4, 2, 4));
		graphCtrlPanel.add(btnZoomOut);
		
		JButton btnZoomOutTree = new JButton("-");
		btnZoomOutTree.setToolTipText("Zoom Out");
		btnZoomOutTree.setMargin(new Insets(2, 4, 2, 4));
		treeCtrlPanel.add(btnZoomOutTree);
		
		JButton btnZoomIn = new JButton("+");
		btnZoomIn.setToolTipText("Zoom In");
		btnZoomIn.setMargin(new Insets(2, 2, 2, 2));
		graphCtrlPanel.add(btnZoomIn);
		
		JButton btnZoomInTree = new JButton("+");
		btnZoomInTree.setToolTipText("Zoom In");
		btnZoomInTree.setMargin(new Insets(2, 2, 2, 2));
		treeCtrlPanel.add(btnZoomInTree);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		graphCtrlPanel.add(horizontalStrut_1);
		Component horizontalStrut_6 = Box.createHorizontalStrut(20);
		treeCtrlPanel.add(horizontalStrut_6);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				graphViewer.repaint();
			}
		});
		btnRefresh.setToolTipText("Repaint the graph");
		graphCtrlPanel.add(btnRefresh);
		
		JButton btnRefreshTree = new JButton("Refresh");
		btnRefreshTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				treeViewer.repaint();
			}
		});
		btnRefreshTree.setToolTipText("Repaint the tree");
		treeCtrlPanel.add(btnRefreshTree);
		
		// Event Listener for "Zoom In" button
		btnZoomIn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				graphScaler.scale(graphViewer, 1.1f, graphViewer.getCenter());
			}
		});
		
		// Event Listener for "Zoom Out" button
		btnZoomOut.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				graphScaler.scale(graphViewer, 1/1.1f, graphViewer.getCenter());
			}
		});
		
		// Event Listener for "Zoom In" button in Tree Viewer Panel
		btnZoomInTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				treeScaler.scale(treeViewer, 1.1f, treeViewer.getCenter());
			}
		});
		
		// Event Listener for "Zoom Out" button in Tree Viewer Panel
		btnZoomOutTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				treeScaler.scale(treeViewer, 1/1.1f, treeViewer.getCenter());
			}
		});
		
		// Event Listener for "Layout" list
		layoutOption.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox evSrc = (JComboBox)e.getSource();
				String modeStr = (String)evSrc.getSelectedItem();
				if(modeStr.equalsIgnoreCase("FRLayout")){
					graphViewer.setGraphLayout(new FRLayout<String,String>(paramsDB.getGraph()));
				}else if(modeStr.equalsIgnoreCase("KKLayout")){
					graphViewer.setGraphLayout(new KKLayout<String,String>(paramsDB.getGraph()));
				}else if(modeStr.equalsIgnoreCase("SpringLayout")){
					graphViewer.setGraphLayout(new SpringLayout<String,String>(paramsDB.getGraph()));
				}else if(modeStr.equalsIgnoreCase("ISOMLayout")){
					graphViewer.setGraphLayout(new ISOMLayout<String,String>(paramsDB.getGraph()));
				}else if(modeStr.equalsIgnoreCase("CircleLayout")){
					graphViewer.setGraphLayout(new CircleLayout<String,String>(paramsDB.getGraph()));
				}
			}
		});
		
		// Event Listener for "Layout" list in Tree Viewer Panel
		treeLayoutOption.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    LayoutTransition<String, String> lt;
			    DelegateTree<String,String> tree = paramsDB.getTree();
				JComboBox evSrc = (JComboBox)e.getSource();
				String modeStr = (String)evSrc.getSelectedItem();
				if(modeStr.equalsIgnoreCase("TreeLayout")){
					lt = new LayoutTransition<String, String>(treeViewer, treeViewer.getGraphLayout(), new TreeLayout<String,String>(tree));
				}else if(modeStr.equalsIgnoreCase("BalloonLayout")){
				    lt = new LayoutTransition<String, String>(treeViewer, treeViewer.getGraphLayout(), new BalloonLayout<String,String>(tree));
				}else{
				    lt = new LayoutTransition<String, String>(treeViewer, treeViewer.getGraphLayout(), new RadialTreeLayout<String,String>(tree));
				}
				Animator animator = new Animator(lt);
				animator.start();
				treeViewer.getRenderContext().getMultiLayerTransformer().setToIdentity();
			}
		});
		
		// Event Listener for "Line Shape" list
		lineShapeOption.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox evSrc = (JComboBox)e.getSource();
				String modeStr = (String)evSrc.getSelectedItem();
				if(modeStr.equalsIgnoreCase("CubicCurve")){
					graphViewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.CubicCurve<String, String>());
				}else if(modeStr.equalsIgnoreCase("Line")){
					graphViewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<String, String>());
				}else if(modeStr.equalsIgnoreCase("QuadCurve")){
					graphViewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<String, String>());
				}
				graphViewer.repaint();
			}
		});
		
		// Event listener for "Add" button
		btnAdd.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Auto-save table
				if(funcTable.isEditing()){
					funcTable.getCellEditor().stopCellEditing();
				}
				
				funcTableModel.addRow(new String[]{Integer.toString(1 + funcTableModel.getRowCount()), null, null});
			}
		});
		
		// Event listener for "Delete" button
		btnDel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Auto-save table
				if(funcTable.isEditing()){
					funcTable.getCellEditor().stopCellEditing();
				}
				
				int[] index = funcTable.getSelectedRows();
				for(int i = index.length - 1; i >= 0; i--){
					funcTableModel.removeRow(index[i]);
				};
				funcTable.clearSelection();
				for(int i = index[0]; i < funcTableModel.getRowCount(); i++){
					funcTableModel.setValueAt(i, i, 0);
				}
			}
		});
		
		/* 
		 * Open and save data
		 */
		
		// File filter
		class DataFileFilter extends FileFilter{
			public boolean accept(File f) {
				if(f.isDirectory()){
					return true;
				}
				
				String extension = f.getName().substring(f.getName().lastIndexOf('.')+1);
				if((extension != null) && (extension.equals("fdf"))){
					return true;
				}
				return false;
			}

			public String getDescription() {
				return "Function Defintion File (*.fdf)";
			}
		}
		
		// Event Listener for "Open" button
		btnOpen.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Auto-save table
				if(funcTable.isEditing()){
					funcTable.getCellEditor().stopCellEditing();
				}
				
				JFileChooser fc = new JFileChooser(curDir);
				fc.setFileFilter(new DataFileFilter());
				fc.setMultiSelectionEnabled(false);
				int choice = fc.showOpenDialog(TreeSearchFrame.this);
				if(choice == JFileChooser.APPROVE_OPTION){
					curDir = fc.getSelectedFile();
					try {
						XMLDecoder input = new XMLDecoder(new BufferedInputStream(new FileInputStream(curDir)));
						funcTableModel = (CustomizedTableModel)input.readObject(); 
						columnModel = (CustomizedColumnModel)input.readObject();
						funcTable.setModel(funcTableModel);
						funcTable.setColumnModel(columnModel);
						input.close();
					} catch (IOException ioE) {
						JOptionPane.showMessageDialog(TreeSearchFrame.this, "Fail to open the file: "+ curDir.getAbsolutePath() + ". Please make sure you have enough permissions.", "File Open Failed",JOptionPane.ERROR_MESSAGE);
						ioE.printStackTrace();
					} 
				}
			}
		});
		
		// Event Listener for "Save" button
		btnSave.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Auto-save table
				if(funcTable.isEditing()){
					funcTable.getCellEditor().stopCellEditing();
				}
				
				JFileChooser fc = new JFileChooser(curDir);
				fc.setFileFilter(new DataFileFilter());
				fc.setMultiSelectionEnabled(false);
				int choice = fc.showSaveDialog(TreeSearchFrame.this);
				if(choice == JFileChooser.APPROVE_OPTION){
					String name = fc.getSelectedFile().getAbsolutePath();
					if(!name.endsWith(".fdf")){
						name += ".fdf";
					}
					curDir = new File(name);
					try {
						curDir = new File(name);
						if(!curDir.exists()){
							curDir.createNewFile();
						}
						XMLEncoder input = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(curDir)));
						input.writeObject(funcTableModel);
						input.writeObject(columnModel);
						input.flush();
						input.close();
					} catch (IOException ioE) {
						JOptionPane.showMessageDialog(TreeSearchFrame.this, "Fail to save the data to " + name + ". Please make sure you have enough permissions.", "Data Save Failed",JOptionPane.ERROR_MESSAGE);
						ioE.printStackTrace();
					} 
				}
			}
		});
		
		// Event Listener for "Execute" button
		btnExe.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Auto-save table
				if(funcTable.isEditing()){
					funcTable.getCellEditor().stopCellEditing();
				}
				
				boolean success = paramsDB.parse(funcTableModel,TreeSearchFrame.this); // Parse the user input
				if(success){
					refreshViewers();
				}
			}
		});
		
		// Event Listener for "Continue" button
		btnContinue.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {	
				if(restListModel.isEmpty()){
					JOptionPane.showMessageDialog(TreeSearchFrame.this, "No parameter needs to be processed", "No Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// Begin to process this parameter
				paramsDB.nextStep((String)restNodeList.getSelectedValue());
				refreshViewers();
			}
		});
		
		// Event Listener for "Roll Back" button
		btnRollBack.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {			
				if(finishedListModel.isEmpty()){
					JOptionPane.showMessageDialog(TreeSearchFrame.this, "No processed parameter", "No Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// Begin to roll back this parameter
				paramsDB.rollback();
				refreshViewers();
			}
		});
	}

  private void refreshViewers() {
    // Update the list of unprocessed and processed parameters
    restListModel.removeAllElements();
    finishedListModel.removeAllElements();
    for(String item:paramsDB.getRestParams()){
        restListModel.addElement(item);
    }
    for(String item:paramsDB.getFinishedParams()){
        finishedListModel.addElement(item);
    }
    
    if(!restListModel.isEmpty()){
        restNodeList.setSelectedIndex(0);
    }
    
    // Update the tree width
    twResult.setText(paramsDB.getTreeWidth());
    
    // Update the graph viewer
    graphViewer.getGraphLayout().setGraph(paramsDB.getGraph());
    graphViewer.repaint();
    
    // Update the tree viewer
    treeViewer.getGraphLayout().setGraph(paramsDB.getTree());
    treeViewer.repaint();
  }
}



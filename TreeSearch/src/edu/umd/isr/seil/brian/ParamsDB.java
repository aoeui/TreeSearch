package edu.umd.isr.seil.brian;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import edu.umd.isr.seil.brian.treesearch.Configuration;
import edu.umd.isr.seil.brian.treesearch.ConfigurationManager;

public class ParamsDB {
	private HashSet<String> paramsList;
	private TreeMap<String,TreeSet<String>> functions;
	private UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<String, String>();;
	private DelegateTree<String,String> tree = new DelegateTree<String,String>();
	private Configuration.ConfigurationBuilder builder;
	private ConfigurationManager manager;
	// private TreeSearch<ConfigurationSearch> searcher;
	
	// Parse the input functions and their parameters
	public boolean parse(DefaultTableModel dtm, JFrame frame){
		paramsList = new HashSet<String>();
		functions = new TreeMap<String,TreeSet<String>>();
		
		for(int i = 0; i < dtm.getRowCount(); i++){
			TreeSet<String> result = new TreeSet<String>();
			
			// Parse one line
			boolean success = parseOneFunc(frame, result, dtm, i);
			if(!success){
				return false;
			}
			
			// Add new parameters to the list
			for(String token : result){
				if(!paramsList.contains(token)){
					paramsList.add(token);
				}
			}
			
			// Store the function information
			functions.put((String)dtm.getValueAt(i, 1), result);
			
		}	
		// Initiate the algorithm
		builder = new Configuration.ConfigurationBuilder(paramsList);
		for (TreeSet<String> set : functions.values()) {
			builder.setClique(set);
		}
		manager = new ConfigurationManager(builder.build());
		manager.start();

		String succMsg = "All " + paramsList.size() + " parameters are parsed successfully!";
		JOptionPane.showMessageDialog(frame, succMsg, "Parse Successfully", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
	
	// Parse a single function and its parameters
	private boolean parseOneFunc(JFrame frame, TreeSet<String> result, DefaultTableModel dtm, int row){
		String temp = (String)(dtm.getValueAt(row, 2));
		if(temp == null){
			// No valid parameter for this function
			String errMsg = "Line " + (row+1) + ": Function " + (String)(dtm.getValueAt(row, 1) + " has no parameter." );
			JOptionPane.showMessageDialog(frame, errMsg, "Parse Failed",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(temp,", ");
		while(st.hasMoreTokens()){
			if(!result.add(st.nextToken())){
				// The same parameter is inputed multiple times
				String errMsg = "Line " + (row+1) + ": Function " + (String)(dtm.getValueAt(row, 1) + " has duplicated parameters." );
				JOptionPane.showMessageDialog(frame, errMsg, "Parse Failed",JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		if(result.isEmpty()){
			String errMsg = "Line " + (row+1) + ": Function " + (String)(dtm.getValueAt(row, 1) + " has no parameter." );
			JOptionPane.showMessageDialog(frame, errMsg, "Parse Failed",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	// Access methods
	public UndirectedSparseGraph<String, String> getGraph(){
		if(manager != null){
			constructGraph();
		}
		return graph;
	}
	
	public HashSet<String> getAllParams(){
		return paramsList;
	}
	
	public DelegateTree<String,String> getTree(){
		if(manager != null){
			constructTree();
		}else{
		    tree.addVertex("Empty Tree!");
		}
		return tree;
	}

	private void constructTree(){
		TreeMap<TreeSet<String>,TreeSet<String>> edgeList = manager.getCurrentTree();
		DirectedSparseGraph<String, String> rawTree = new DirectedSparseGraph<String, String>();
		String root = null;
		for(Map.Entry<TreeSet<String>,TreeSet<String>> entry : edgeList.entrySet()){
			TreeSet<String> childSet = entry.getKey();
			TreeSet<String> parentSet = entry.getValue();
			String childName = childSet.toString();
			rawTree.addVertex(childName);
			if(parentSet != null){
				String parentName = parentSet.toString();
				rawTree.addVertex(parentName);
				rawTree.addEdge(parentName+"2"+childName, parentName, childName, EdgeType.DIRECTED);
			}else{
			    root = childName;
			}
		}
		tree = new DelegateTree<String,String>(rawTree);
		tree.setRoot(root);
	}
	
	private void constructGraph(){
		Configuration config = manager.getCurrentConfiguration();
		graph = new UndirectedSparseGraph<String, String>();
		for(String name : config.nodes.keys()){
			if(!config.eliminated.containsKey(name)){
				graph.addVertex(name);
			}
			for(String nbr : config.nodes.get(name).neighbors){
				if(name.compareTo(nbr) < 0){
					graph.addEdge(name+"2"+nbr, name,nbr);
				}else if(name.compareTo(nbr) > 0){
					graph.addEdge(nbr+"2"+name, nbr,name);
				}
			}
		}
	}
	
	public HashSet<String> getFinishedParams(){
		return new HashSet<String>(manager.getCurrentConfiguration().eliminated.keys());
	}
	
	public ArrayList<String> getRestParams(){
	    return manager.getAvailableChoices();
	}
	
	public String getTreeWidth(){
		if(manager != null){
			return Double.toString(manager.getWidth());
		}else{
			return "-1";
		}
	}
	
	public void nextStep(String choice){
	    manager.setNextChoice(choice);
	}
	
	public void rollback(){
	    manager.pop();
	}
}

package edu.umd.isr.seil.brian;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.umd.isr.seil.brian.treesearch.Configuration;
import edu.umd.isr.seil.brian.treesearch.Configuration.Node;
import edu.umd.isr.seil.brian.treesearch.ConfigurationSearch;
import edu.umd.isr.seil.brian.treesearch.TreeSearch;

public class ParamsDB {
	private HashSet<String> paramsList;
	private TreeMap<String,TreeSet<String>> functions;
	private UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<String, String>();;
	private DelegateTree<String,String> tree = new DelegateTree<String,String>();
	private Configuration.ConfigurationBuilder builder;
	private TreeSearch<ConfigurationSearch> searcher;
	
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
			
			
			// Initiate the algorithm
			builder = new Configuration.ConfigurationBuilder(paramsList);
			for (TreeSet<String> set : functions.values()) {
				builder.setClique(set);
			}	    
			searcher = Configuration.initSearch(builder.build());
		}
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
		if(searcher != null){
			constructGraph();
		}
		return graph;
	}
	
	public HashSet<String> getAllParams(){
		return paramsList;
	}
	
	public DelegateTree<String,String> getTree(){
		if(searcher != null){
			constructTree();
		}
		return tree;
	}
	
	private void constructTree(){
		TreeMap<TreeSet<String>,TreeSet<String>> rawTree = searcher.root.data.getTree();
		tree = new DelegateTree<String,String>();
		for(Map.Entry<TreeSet<String>,TreeSet<String>> entry : rawTree.entrySet()){
			TreeSet<String> childSet = entry.getKey();
			TreeSet<String> parentSet = entry.getValue();
			String childName = childSet.toString();
			tree.addVertex(childName);
			if(parentSet != null){
				String parentName = parentSet.toString();
				tree.addVertex(parentName);
				tree.addEdge(parentName+"2"+childName, parentName, childName);
			}
		}
	}
	
	private void constructGraph(){
		Configuration config = searcher.root.data.reduced;
		graph = new UndirectedSparseGraph<String, String>();
		for(String name : config.nodes.keys()){
			if(!config.eliminated.containsKey(name)){
				graph.addVertex(name);
				for(String nbr : config.nodes.get(name).neighbors){
					if(name.compareTo(nbr) < 0){
						graph.addEdge(name+"2"+nbr, name,nbr);
					}else if(name.compareTo(nbr) > 0){
						graph.addEdge(nbr+"2"+name, nbr,name);
					}
				}
			}
		}
	}
	
	public HashSet<String> getFinishedParams(){
		return new HashSet<String>(searcher.root.data.reduced.eliminated.keys());
	}
	
	public HashSet<String> getRestParams(){
		HashSet<String> result = new HashSet<String>(searcher.root.data.reduced.nodes.keys());
		result.removeAll(searcher.root.data.reduced.eliminated.keys());
		return result;
	}
	
	public double getTreeWidth(){
		if(searcher != null){
			double score = searcher.getMinPath().getScore();
			return Math.log(1/score);
		}else{
			return -1;
		}
	}
}

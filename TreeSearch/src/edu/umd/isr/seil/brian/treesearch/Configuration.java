package edu.umd.isr.seil.brian.treesearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import edu.umd.isr.seil.brian.util.Joiner;
import edu.umd.isr.seil.brian.util.RbSet;
import edu.umd.isr.seil.brian.util.RbTree;
import edu.umd.isr.seil.brian.util.RbTreeIterator;

/* Fill-ins can be determined by checking against the original configuration */
public class Configuration {
  public final RbTree<String, Node> nodes;
  public final RbTree<String, Integer> eliminated;
  
  public static TreeSearch<ConfigurationSearch> initSearch(Configuration config) {
    ConfigurationSearch search = new ConfigurationSearch(config, config.reduceSimplical());
    if (search.getNumChoices() == 0) {
      return new TreeSearch<ConfigurationSearch>(new Program.Terminal<ConfigurationSearch>(null, 0, search));
    } else {
      return new TreeSearch<ConfigurationSearch>(new Program.Branch<ConfigurationSearch>(null, 0, search));
    }
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    boolean isFirst = true;
    for (RbTreeIterator<String,Node> it = nodes.iterator(); !it.isEnd(); it = it.next()) {
      if (isFirst) isFirst = false;
      else builder.append(", ");
      builder.append(it.key());
      Integer elim = eliminated.get(it.key());
      if (elim != null) builder.append('(').append(elim).append(')');
      builder.append(" -> {");
      Node node = it.value();
      builder.append(Joiner.join(node.neighbors,", "));
      builder.append("}");
    }
    return builder.toString();
  }

  private Configuration(RbTree<String,Node> nodes) {
    this(nodes, RbTree.<String,Integer>getEmpty());
  }
  
  private Configuration(RbTree<String,Node> nodes, RbTree<String,Integer> eliminated) {
    this.nodes = nodes;
    this.eliminated = eliminated;
  }
  
  public int getRemainingCount() {
    return nodes.size()-eliminated.size();
  }
  
  // This method is valid only after an elimination order has been established.
  public int computeTreeWidth() {
    int max = 0;
    for (RbTreeIterator<String,Integer> it = eliminated.iterator(); !it.isEnd(); it = it.next()) {
      Node node = nodes.get(it.key());
      int count = 0;
      for (String neighbor : node.neighbors) {
        if (eliminated.get(neighbor) > it.value()) count++;
      }
      if (count > max) max = count;
    }
    return max;
  }
  
  public Configuration eliminate(String name) {
    if (eliminated.containsKey(name)) throw new RuntimeException();
    RbTree<String,Node> newNodes = nodes;
    Node victim = nodes.get(name);
    RbSet<String> neighborhood = RbSet.<String>empty();
    for (String neighborName : victim.neighbors) {
      if (eliminated.containsKey(neighborName)) continue;
      neighborhood = neighborhood.add(neighborName);
    }
    for (String neighbor : neighborhood) {
      newNodes = newNodes.put(neighbor, nodes.get(neighbor).addNeighbors(neighborhood));
    }
    return new Configuration(newNodes, eliminated.put(name, eliminated.size()));
  }
  
  public Configuration reduceSimplical() {
    String simplical = findSimplical();
    if (simplical == null) return this;
    
    return eliminate(simplical).reduceSimplical();
  }

  public Iterator<String> nameIterator() {
    return new Iterator<String>() {
      RbTreeIterator<String,Node> state = nodes.iterator();
      
      public boolean hasNext() {
        return !state.isEnd();
      }
      public String next() {
        String rv = state.key();
        state = state.next();
        return rv;
      }
      public void remove() { throw new UnsupportedOperationException(); }
    };
  }
  
  // Returns null if no simplical node found.
  public String findSimplical() {
    for (RbTreeIterator<String,Node> it = nodes.iterator(); !it.isEnd(); it=it.next()) {
      if (eliminated.containsKey(it.key())) continue;
      if (it.value().isSimplical(this)) return it.key();
    }
    return null;
  }
  
  public boolean isSimplical(String id) {
    return nodes.get(id).isSimplical(this);
  }

  public boolean isConnected(String n1, String n2) {
    return nodes.get(n1).isConnected(n2);
  }
  
  public static class Node {
    public final String name;    
    public final RbSet<String> neighbors;  // includes fill-ins

    public Node(String name, ArrayList<String> neighbors) {
      this.name = name;
      RbSet<String> temp = RbSet.<String>getEmpty();
      for (String neighbor : neighbors) {
        temp = temp.add(neighbor);
      }
      this.neighbors = temp;
    }
    
    private Node(String name, RbSet<String> neighbors) {
      this.name = name;
      this.neighbors = neighbors;
    }
    
    public Node addNeighbors(RbSet<String> newNeighbors) {
      return new Node(name, neighbors.union(newNeighbors.contains(name) ? newNeighbors.remove(name) : newNeighbors));      
    }
    
    public boolean isConnected(String id) {
      return neighbors.contains(id);
    }

    // Checks mutual connectivity of all 1-hop nodes that are not eliminated
    public boolean isSimplical(Configuration el) {
      for (RbTreeIterator<String,Void> it1 = neighbors.rbTreeIterator(); !it1.isEnd(); it1 = it1.next()) {
        if (el.eliminated.containsKey(it1.key())) continue;
        for (RbTreeIterator<String,Void> it2 = it1.next(); !it2.isEnd(); it2 = it2.next()) {
          if (el.eliminated.containsKey(it2.key())) continue;
          if (!el.isConnected(it1.key(), it2.key())) return false;
        }
      }
      return true;
    }
    
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(name).append(" : ");
      builder.append(Joiner.join(neighbors,", "));
      return builder.toString();
    }
  }
  
  public static class ConfigurationBuilder {
    final int N;
    TreeMap<String,Integer> nameToIdx;
    String[] idxToName;
    boolean[][] connectivity;  // graph is symmetric
    
    public ConfigurationBuilder(Iterable<String> name) {
      this(name.iterator());
    }
    
    public ConfigurationBuilder(Iterator<String> nameIt) {
      nameToIdx = new TreeMap<String,Integer>();
      int idx = 0;
      while (nameIt.hasNext()) {
        String nextName = nameIt.next();
        nameToIdx.put(nextName, idx++);
      }
      this.N = this.nameToIdx.size();
      idxToName = new String[N];
      for (Map.Entry<String,Integer> entry : nameToIdx.entrySet()) {
        idxToName[entry.getValue()] = entry.getKey();
      }
      connectivity = new boolean[N][];
      for (int i = 0; i < N; i++) {
        connectivity[i] = new boolean[N];
      }
    }
    
    // enforces symmetry
    public void setRow(int rowNum, boolean ... bs) {
      if (!(rowNum >= 0 && rowNum < N) || bs.length != N) throw new RuntimeException();
      for (int i = 0; i < N; i++) {
        connectivity[rowNum][i] = bs[i];
        connectivity[i][rowNum] = bs[i];
      }
    }
    
    // enforces symmetry
    public void setRow(int rowNum, ArrayList<Boolean> row) {
      if (!(rowNum >= 0 && rowNum < N) || row.size() != N) throw new RuntimeException();
      for (int i = 0; i < N; i++) {
        connectivity[rowNum][i] = row.get(i);
        connectivity[i][rowNum] = row.get(i);
      }
    }
    
    // enforces symmetry
    public void set(int i, int j, boolean val) {
      connectivity[i][j] = val;
      connectivity[j][i] = val;
    }
    
    public void setClique(Iterable<String> set) {
      ArrayList<String> temp = new ArrayList<String>();
      for (String str : set) {
        temp.add(str);
      }
      for (int i = 0; i < temp.size(); i++) {
        for (int j = i+1; j < temp.size(); j++) {
          set(nameToIdx.get(temp.get(i)), nameToIdx.get(temp.get(j)), true);
        }
      }
    }
    
    public Configuration build() {
      RbTree<String,Node> nodes = RbTree.<String,Node>empty();
      for (Map.Entry<String, Integer> entry : nameToIdx.entrySet()) {
        ArrayList<String> neighbors = new ArrayList<String>();
        boolean[] row = connectivity[entry.getValue()];
        for (int i = 0; i < N; i++) {
          if (row[i]) {
            neighbors.add(idxToName[i]);
          }
        }
        nodes = nodes.put(entry.getKey(), new Node(entry.getKey(), neighbors));
      }
      return new Configuration(nodes);
    }
  }
}

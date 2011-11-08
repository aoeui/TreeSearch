package edu.umd.isr.seil.brian.treesearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umd.isr.seil.brian.util.RbTree;
import edu.umd.isr.seil.brian.util.RbTreeIterator;
import edu.umd.isr.seil.brian.util.LexicalCompare;

public class ConfigurationSearch implements Program.Search<ConfigurationSearch> {
  public final Configuration current;
  public final Configuration reduced;
  
  public ConfigurationSearch(Configuration current, Configuration reduced) {
    this.current = current;
    this.reduced = reduced;
  }
  public int getNumChoices() {
    return reduced.nodes.size() - reduced.eliminated.size();
  }
  
  public String toString() {
    return reduced.toString();
  }
  
  // Returns a graph (a mapping from nodes to their parent pointers)
  // Assumes that getNumChoices returns 0.
  public TreeMap<TreeSet<String>,TreeSet<String>> getTree() {
    if (getNumChoices() > 0) throw new RuntimeException();

    String[] eo = new String[reduced.nodes.size()];
    for (RbTreeIterator<String,Integer> elimIt = reduced.eliminated.iterator(); !elimIt.isEnd(); elimIt = elimIt.next()) {
      eo[elimIt.value()] = elimIt.key();
    }    
    ArrayList<TreeSet<String>> cliques = new ArrayList<TreeSet<String>>();
    for (int i = 0; i < eo.length; i++) {
      String elim = eo[i];
      TreeSet<String> clique = new TreeSet<String>();
      Configuration.Node next = reduced.nodes.get(elim);
      clique.add(next.name);
      for (String neighbor : next.neighbors) {
        if (reduced.eliminated.get(neighbor) < i) continue;
        clique.add(neighbor);
      }
      cliques.add(clique);
    }
    for (int i = cliques.size()-1; i >= 0; i--) {
      for (int j = 0; j < cliques.size(); j++) {
        if (i == j) continue;
        
        if (cliques.get(j).containsAll(cliques.get(i))) {
          System.out.println("Removing clique " + cliques.get(i) + " contained in " + cliques.get(j));
          cliques.remove(i);
          break;
        }
      }
    }

    // have all cliques, sort them by rank
    Collections.sort(cliques, new CliqueRanker(reduced.eliminated));
    Collections.reverse(cliques);
    
    TreeMap<TreeSet<String>,TreeSet<String>> tree = new TreeMap<TreeSet<String>,TreeSet<String>>(LexicalCompare.<String>comparatorInstance());
    tree.put(cliques.get(0), null);
    for (int i = 1; i < cliques.size(); i++) {
      TreeSet<String> clique = cliques.get(i);
      // find the previous clique sharing the most nodes to connect to
      TreeSet<String> maxShare = null;
      int maxShareCount = -1;
      for (int j = 0; j < i; j++) {
        TreeSet<String> intersection = new TreeSet<String>(cliques.get(j));
        intersection.retainAll(clique);
        int shareCount = intersection.size();
        if (shareCount > maxShareCount) {
          maxShareCount = shareCount;
          maxShare = cliques.get(j);
        }
      }
      if (maxShareCount < 1) throw new RuntimeException();
      tree.put(clique, maxShare);
    }
    return tree;
  }
  
  public static class CliqueRanker implements Comparator<TreeSet<String>> {
    RbTree<String,Integer> ranks;
    public CliqueRanker(RbTree<String,Integer> ranks) {
      this.ranks = ranks;
    }
    
    public int compare(TreeSet<String> set1, TreeSet<String> set2) {
      int diff = getRank(set1) - getRank(set2);
      if (diff == 0) {
        return LexicalCompare.compare(set1, set2);
      } else {
        return diff > 0 ? 1 : -1;
      }
    }
    
    private int getRank(TreeSet<String> set) {
      int maxRank = Integer.MIN_VALUE;
      
      for (String str : set) {
        int currRank = ranks.get(str);
        if (currRank > maxRank) {
          maxRank = currRank;
        }
      }
      return maxRank;
    }
  }
  
  public final static Comparator<TreeSet<String>> CLIQUE_COMP = TreeSetComparator.INSTANCE;
  public static class TreeSetComparator implements Comparator<TreeSet<String>> {
    public final static TreeSetComparator INSTANCE = new TreeSetComparator();
    private TreeSetComparator() {}
    public int compare(TreeSet<String> set1, TreeSet<String> set2) {
      return LexicalCompare.compare(set1, set2);
    }
  }
  
  // Only valid when getNumChoices == 0
  public double getScore() {
    if (scoreCache == null) scoreCache = 1/Math.exp(reduced.computeTreeWidth());
    return scoreCache;
  }
  Double scoreCache = null;
  
  public ConfigurationSearch choose(int choice) {
    if (choice < 0 || choice >= getNumChoices()) throw new RuntimeException();
    int counter = 0;
    String selected = null;
    for (RbTreeIterator<String,?> it = reduced.nodes.iterator(); !it.isEnd(); it = it.next()) {
      if (reduced.eliminated.containsKey(it.key())) continue;
      if (counter == choice) {
        selected = it.key();
        break;
      } else {
        counter++;
      }
    }
    Configuration next = reduced.eliminate(selected);
    Configuration nextReduced = next.reduceSimplical();
    return new ConfigurationSearch(next, nextReduced);
  }
}

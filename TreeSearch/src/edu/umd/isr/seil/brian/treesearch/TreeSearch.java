package edu.umd.isr.seil.brian.treesearch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import edu.umd.isr.seil.brian.util.Ptr;

public class TreeSearch<S extends Program.Search<S>> {
  public final int N = 1000;  // aim for N "particles"
  public final double EPSILON = 0.5;  // amount of time spent in blind search
  final Random rng = new Random();
  
  public final Program<S> root;
  public final Node rootNode;
  
  Program.Terminal<S> minPath; // best scoring path (head is root)
  
  public TreeSearch(Program<S> prog) {
    this.root = prog;
    this.rootNode = new Terminal(prog, null, 0);
  }
  
  public Program.Terminal<S> getMinPath() {
    return minPath;
  }
  
  public S getMinConfig() {
    return minPath.data;
  }
  
  public void runEpoch() {
    // loop until interrupted (can happen anytime)

    // make N trials resulting in max 2N nodes
    for (int i = 0; i < N; i++) {
      rootNode.sample(rng.nextDouble() < EPSILON, N);
    }
    rootNode.prune(N);  // enforce number of nodes <= N
  }
  
  public void testMinPath(Program.Terminal<S> term) {
    if (minPath == null) {
      minPath = term;
    } else {
      if (minPath.getScore() > term.getScore()) {
        minPath = term;
      }
    }
  }
  
  public abstract class Node {
    public final Fork parent;  // null for root node
    public final int id;
    
    double aggregateSum;
    int aggregateCount;
    
    ArrayList<Node> children;

    public Node(Fork parent, int id) {
      this.parent = parent;
      this.id = id;
      
      if (parent != null) parent.setChild(id, this);
    }

    public void setAggregateSum(double val) {
      this.aggregateSum = val;
    }
    public void setAggregateCount(int val) {
      this.aggregateCount = val;
    }
    public abstract Double getMean();
    public abstract int getTotalCount();
    
    public abstract void prune(int remaining);
    
    public abstract boolean sample(boolean isBlind, int remaining);
  }
  
  public class Fork extends Node {
    public final Program.Branch<S> branch;

    ArrayList<Node> children;

    public Fork(Program.Branch<S> branch, Fork parent, int id) {
      super(parent, id);
      this.branch = branch;
      children = new ArrayList<Node>();
      for (int i = 0; i < branch.getNumChoices(); i++) {
        children.add(new Terminal(branch.choose(i), this, i));
      }
    }
    
    public void setChild(int idx, Node child) {
     children.set(idx, child);
    }
    public Node getChild(int idx) {
      return children.get(idx);
    }
    public int getNumChildren() {
      return children.size();
    }

    boolean meanDirty = true;
    Double meanCache = null;
    public Double getMean() {
      if (!meanDirty) return meanCache;
      double sum = 0;
      int childCount = 0;
      int childSamples = 0;
      for (Node child : children) {
        if (child == null) continue;
        childCount++;
        childSamples += child.getTotalCount();
        sum += child.getMean();
      }
      meanDirty = false;
      if (childCount > 0) {
        meanCache = ((sum/childCount)*childSamples + aggregateSum)/((double)childSamples+(double)aggregateCount);
      } else if (aggregateCount > 0) {
        meanCache = (double)aggregateSum/(double)aggregateCount;        
      } else {
        meanCache = null;
      }
      return meanCache;
    }
    public int getTotalCount() {
      int rv = aggregateCount;
      for (Node child : children) {
        rv += child.getTotalCount();
      }
      return rv; 
    }
    
    public Terminal demote() {
      Terminal rv = new Terminal(branch, parent, id);
      int totalCount = getTotalCount();
      rv.setAggregateCount(totalCount);
      rv.setAggregateSum(getMean()*totalCount);
      return rv;
    }

    // There are different ways to compute allocations
    // 1. All children have allocations -> use these values
    // 2. Some children have allocations ->
    //    a. There is an aggregate value -> use the aggregate to estimate
    //    b. There is no aggregate value -> use the mean of existing to estimate
    // 3. No children have allocations -> uniform
    // Compute the allocation
    public double[] computeChildWeights() {
      LinkedList<Integer> useDefault = new LinkedList<Integer>();
      double meanSum = 0;
      double[] rv = new double[getNumChildren()];
      for (int i = 0; i < getNumChildren(); i++) {
        Node child = getChild(i);
        Double mean = child.getMean();
        if (mean != null) {
          rv[i] = mean;
          meanSum += mean;
        } else {
          useDefault.add(i);
        }
      }
      if (useDefault.size() > 0) {
        if (useDefault.size() < getNumChildren()) { // partial information
          double defaultWeight = 0;
          if (aggregateCount == 0) {  // no aggregate data -> assume unknown nodes are similar to known ones
            defaultWeight = meanSum / (getNumChildren()-useDefault.size());
          } else {  // use aggregate data as a reference point
            double aggregateMean = (double)aggregateSum / (double)aggregateCount;
            defaultWeight = (aggregateMean*getNumChildren()-meanSum)/useDefault.size();
            if (defaultWeight < 0) defaultWeight = 0;
          }
          for (int idx : useDefault) {
            rv[idx] = defaultWeight;
          }
          meanSum += defaultWeight*useDefault.size();
        } else {  // no information (blind)
          for (int i = 0; i < getNumChildren(); i++) {
            rv[i] = 1;
          }
          meanSum = getNumChildren();
        }
      }
      for (int i = 0; i < getNumChildren(); i++) {
        rv[i] /= meanSum;
      }
      return rv;
    }
    
    // Returns true when the sample uncovered new information
    public boolean sample(boolean isBlind, int remaining) {
      double[] weights = computeChildWeights();
      int choice = isBlind ? rng.nextInt(getNumChildren()) : chooseByWeight(weights);
      Node next = getChild(choice);
      boolean mod = next.sample(isBlind, (int)(weights[choice]*(remaining-1)));
      if (mod) meanDirty = true;
      return mod;
    }
  
    public int chooseByWeight(double[] weights) {
      double thresh = rng.nextDouble();
      double sum = 0;
      for (int i = 0; i < weights.length; i++) {
        sum += weights[i];
        if (sum >= thresh) return i;
      }
      return weights.length-1;
    }
    
    public void prune(int remaining) {
      if (remaining < 1+getNumChildren()) {
        demote();
      } else {
        double[] weights = computeChildWeights();
        for (int i = 0; i < getNumChildren(); i++) {
          getChild(i).prune((int)(weights[i]*(remaining-1)));
        }
      }
    }
  }
  
  public class Terminal extends Node {
    public final Program<S> prog;

    public Terminal(Program<S> prog, Fork parent, int id) {
      super(parent, id);
      this.prog = prog;
    }
    public Double getMean() {
      return aggregateCount > 0 ? (aggregateSum / aggregateCount) : null;
    }
    public int getTotalCount() { return aggregateCount; }
    
    public Fork promote(Program.Branch<S> branch) {
      Fork f = new Fork(branch, parent, id);
      f.setAggregateCount(aggregateCount);
      f.setAggregateSum(aggregateSum);
      return f;
    }
    
    public boolean sample(final boolean isBlind, final int remaining) {
      final Ptr<Boolean> rvPtr = new Ptr<Boolean>();
      prog.accept(new Program.Visitor<S>() {
        public void visitTerminal(Program.Terminal<S> terminal) {
          if (aggregateCount == 0) {
            aggregateCount = 1;
            aggregateSum = terminal.getScore();
            testMinPath(terminal);
            rvPtr.value = true;
          } else {
            rvPtr.value = false;
          }
        }
        public void visitBranch(Program.Branch<S> branch) {
          if (branch.getNumChoices() + 1 <= remaining) {
            Fork f = promote(branch);
            rvPtr.value = f.sample(isBlind, remaining);
          } else {
            Program.Terminal<S> terminal = branch.randomRecurse(rng);
            aggregateCount++;
            aggregateSum += terminal.getScore();
            rvPtr.value = true;
            testMinPath(terminal);
          }
        }
      });
      return rvPtr.value;
    }
    
    // Terminals cannot be pruned further
    public void prune(int remaining) {}
  }
}

package edu.umd.isr.seil.brian.treesearch;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umd.isr.seil.brian.treesearch.Program.Branch;
import edu.umd.isr.seil.brian.treesearch.Program.Terminal;
import edu.umd.isr.seil.brian.util.Ptr;
import edu.umd.isr.seil.brian.util.RbTreeIterator;
import edu.umd.isr.seil.brian.util.Stack;

public class ConfigurationManager implements Runnable {
  public final Configuration root;
  // Add an LRU here.
  TreeMap<Stack<String>,TreeSearch<ConfigurationSearch>> cache;

  Stack<String> currentChoices;
  TreeSearch<ConfigurationSearch> currentSearcher;
  
  boolean isGo = true;
  
  public ConfigurationManager(Configuration root) {
    this.root = root;
    cache = new TreeMap<Stack<String>,TreeSearch<ConfigurationSearch>>(Stack.STRING_COMP);
    currentChoices = Stack.<String>emptyInstance();
    ConfigurationSearch search = new ConfigurationSearch(root, root.reduceSimplical());
    Program<ConfigurationSearch> prog = search.getNumChoices() > 0 ? new Program.Branch<ConfigurationSearch>(null, 0, search) : new Program.Terminal<ConfigurationSearch>(null, 0, search);
    
    currentSearcher = new TreeSearch<ConfigurationSearch>(prog);
    cache.put(currentChoices, currentSearcher);
  }
  
  public synchronized Configuration getCurrentConfiguration() {
    return currentSearcher.root.data.reduced;
  }
  
  public void start() {
    new Thread(this).start();
  }
  
  public synchronized void stop() {
    isGo = false;
  }
  
  public void run() {
    while (isGo) {
      synchronized (this) {
        currentSearcher.runEpoch();
      }
    }
  }
  
  public synchronized TreeSearch<ConfigurationSearch> getCurrentSearcher() { return currentSearcher; }
  public synchronized Stack<String> getCurrentChoices() { return currentChoices; }

  public synchronized TreeMap<TreeSet<String>,TreeSet<String>> getCurrentTree() {
    TreeMap<TreeSet<String>,TreeSet<String>> rv = currentSearcher.getMinPath().data.getTree();
    System.out.println(rv);
    return rv;
  }
  
  public synchronized int getWidth() {
    return (int)Math.log(1/currentSearcher.getMinPath().getScore());
  }
  
  public synchronized ArrayList<String> getAvailableChoices() {
    Configuration config = currentSearcher.root.data.reduced;
    ArrayList<String> rv = new ArrayList<String>();
    for (RbTreeIterator<String,?> it = config.nodes.iterator(); !it.isEnd(); it = it.next()) {
      if (config.eliminated.containsKey(it.key())) continue;
      rv.add(it.key());
    }
    return rv;
  }
  
  public synchronized void setNextChoice(final String str) {
    Stack<String> nextKey = currentChoices.push(str);
    final Ptr<TreeSearch<ConfigurationSearch>> searcher = new Ptr<TreeSearch<ConfigurationSearch>>();
    searcher.value = cache.get(nextKey);
    if (searcher.value == null) {
      currentSearcher.root.accept(new Program.Visitor<ConfigurationSearch>() {
        @Override
        public void visitBranch(Branch<ConfigurationSearch> branch) {
          Program<ConfigurationSearch> prog = branch.choose(getAvailableChoices().indexOf(str));
          searcher.value = new TreeSearch<ConfigurationSearch>(prog);
        }
        @Override
        public void visitTerminal(Terminal<ConfigurationSearch> terminal) {
          throw new RuntimeException();
        }
      });
      cache.put(nextKey, searcher.value);
    }
    currentSearcher = searcher.value;
    currentChoices = nextKey;
  }
  
  public synchronized void pop() {
    skipTo(currentChoices.tail());
  }
  
  public synchronized void skipTo(Stack<String> choice) {
    TreeSearch<ConfigurationSearch> searcher = cache.get(choice);
    if (searcher != null) {
      currentSearcher = searcher;
      currentChoices = choice;
      return;
    }
    Stack<String> prefix = findClosest(choice);
    if (prefix == null) {
      ConfigurationSearch search = new ConfigurationSearch(root, root.reduceSimplical());
      Program<ConfigurationSearch> prog = search.getNumChoices() > 0 ? new Program.Branch<ConfigurationSearch>(null, 0, search) : new Program.Terminal<ConfigurationSearch>(null, 0, search);
      searcher = new TreeSearch<ConfigurationSearch>(prog);
      currentSearcher = searcher;
      currentChoices = Stack.<String>emptyInstance();
      chooseSequence(choice.reverse());
    } else {
      currentSearcher = cache.get(prefix);
      currentChoices = prefix;
      Stack<String> toChoose = Stack.<String>emptyInstance();
      Stack<String> choiceIt = choice;
      while (choiceIt.head() != prefix.head()) {
        toChoose = toChoose.push(choiceIt.head());
        choiceIt = choiceIt.tail();
      }
      chooseSequence(toChoose);
    }
  }
  
  // the top of the stack should be the next choice (backwards the usual ordering)
  public synchronized void chooseSequence(Stack<String> choices) {
    if (choices.isEmpty()) return;
    setNextChoice(choices.head());
    chooseSequence(choices.tail());
  }
  
  // Keeps taking prefixes until it finds one recursively
  private Stack<String> findClosest(Stack<String> key) {
    if (cache.get(key) == null) {
      if (key.isEmpty()) return null;
      return findClosest(key.tail());
    }
    return key;
  }
}

package edu.umd.isr.seil.brian.treesearch;

import java.util.Random;

public abstract class Program<S extends Program.Search<S>> {
  public final Program<S> parent;  // null if root
  public final int id;  // root has ID 0, otherwise gives index number wrt parent
  public final S data;
  
  public interface Search<S extends Search<S>> {
    public int getNumChoices();
    public S choose(int choice);
    public double getScore();
  }
  
  public Program(Program<S> parent, int id, S data) {
    this.parent = parent;
    this.id = id;
    this.data = data;
  }
  
  public abstract void accept(Visitor<S> visitor);
  public abstract boolean isTerminal();
  
  public abstract Terminal<S> randomRecurse(Random rng);

  public static class Branch<S extends Search<S>> extends Program<S> {
    public Branch(Program<S> parent, int id, S data) {
      super(parent, id, data);
    }
    public int getNumChoices() {
      return data.getNumChoices();
    }
    public Program<S> choose(int choice) {
      S next = data.choose(choice);
      return next.getNumChoices() > 0 ? new Branch<S>(this, choice, next) : new Terminal<S>(this, choice, next);
    }
    public void accept(Visitor<S> visitor) {
      visitor.visitBranch(this);
    }
    public final boolean isTerminal() { return false; }

    public Terminal<S> randomRecurse(Random rng) {
      return choose(rng.nextInt(getNumChoices())).randomRecurse(rng);
    }
  }
  
  public static class Terminal<S extends Search<S>> extends Program<S> {
    public Terminal(Program<S> parent, int id, S data) {
      super(parent, id, data);
    }
    public double getScore() {
      return data.getScore();
    }
    public void accept(Visitor<S> visitor) {
      visitor.visitTerminal(this);
    }
    public final boolean isTerminal() { return true; }
    public Terminal<S> randomRecurse(Random rng) { return this; }
  }
  
  public static interface Visitor<S extends Search<S>> {
    public void visitBranch(Branch<S> branch);
    public void visitTerminal(Terminal<S> terminal);
  }
}

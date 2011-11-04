package edu.umd.isr.seil.brian.test;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.TreeMap;
import java.util.TreeSet;

import edu.umd.isr.seil.brian.treesearch.Configuration;
import edu.umd.isr.seil.brian.treesearch.ConfigurationSearch;
import edu.umd.isr.seil.brian.treesearch.TreeSearch;

public class TestSearch {
  public static void main(String[] args) throws Exception {
    TreeMap<String,TreeSet<String>> constraints = new TreeMap<String,TreeSet<String>>();
    
    BufferedReader reader = new BufferedReader(new FileReader(args[0]));
    String next = reader.readLine();
    while (next != null) {
      String[] strings = next.split("\\s");
      TreeSet<String> vars = new TreeSet<String>();
      for (int i = 1; i < strings.length; i++) {
        vars.add(strings[i]);
      }
      constraints.put(strings[0],vars);
      next = reader.readLine();
    }
    TreeSet<String> allVars = new TreeSet<String>();
    for (TreeSet<String> set : constraints.values()) {
      allVars.addAll(set);
    }
    Configuration.ConfigurationBuilder builder = new Configuration.ConfigurationBuilder(allVars);
    for (TreeSet<String> set : constraints.values()) {
      builder.setClique(set);
    }
    TreeSearch<ConfigurationSearch> searcher = Configuration.initSearch(builder.build());
    for (int i = 0; i < 10; i++) {
      searcher.runEpoch();
      double score = searcher.getMinPath().getScore();
      double width = Math.log(1/score);
      System.out.println("Best Score = " + score + " (width = " + width + ")");
      System.out.println(searcher);
    }
    System.out.println(searcher.getMinPath().data.reduced);
    System.out.println(searcher.getMinConfig().getTree());
  }
}

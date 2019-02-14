# Semantic Merge Conflicts Soot Library 

This project aims to implement a library of soot analysis to detect semantic merge conflicts. 
Current supported algorithms: 

   * Intraprocedural use-def conflicts: This algorithm identifies conflicts 
    that occur when a contribution from "left" defines a variable 
    that a contribution of "right" uses. Its implementation is mostly based on 
    intraprocedural dataflow analysis. 
    
   * Reachability conflicts: This algorithm identifies conflicts that occur 
   when there is a interprocedural flow path from a left statement contribution from 
   to a right statement contribution.
   
    

## TODO

   * when computing the number of conflicts, consider that multiple JIMPLE statements might appear in the same line. 
   For this reason, we would rather use a data structure to avoid reporting multiple conflicts from the same pairs 
   of source-sink lines. 
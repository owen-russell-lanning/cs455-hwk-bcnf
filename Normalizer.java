import java.sql.Time;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides static methods for performing normalization
 * 
 * @author <YOUR NAME>
 * @version <DATE>
 */
public class Normalizer {

  /**
   * Performs BCNF decomposition
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of relations (as attribute sets) that are in BCNF
   */
  public static Set<Set<String>> BCNFDecompose(Set<String> rel, FDSet fdset) {
    System.out.println("BCNF START");
    System.out.println("Current Schema = " + rel);
    // First test if the given relation is already in BCNF with respect to
    // the provided FD set.
    if (!isBCNF(rel, fdset)) {

      Set<Set<String>> superkeys = findSuperkeys(rel, fdset);
      System.out.println("Current schema's superkeys = " + superkeys);

      // Identify a nontrivial FD that violates BCNF. Split the relation's
      // attributes using that FD, as seen in class.
      FD violator = null;
      for (FD fd : fdset) {
        // check if trivial
        if (!fd.leftContains(fd.getRight())) {
          // not trivial
          if (!superkeys.contains(fd.getLeft())) {
            violator = fd;
            break;
          }

        }
      }

      System.out.println("Splitting on " + violator);
      Set<String> r1 = new HashSet<>();
      Set<String> r2 = new HashSet<>();
      r1.addAll(violator.getLeft());
      r1.addAll(violator.getRight());

      r2.addAll(violator.getLeft());
      r2.addAll(rel);
      r2.removeAll(violator.getRight());

      System.out.println("Left Schema = " + r1);


      System.out.println("Right Schema = " + r2);


      // Redistribute the FDs in the closure of fdset to the two new
      // relations (R_Left and R_Right) as follows:

      FDSet c = FDUtil.fdSetClosure(fdset);
      FDSet f1 = new FDSet();
      FDSet f2 = new FDSet();

      // Iterate through closure of the given set of FDs, then union all attributes
      // appearing in the FD, and test if the union is a subset of the R_Left (or
      // R_Right) relation. If so, then the FD gets added to the R_Left's (or
      // R_Right's) FD
      // set. If the union is not a subset of either new relation, then the FD is
      // discarded

      for (FD fd : c) {
        Set<String> union = new HashSet<String>();
        union.addAll(fd.getLeft());
        union.addAll(fd.getRight());

        if (r1.containsAll(union)) {
          f1.add(fd);
        }
        if (r2.containsAll(union)) {
          f2.add(fd);
        }
      }

      Set<Set<String>> r1Supers = findSuperkeys(r1, f1);
      System.out.println("Left Schema's Superkeys = " + r1Supers);

      Set<Set<String>> r2Supers = findSuperkeys(r2, f2);
      System.out.println("Right Schema's Superkeys = " + r2Supers);


      // Repeat the above until all relations are in BCNF
      Set<Set<String>> f1R = BCNFDecompose(r1, f1);
      Set<Set<String>> f2R = BCNFDecompose(r2, f2);
      f2R.addAll(f1R);


      return f2R;

    }
    System.out.println("BCNF END");

    Set<Set<String>> outSet = new HashSet<Set<String>>();
    outSet.add(rel);

    return outSet;
  }

  /**
   * Tests whether the given relation is in BCNF. A relation is in BCNF iff the
   * left-hand attribute set of all nontrivial FDs is a super key.
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return true if the relation is in BCNF with respect to the specified FD set
   */
  public static boolean isBCNF(Set<String> rel, FDSet fdset) {
    Set<Set<String>> superkeys = findSuperkeys(rel, fdset);
    FDSet c = FDUtil.fdSetClosure(fdset);
    for (FD fd : c) {
      // check if trivial
      if (!fd.leftContains(fd.getRight())) {
        // not trivial
        if (!superkeys.contains(fd.getLeft())) {
          return false; // not bcnf, retrun
        }

      }
    }

    return true;
  }

  /**
   * This method returns a set of super keys
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of super keys
   */
  public static Set<Set<String>> findSuperkeys(Set<String> rel, FDSet fdset) {

    // sanity check: are all the attributes in the FD set even in the
    // relation? Throw an IllegalArgumentException if not.
    for (FD fd : fdset) {
      for (String a : fd.getLeft()) {
        if (!rel.contains(a)) {
          throw new IllegalArgumentException("FD refers to unknown attributes: " + fd);
        }

      }
      for (String a : fd.getRight()) {
        if (!rel.contains(a)) {
          throw new IllegalArgumentException("FD refers to unknown attributes: " + fd);
        }

      }
    }

    // iterate through each subset of the relation's attributes, and test
    // the attribute closure of each subset

    Set<Set<String>> keys = new HashSet<Set<String>>();
    FDSet cloneFdset = new FDSet(fdset);
    cloneFdset.add(new FD(rel, rel));
    FDSet c = FDUtil.fdSetClosure(cloneFdset);

    Set<Set<String>> rels = FDUtil.powerSet(rel);
    for (Set<String> s : rels) {

      for (FD fd : c) {
        if (s.containsAll(fd.getLeft())) {
          if (fd.getRight().equals(rel)) {

            keys.add(s);
          }
        }
      }
    }

    return keys;
  }

}
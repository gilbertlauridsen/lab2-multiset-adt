import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A recursive tree data structure, which provides services required of the
 * MultiSet ADT. See TreeMultiSet, which uses this class.
 * This is a simplified version of the Tree data structure adapted from CSC148.
 * See the python version for additional documentation.
 */
public class Tree {
    private static final Random RANDOM = new Random();

    // The item stored at this tree's root, or null if the tree is empty.
    // We use Integer here so that we can set the root to null, like the BST class does.
    private Integer root;

    // The list of all subtrees of this tree.
    private final List<Tree> subtrees = new ArrayList<>();

    // === Representation Invariants ===
    // - If root is null then subtrees is an empty list. This represents an empty tree.
    // - subtrees may be empty when root is not null. This represents a tree
    //   consisting of just one node.

    /**
     * Create an empty tree.
     */
    public Tree() {
        this.root = null;
    }

    /**
     * Create a tree with the given root value and no subtrees.
     *
     * @param root the root value
     */
    public Tree(int root) {
        this.root = root;
    }

    /**
     * Create a tree with the given root value and subtrees.
     *
     * @param root     the root value
     * @param subtrees the subtrees of this tree (the list is copied, not aliased)
     */
    public Tree(int root, List<Tree> subtrees) {
        this.root = root;
        this.subtrees.addAll(subtrees);
    }

    /**
     * @return whether this tree is empty
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * @return the number of items contained in this tree
     */
    public int size() {
        if (this.isEmpty()) {
            return 0;
        }
        int size = 1; // count the root
        for (Tree subtree : subtrees) {
            size += subtree.size();
        }
        return size;
    }

    /**
     * @param item the item to count
     * @return the number of occurrences of item in this tree
     */
    public int count(int item) {
        if (this.isEmpty()) {
            return 0;
        }
        int num = 0;
        if (root == item) {
            num += 1;
        }
        for (Tree subtree : subtrees) {
            num += subtree.count(item);
        }
        return num;
    }

    /**
     * Return a string representation of this tree. For each node, its item is
     * printed before any of its descendants' items, and the output is indented
     * by depth. You may find this method helpful for debugging.
     */
    @Override
    public String toString() {
        return strIndented(0);
    }

    private String strIndented(int depth) {
        if (this.isEmpty()) {
            return "";
        }
        StringBuilder s = new StringBuilder("  ".repeat(depth) + root + "\n");
        for (Tree subtree : subtrees) {
            // Note that the depth argument to the recursive call is modified.
            s.append(subtree.strIndented(depth + 1));
        }
        return s.toString();
    }

    /**
     * @return the average of all the values in this tree, or 0.0 if this tree is empty
     */
    public double average() {
        if (this.isEmpty()) {
            return 0.0;
        }
        // Java has no tuples, so the helper returns {total, size} as an array.
        int[] totalAndSize = averageHelper();
        return (double) totalAndSize[0] / totalAndSize[1];
    }

    private int[] averageHelper() {
        if (this.isEmpty()) {
            return new int[] {0, 0};
        }
        int total = root;
        int size = 1;
        for (Tree subtree : subtrees) {
            int[] sub = subtree.averageHelper();
            total += sub[0];
            size += sub[1];
        }
        return new int[] {total, size};
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tree)) {
            return false;
        }
        Tree other = (Tree) obj;
        if (this.isEmpty() && other.isEmpty()) {
            return true;
        } else if (this.isEmpty() || other.isEmpty()) {
            return false;
        }
        return root.equals(other.root) && subtrees.equals(other.subtrees);
    }

    @Override
    public int hashCode() {
        return isEmpty() ? 0 : 31 * root + subtrees.hashCode();
    }

    /**
     * @param item the item to look for
     * @return whether item is in this tree
     */
    public boolean contains(int item) {
        if (this.isEmpty()) {
            return false;
        }
        // item may be in root, or subtrees
        if (root == item) {
            return true;
        }
        for (Tree subtree : subtrees) {
            if (subtree.contains(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return a list of all the leaf items in this tree
     */
    public List<Integer> leaves() {
        List<Integer> leaves = new ArrayList<>();
        if (this.isEmpty()) {
            return leaves;
        } else if (subtrees.isEmpty()) {
            leaves.add(root);
            return leaves;
        }
        for (Tree subtree : subtrees) {
            leaves.addAll(subtree.leaves());
        }
        return leaves;
    }

    // -------------------------------------------------------------------------
    // Mutating methods
    // -------------------------------------------------------------------------

    /**
     * Delete *one* occurrence of the given item from this tree.
     * Do not modify this tree if it does not contain item.
     *
     * @param item the item to delete
     * @return true if item was deleted, and false otherwise
     */
    public boolean deleteItem(int item) {
        if (this.isEmpty()) {
            // The item is not in the tree.
            return false;
        } else if (root == item) {
            // We've found the item: now delete it.
            this.deleteRoot();
            return true;
        }
        // Loop through each subtree, and stop the first time the item is deleted.
        for (int i = 0; i < subtrees.size(); i++) {
            Tree subtree = subtrees.get(i);
            if (subtree.deleteItem(item)) {
                if (subtree.isEmpty()) {
                    // The subtree is now empty, so remove it from the list of subtrees.
                    // This is only safe because we return immediately afterwards.
                    subtrees.remove(i);
                }
                return true;
            }
        }
        // The item does not appear in this tree.
        return false;
    }

    /**
     * Remove the root item of this tree.
     * Precondition: this tree is non-empty.
     */
    private void deleteRoot() {
        if (subtrees.isEmpty()) {
            // This is a leaf. Deleting the root gives an empty tree.
            root = null;
        } else {
            // Replace the root with the root of the last subtree.
            Tree chosenSubtree = subtrees.remove(subtrees.size() - 1);
            root = chosenSubtree.root;
            subtrees.addAll(chosenSubtree.subtrees);
        }
    }

    /**
     * Insert item into this tree using the following algorithm.
     * 1. If the tree is empty, item is the new root of the tree.
     * 2. If the tree has a root but no subtrees, create a new tree containing
     *    the item, and make this new tree a subtree of the original tree.
     * 3. Otherwise, pick a random number between 1 and 3 inclusive.
     *    - If the random number is 3, create a new tree containing the item,
     *      and make this new tree a subtree of the original.
     *    - If the random number is a 1 or 2, pick one of the existing subtrees
     *      at random, and recursively insert the new item into that subtree.
     *
     * @param item the item to insert
     */
    public void insert(int item) {
        if (this.isEmpty()) {
            root = item;
        } else if (subtrees.isEmpty()) {
            subtrees.add(new Tree(item));
        } else if (RANDOM.nextInt(3) + 1 == 3) {
            subtrees.add(new Tree(item));
        } else {
            int subtreeIndex = RANDOM.nextInt(subtrees.size());
            subtrees.get(subtreeIndex).insert(item);
        }
    }

    /**
     * Insert item into this tree as a child of parent.
     * If parent appears more than once in this tree, item is only inserted once.
     *
     * @param item   the item to insert
     * @param parent the item to insert under
     * @return true if successful, or false if parent is not in this tree
     */
    public boolean insertChild(int item, int parent) {
        if (this.isEmpty()) {
            return false;
        } else if (root == parent) {
            subtrees.add(new Tree(item));
            return true;
        }
        for (Tree subtree : subtrees) {
            if (subtree.insertChild(item, parent)) {
                return true;
            }
        }
        return false;
    }
}

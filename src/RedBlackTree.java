
public class RedBlackTree {
	   Node root; // Package access, for testing

	   static final int BLACK = 1;
	   static final int RED = 0;
	   private static final int NEGATIVE_RED = -1;
	   private static final int DOUBLE_BLACK = 2;

	   /**
	      Constructs an empty tree.
	   */
	   public RedBlackTree()
	   {  
	      root = null;
	   }
	   
	   /**
	      Inserts a new node into the tree.
	      @param obj the object to insert
	   */
	   public void add(Grid obj) 
	   {  
	      Node newNode = new Node();
	      newNode.data = obj;
	      newNode.left = null;
	      newNode.right = null;
	      if (root == null) { root = newNode; }
	      else { root.addNode(newNode); }
	      fixAfterAdd(newNode);
	   }

	   /**
	      Tries to find a grid in the tree; updates the grid if found
	      @param obj the object to find
	      @param time the current timestamp
	      @param dim the current number of dimensions
	      @return true if the object is contained in the tree
	   */
	   public boolean find(Grid obj,int time,int dim)
	   {
	      Node current = root;
	      while (current != null)
	      {
	         int d = current.data.compareTo(obj);
	         if (d == 0){
	        	 current.data.updateCharVector(time, dim);
	        	 return true;
	         }
	         else if (d > 0) current = current.left;
	         else current = current.right;
	      }
	      return false;
	   }
	   
	   /**
	      Tries to remove an object from the tree. Does nothing
	      if the object is not contained in the tree.
	      @param obj the object to remove
	   */
	   public void remove(Grid obj)
	   {
	      // Find node to be removed

	      Node toBeRemoved = root;
	      boolean found = false;
	      while (!found && toBeRemoved != null)
	      {
	         int d = toBeRemoved.data.compareTo(obj);
	         if (d == 0) { found = true; }
	         else 
	         {
	            if (d > 0) { toBeRemoved = toBeRemoved.left; }
	            else { toBeRemoved = toBeRemoved.right; }
	         }
	      }

	      if (!found) { return; }

	      // toBeRemoved contains obj

	      // If one of the children is empty, use the other

	      if (toBeRemoved.left == null || toBeRemoved.right == null)
	      {
	         Node newChild;
	         if (toBeRemoved.left == null) { newChild = toBeRemoved.right; }
	         else { newChild = toBeRemoved.left; }

	         fixBeforeRemove(toBeRemoved); 
	         
	         if (toBeRemoved.parent == null) { root = newChild; } // Found in root
	         else { toBeRemoved.replaceWith(newChild); }
	         return;
	      }
	      
	      // Neither subtree is empty

	      // Find smallest element of the right subtree

	      Node smallest = toBeRemoved.right;
	      while (smallest.left != null)
	      {
	         smallest = smallest.left;
	      }

	      // smallest contains smallest child in right subtree
	         
	      // Move contents, unlink child

	      toBeRemoved.data = smallest.data;
	      fixBeforeRemove(smallest);
	      smallest.replaceWith(smallest.right);
	   }
	   
	   /**
	      Visits all nodes of this tree in order.
	      @param v the visitor to apply at each node
	   */
	   public void inOrderVisit(Visitor v)
	   {
	      inOrderVisit(root, v);
	   }
	   
	   public static interface Visitor
	   {
	      /**
	         This method is called at each node.
	         @param n the visited node
	      */
	      void visit(Node n);
	   }
	   
	   private static void inOrderVisit(Node n, Visitor v)
	   {
	      if (n == null) return;
	      inOrderVisit(n.left, v);
	      v.visit(n);
	      inOrderVisit(n.right, v);
	   }
	   
	   /**
	      A node of a red-black tree stores a data item and references
	      of the child nodes to the left and to the right. The color
	      is the "cost" of passing the node; 1 for black or 0 for red.
	      Temporarily, it may be set to 2 or -1. 
	   */
	   static class Node
	   {  
	      public Grid data;
	      public Node left;
	      public Node right;
	      public Node parent;
	      public int color;
	      
	      /**
	         Constructs a red node with no data.
	      */
	      public Node() {}
	      
	      /**
	         Sets the left child and updates its parent pointer.
	         @param child the new left child
	      */
	      public void setLeftChild(Node child)
	      {
	         left = child;
	         if (child != null) { child.parent = this; }
	      }
	      
	      /**
	         Sets the right child and updates its parent pointer.
	         @param child the new right child
	      */
	      public void setRightChild(Node child)
	      {
	         right = child;
	         if (child != null) { child.parent = this; }
	      }
	      
	      /**
	       * Updates the parent's and replacement node's links when this node is replaced.
	       * @param replacement the node that replaces this node
	       */
	      public void replaceWith(Node replacement)
	      {
	    	  if (parent == null) return;
	    	  if (this == parent.left) parent.setLeftChild(replacement);
	    	  else parent.setRightChild(replacement);
	      }
	      
	      /**
	         Inserts a new node as a descendant of this node.
	         @param newNode the node to insert
	      */
	      public void addNode(Node newNode)
	      {  
	         int comp = newNode.data.compareTo(data);
	         if (comp < 0)
	         {  
	            if (left == null) 
	            {
	               left = newNode;
	               left.parent = this;
	            }
	            else { left.addNode(newNode); }
	         }
	         else if (comp > 0)
	         {  
	            if (right == null) 
	            {
	               right = newNode;
	               right.parent = this;
	            }
	            else { right.addNode(newNode); }
	         }
	      }
	   }

	   /**
	      Restores the tree to a red-black tree after a node has been added.
	      @param newNode the node that has been added
	   */
	   private void fixAfterAdd(Node newNode)
	   {
	      if (newNode.parent == null) 
	      { 
	         newNode.color = BLACK; 
	      }
	      else
	      {
	         newNode.color = RED;
	         if (newNode.parent.color == RED) { fixDoubleRed(newNode); }
	      }
	   }

	   /** 	
	     Fixes the tree so that it is a red-black tree after a node has been removed.
	     @param removed the node that is to be removed
	   */
	   private void fixBeforeRemove(Node removed)
	   {
	      if (removed.color == RED) { return; }

	      if (removed.left != null || removed.right != null) // It is not a leaf
	      {
	         // Color the child black
	         if (removed.left == null) { removed.right.color = BLACK; }
	         else { removed.left.color = BLACK; }
	      }	   
	      else { bubbleUp(removed.parent); }
	   }
	   
	   /**
	      Move a charge from two children of a parent
	      @param parent a node with two children, or null (in which case nothing is done)
	   */
	   private void bubbleUp(Node parent)
	   {
	      if (parent == null) { return; }
	      parent.color++;
	      parent.left.color--;
	      parent.right.color--;
		   
	      Node child = parent.left;
	      if (child.color == NEGATIVE_RED) { fixNegativeRed(child); return; }
	      else if (child.color == RED)
	      {
	         if (child.left != null && child.left.color == RED) 
	         { 
	            fixDoubleRed(child.left); 
	            return; 
	         }
	         if (child.right != null && child.right.color == RED) 
	         { 
	            fixDoubleRed(child.right); return; 
	         }
	      }
	   
	      child = parent.right;
	      if (child.color == NEGATIVE_RED) { fixNegativeRed(child); return; }
	      else if (child.color == RED)
	      {
	         if (child.left != null && child.left.color == RED) 
	         { 
	            fixDoubleRed(child.left); 
	            return; 
	         }
	         if (child.right != null && child.right.color == RED) 
	         { 
	            fixDoubleRed(child.right); 
	            return; 
	         }
	      }
		  
	      if (parent.color == DOUBLE_BLACK) 
	      { 
	         if (parent.parent == null) { parent.color = BLACK; }
	         else { bubbleUp(parent.parent); }
	      }
	   }
	   
	   
	   /**
	      Fixes a "double red" violation.
	      @param child the child with a red parent
	   */
	   private void fixDoubleRed(Node child)
	   {
	      Node parent = child.parent;      
	      Node grandParent = parent.parent;
	      if (grandParent == null) { parent.color = BLACK; return; }
	      Node n1, n2, n3, t1, t2, t3, t4;
	      if (parent == grandParent.left)
	      {
	         n3 = grandParent; t4 = grandParent.right;
	         if (child == parent.left)
	         {
	            n1 = child; n2 = parent;
	            t1 = child.left; t2 = child.right; t3 = parent.right;
	         }
	         else
	         {
	            n1 = parent; n2 = child;
	            t1 = parent.left; t2 = child.left; t3 = child.right; 
	         }
	      }
	      else
	      {
	         n1 = grandParent; t1 = grandParent.left;
	         if (child == parent.left)
	         {
	            n2 = child; n3 = parent;
	            t2 = child.left; t3 = child.right; t4 = parent.right;
	         }
	         else
	         {
	            n2 = parent; n3 = child;
	            t2 = parent.left; t3 = child.left; t4 = child.right; 
	         }         
	      }
	      
	      if (grandParent == root)
	      {
	         root = n2;
	         n2.parent = null;
	      }
	      else
	      {
	         grandParent.replaceWith(n2);
	      }
	      
	      n1.setLeftChild(t1);
	      n1.setRightChild(t2);
	      n2.setLeftChild(n1);
	      n2.setRightChild(n3);
	      n3.setLeftChild(t3);
	      n3.setRightChild(t4);
	      n2.color = grandParent.color - 1; 
	      n1.color = BLACK;
	      n3.color = BLACK;

	      if (n2 == root)
	      {
	         root.color = BLACK;
	      }
	      else if (n2.color == RED && n2.parent.color == RED)
	      {
	         fixDoubleRed(n2);
	      }
	   }
	   
	   /**
	      Fixes a "negative red" violation.
	      @param negRed the negative red node
	   */
	   private void fixNegativeRed(Node negRed)
	   {	
	      Node n1, n2, n3, n4, t1, t2, t3, child;
	      Node parent = negRed.parent;
	      if (parent.left == negRed)
	      {
	         n1 = negRed.left;
	         n2 = negRed;
	         n3 = negRed.right;
	         n4 = parent;
	         t1 = n3.left;
	         t2 = n3.right;
	         t3 = n4.right;
	         n1.color = RED;
	         n2.color = BLACK;
	         n4.color = BLACK;
	         n2.setRightChild(t1);
	         Grid temp = n4.data; n4.data = n3.data; n3.data = temp;
	         n3.setLeftChild(t2);
	         n3.setRightChild(t3);
	         n4.setRightChild(n3);
	         child = n1;
	      }
	      else
	      {
	         n4 = negRed.right;
	         n3 = negRed;
	         n2 = negRed.left;
	         n1 = parent;
	         t3 = n2.right;
	         t2 = n2.left;
	         t1 = n1.left;
	         n4.color = RED;
	         n3.color = BLACK;
	         n1.color = BLACK;
	         n3.setLeftChild(t3);
	         Grid temp = n1.data; n1.data = n2.data; n2.data = temp;
	         n2.setRightChild(t2);
	         n2.setLeftChild(t1);
	         n1.setLeftChild(n2);
	         child = n4;
	      }
		   
	      if (child.left != null && child.left.color == RED) 
	      { 
	         fixDoubleRed(child.left); 
	         return; 
	      }
	      if (child.right != null && child.right.color == RED) 
	      { 
	         fixDoubleRed(child.right);  
	      }
	   }
}

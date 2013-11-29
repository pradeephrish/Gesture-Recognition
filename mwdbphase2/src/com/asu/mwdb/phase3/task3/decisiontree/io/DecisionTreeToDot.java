package com.asu.mwdb.phase3.task3.decisiontree.io;

import java.util.*;
import java.text.*;
import com.asu.mwdb.phase3.task3.decisiontree.misc.*;

public class DecisionTreeToDot {

	static final String header = "digraph decision_tree {\n";
	static final String tailer = "}\n";
	private DecisionTree tree;
	private NumberFormat formatter;

	/**
	 * Build a new decision tree drawer.
	 * 
	 * @param tree
	 *            The tree to draw.
	 **/
	public DecisionTreeToDot(DecisionTree tree) {
		if (tree == null)
			throw new IllegalArgumentException("Invalid 'null' argument");
		this.tree = tree;
		formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(3);
	}

	/**
	 * Produce the .dot file content encoded in a String.
	 * 
	 * @return The .dot file content.
	 **/
	public String produce() {
		String s = header;

		Iterator nodesIterator = tree.breadthFirstIterator();
		while (nodesIterator.hasNext()) {
			Node node = (Node) nodesIterator.next();

			s += produceLabel(node);

			if (node instanceof TestNode)
				s += produceTransitions((TestNode) node);
		}

		return s + tailer;
	}

	private String produceTransitions(TestNode node) {
		String s = "";

		for (int i = 0; i < node.nbSons(); i++)
			s += id(node) + " -> " + id(node.son(i)) + " [label = \""
					+ node.test().issueToString(i) + "\"];\n";

		return s;
	}

	private String produceLabel(Node node) {
		String label = "";

		if (node instanceof TestNode) {
			label += ((TestNode) node).test().toString();

			if (node instanceof ScoreTestNode)
				label += " (score= "
						+ formatter.format(((ScoreTestNode) node).getScore())
						+ ")";
		} else /* Leaf */{
			LeafNode leafNode = (LeafNode) node;
			String goalValueString = "";
			String entropyString = "";

			label += "Leaf";

			if (leafNode.goalValue() != null)
				if (tree.getGoalAttribute() != null)
					goalValueString = " - "
							+ tree.getGoalAttribute().valueToString(
									leafNode.goalValue());
				else
					goalValueString = " - " + leafNode.goalValue();

			if (leafNode.getGoalValueDistribution() != null) {
				double[] distribution = leafNode.getGoalValueDistribution();

				label += "[";
				for (int i = 0; i < distribution.length; i++)
					label += " " + formatter.format(distribution[i]);
				label += " ]";
			} else if (leafNode.getEntropy() >= 0)
				entropyString = " Entropy: "
						+ formatter.format(leafNode.getEntropy());

			label += goalValueString + entropyString;
		}

		label += " Weight = " + formatter.format(node.weight);

		return id(node) + " [label=\"" + label + "\"];\n";
	}

	private String id(Node node) {
		return "\"" + node + "\"";
	}
}

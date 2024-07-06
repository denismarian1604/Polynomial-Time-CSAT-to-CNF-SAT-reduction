import java.io.PrintWriter;
import java.util.*;

public class GateTree {
    // type can be AND, OR or VAR
    String type;
    int node_id;
    List<GateTree> children;
    private static int variable_last_idx, nr_clauses;

    public GateTree(String type, int node_id) {
        this.type = type;
        this.node_id = node_id;
        this.children = new ArrayList<>();
    }

    public GateTree() {
        this.type = null;
        this.children = null;
        this.node_id = -1;
    }

    public String getType() {
        return type;
    }

    public int getNodeId() {
        return node_id;
    }

    public List<GateTree> getChildren() {
        return children;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNodeId(int node_id) {
        this.node_id = node_id;
    }

    public void setChildren(List<GateTree> children) {
        this.children = children;
    }

    public void setVarLastIdx(int var_last_idx) {
        variable_last_idx = var_last_idx;
    }

    public int getNrClauses() {
        return nr_clauses;
    }

    public static int findNodeLine(String[][] tree_info, int node_id) {
        for (int i = 0; i < tree_info.length; i++) {
            if (tree_info[i][tree_info[i].length - 1].equals(Integer.toString(node_id))) {
                return i;
            }
        }
        return -1;
    }

    public static GateTree buildTree(String[][] tree_info, int node_line
            , int output_variable, int negated) {
        GateTree node = new GateTree();

        // if the current node is nowhere at the end of a line, it is a variable
        if (node_line == -1) {
            node.setType("VAR");
            node.setNodeId(output_variable * (negated * (-2) + 1));

            return node;
        }

        // if the current node is an AND or an OR, check if it is negated and if so, turn it into the other
        // and apply the negation to its children
        if (tree_info[node_line][0].equals("AND") || tree_info[node_line][0].equals("OR")) {
            if (negated == 0)
                node.setType(tree_info[node_line][0]);
            else
            if (tree_info[node_line][0].equals("AND"))
                node.setType("OR");
            else
                node.setType("AND");

            node.setNodeId((negated * (-2) + 1) * Integer.parseInt(tree_info[node_line][tree_info[node_line].length - 1]));
            node.setChildren(new ArrayList<>());

            for (int i = 1; i < tree_info[node_line].length - 1; i++) {
                int child_line = findNodeLine(tree_info, Integer.parseInt(tree_info[node_line][i]));
                node.getChildren().add(buildTree(tree_info, child_line, Integer.parseInt(tree_info[node_line][i]), negated));
            }
        } else if (tree_info[node_line][0].equals("NOT")) {
            // if the current node is a NOT, check if it is negated and if so cancel the negation out
            // if it is not negated, apply the negation
            int child_line = findNodeLine(tree_info, Integer.parseInt(tree_info[node_line][1]));
            return buildTree(tree_info, child_line, Integer.parseInt(tree_info[node_line][1]), (negated + 1) % 2);
        }


        return node;
    }

    public boolean checkValid(ArrayList<GateTree> combination, GateTree node) {
        // check if a node is already in the combination
        for (GateTree gateTree : combination) {
            if (gateTree.getNodeId() == node.getNodeId()) {
                return false;
            }
        }

        return true;
    }

    public boolean checkCombinationValid(ArrayList<ArrayList<GateTree>> combinations
            , ArrayList<GateTree> currentCombination) {
        // check if the current combination is valid
        // if it is, return true
        // if it is not, return false

        // check if the current combination is already in the list of combinations
        for (ArrayList<GateTree> combination : combinations) {
            if (combination.size() != currentCombination.size())
                continue;

            boolean found = true;
            for (GateTree gateTree : combination) {
                if (!currentCombination.contains(gateTree)) {
                    found = false;
                    break;
                }
            }

            if (found)
                return false;
        }

        // check if the current combination contains a variable and its negation
        // in that case the combination is always satisfiable so no need to add it
        for (GateTree gateTree : currentCombination) {
            if (gateTree.getType().equals("VAR")) {
                for (GateTree gateTree1 : currentCombination) {
                    if (gateTree1.getType().equals("VAR") && gateTree1.getNodeId() == gateTree.getNodeId() * (-1)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void generateCombinationRecursive(ArrayList<ArrayList<GateTree>> combinations
            , ArrayList<GateTree> currentCombination, int child_idx) {

        // if the current child index is greater than the number of children,
        if (child_idx >= this.getChildren().size()) {
            // check if combination is valid
            // if the combination is valid, add it to the list of combinations
            if (checkCombinationValid(combinations, currentCombination))
                combinations.add(new ArrayList<>(currentCombination));

            return;
        }

        // if the current child is a variable
        if (this.getChildren().get(child_idx).getType().equals("VAR")) {
            // add the variable to the current combination
            if (checkValid(currentCombination, this.getChildren().get(child_idx)))
                currentCombination.add(this.getChildren().get(child_idx));

            // call the function recursively
            generateCombinationRecursive(combinations, currentCombination, child_idx + 1);
        } else if (this.getChildren().get(child_idx).getType().equals("AND")) {
            // if the current child is an AND
            // add the child's children one by one to the current combination
            // then call the function recursively

            // boolean variable to check if we enter any of the conditions
            boolean entered = false;
            for (GateTree child : this.getChildren().get(child_idx).getChildren()) {

                // check if the child isn't already in the formula
                if (!checkValid(currentCombination, child))
                    continue;

                // check if the child is an OR
                if (child.getType().equals("OR")) {
                    entered = true;

                    // add the child's children one by one to the current combination
                    for (GateTree childChild : child.getChildren()) {
                        // check if the child isn't already in the formula
                        if (!checkValid(currentCombination, childChild))
                            continue;

                        currentCombination.add(childChild);

                    }
                    // call the function recursively
                    generateCombinationRecursive(combinations, currentCombination, child_idx + 1);
                    // remove the child's children from the current combination
                    for (GateTree childChild : child.getChildren()) {
                        currentCombination.remove(childChild);
                    }

                } else {
                    entered = true;
                    currentCombination.add(child);
                    // call the function recursively
                    generateCombinationRecursive(combinations, currentCombination, child_idx + 1);
                    // remove the child from the current combination
                    currentCombination.remove(currentCombination.size() - 1);
                }
            }

            // even if the current child is an AND, it might not have any OR children
            // or it might not have valid children
            // either way, call the function recursively,
            // without adding anything to the current combination or removing anything
            if (!entered)
                generateCombinationRecursive(combinations, currentCombination, child_idx + 1);
        }
    }

    private ArrayList<ArrayList<GateTree>> generateCombinations() {
        ArrayList<ArrayList<GateTree>> combinations = new ArrayList<>();
        ArrayList<GateTree> combination = new ArrayList<>();

        // add the "VAR" children to the combination
        for (GateTree child : this.getChildren()) {
            if (child.getType().equals("VAR") && checkValid(combination, child)) {
                combination.add(child);
            }
        }

        // call the recursive function
        generateCombinationRecursive(combinations, combination, 0);

        // return the generated combinations
        return combinations;
    }

    public void distributeOR() {

        // distribute OR over AND
        // (A AND B) OR C = (A OR C) AND (B OR C)
        // define a new node for each new clause
        ArrayList<ArrayList<GateTree>> combinations = generateCombinations();

        // modify the current clause's children
        this.getChildren().clear();
        // add the new clauses to the current clause's children
        for (ArrayList<GateTree> combination : combinations) {

            GateTree newClause = new GateTree("OR", variable_last_idx++);
            newClause.setChildren(combination);

            this.getChildren().add(newClause);

        }
    }

    public GateTree convertToCNF() {
        if (this.getType().equals("VAR")) {
            // If it's a variable, no further conversion needed
            return this;
        }

        if (this.getType().equals("AND")) {
            // Convert children to CNF
            for (int i = 0; i < this.getChildren().size(); i++) {
                if (this.getChildren().get(i).getType().equals("VAR"))
                    continue;

                this.getChildren().set(i, this.getChildren().get(i).convertToCNF());
            }
            return this;
        }

        if (this.getType().equals("OR")) {
            // Convert children to CNF
            for (int i = 0; i < this.getChildren().size(); i++) {
                if (this.getChildren().get(i).getType().equals("VAR"))
                    continue;
                this.getChildren().set(i, this.getChildren().get(i).convertToCNF());
            }

            // Distribute OR over AND to get CNF if there is at least an AND child
            boolean containsAnd = this.getChildren().stream().anyMatch(child -> child.getType().equals("AND"));
            if (containsAnd) {
                this.distributeOR();
                this.setType("AND");
            }

            return this;
        }

        return this;
    }

    public void getCNF(StringBuilder CNF) {
        if (this.getType().equals("VAR")) {
            CNF.append(this.getNodeId()).append(" ");
            return;
        }

        if (this.getType().equals("AND")) {
            for (GateTree child : this.getChildren()) {
                child.getCNF(CNF);
            }
            return;
        }

        if (this.getType().equals("OR")) {
            CNF.append("(");
            for (int i = 0; i < this.getChildren().size(); i++) {
                this.getChildren().get(i).getCNF(CNF);
                if (i != this.getChildren().size() - 1) {
                    CNF.append(" + ");
                }
            }
            CNF.append(") ");
        }
    }

    // combine OR gates within OR gates
    public void combineOrInOrGates() {

        if (this.getType().equals("OR")) {
            // if the current node is an OR gate
            // check if it has OR children
            boolean containsOr = this.getChildren().stream().anyMatch(child -> child.getType().equals("OR"));

            if (containsOr) {
                // if it has OR children, combine them
                // get the current children in a new list where we will add the new children
                ArrayList<GateTree> newChildren = new ArrayList<>(this.getChildren());

                for (GateTree child : this.getChildren()) {
                    if (child.getType().equals("OR")) {
                        // add the child's children to the new children list
                        // but only if they are not already in the list

                        containsOr = child.getChildren().stream().anyMatch(child1 -> child1.getType().equals("OR"));

                        if (containsOr) {
                            child.combineOrInOrGates();
                        }

                        newChildren.addAll(child.getChildren());

                        for (GateTree childChild : child.getChildren()) {
                            if (!newChildren.contains(childChild)) {
                                newChildren.add(childChild);
                            }
                        }
                    } else if (child.getType().equals("AND")) {
                        containsOr = child.getChildren().stream().anyMatch(child1 -> child1.getType().equals("OR"));

                        if (containsOr) {
                            child.combineOrInOrGates();
                        }
                    }
                }
                // remove the OR children
                newChildren.removeIf(child -> child.getType().equals("OR"));

                // assign the new children to the current node
                this.getChildren().clear();
                this.getChildren().addAll(newChildren);
            }
        } else {
            if (!this.getType().equals("VAR"))
                for (GateTree child : this.getChildren()) {
                    child.combineOrInOrGates();
                }
        }
    }

    public void combineAndInAndGates() {

        if (this.getType().equals("AND")) {
            // if the current node is an AND gate
            // check if it has AND children
            boolean containsAnd = this.getChildren().stream().anyMatch(child -> child.getType().equals("AND"));

            if (containsAnd) {
                // if it has AND children, combine them
                // get the current children in a new list where we will add the new children
                ArrayList<GateTree> newChildren = new ArrayList<>(this.getChildren());

                for (GateTree child : this.getChildren()) {
                    if (child.getType().equals("AND")) {
                        containsAnd = child.getChildren().stream().anyMatch(child1 -> child1.getType().equals("AND"));

                        if (containsAnd) {
                            child.combineAndInAndGates();
                        }

                        newChildren.addAll(child.getChildren());
                    } else if (child.getType().equals("OR")) {
                        containsAnd = child.getChildren().stream().anyMatch(child1 -> child1.getType().equals("AND"));

                        if (containsAnd) {
                            child.combineAndInAndGates();
                        }
                    }
                }
                // remove the AND children
                newChildren.removeIf(child -> child.getType().equals("AND"));

                // assign the new children to the current node
                this.getChildren().clear();
                this.getChildren().addAll(newChildren);
            }
        } else {
            if (!this.getType().equals("VAR"))
                for (GateTree child : this.getChildren()) {
                    if (child.getType().equals("AND"))
                        child.combineAndInAndGates();
                }
        }
    }

    public void countClauses(String[] CNF) {
        boolean inClause = false;
        for (String s : CNF) {
            if (s.equals("(")) {
                inClause = true;
                nr_clauses++;
            } else if (s.equals(")")) {
                inClause = false;
            } else if (!inClause) {
                nr_clauses++;
            }
        }
    }

    public void printFormattedCNF(String[] CNF, PrintWriter writer) {
        boolean inClause = false;
        for (int i = 0; i < CNF.length; i++) {
            String s = CNF[i];
            if (s.equals("(")) {
                inClause = true;
            } else if (s.equals(")")) {
                writer.write("0\n");
                inClause = false;
            } else if (inClause && !s.equals("+") && !s.equals("")) {
                writer.write(Integer.parseInt(s) + " ");
            } else if (!inClause && !s.equals("+") && !s.equals("")) {
                writer.write(Integer.parseInt(s) + " 0\n");
            }
        }
    }
}

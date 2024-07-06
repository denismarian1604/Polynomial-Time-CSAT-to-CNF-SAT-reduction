import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Tema2 {
    public static void main(String[] args) throws IOException {
        // open input file
        File inFile = new File(args[0]);

        // open output file
        File outFile = new File(args[1]);
        // create writer
        PrintWriter myWriter = new PrintWriter(outFile);

        // open Scanner
        Scanner myScanner = new Scanner(inFile);
        // read the first line
        String first_line = myScanner.nextLine();

        // get the number of inputs and the output variable
        String[] first_line_info = first_line.split(" ");
        int nr_inputs = Integer.parseInt(first_line_info[0]);
        int output_variable = Integer.parseInt(first_line_info[1]);

        // initialize the tree
        String[][] temp_tree_info = new String[100][62];
        int cnt = 0;
        while (myScanner.hasNextLine()) {
            String line = myScanner.nextLine();
            String[] info = line.split(" ");
            for (int i = 0; i < info.length; i++) {
                temp_tree_info[cnt][i] = info[i];
            }
            cnt++;
        }
        myScanner.close();

        // rebuild the tree_info array with the correct size
        String[][] tree_info = new String[cnt][];

        for (int i = 0; i < cnt; i++) {
            int actual_length = 0;
            for (int j = 0; j < temp_tree_info[i].length && temp_tree_info[i][j] != null; j++) {
                actual_length++;
            }

            tree_info[i] = new String[actual_length];
            for (int j = 0; j < actual_length; j++) {
                tree_info[i][j] = temp_tree_info[i][j];
            }
        }

        // get the SAT formula
        int start_node_line = GateTree.findNodeLine(tree_info, output_variable);
        GateTree tree = GateTree.buildTree(tree_info, start_node_line, output_variable, 0);
        tree.setVarLastIdx(output_variable);

        // check from the beginning if the formula starts with an OR and if it contains a VAR and its negation
        // in that case, the formula is always satisfiable
        if (tree.getType().equals("OR")) {
            boolean containsVarAndNegVar = false;
            for (int i = 0; i < tree.getChildren().size() - 1; i++) {
                containsVarAndNegVar = false;
                for (int j = i + 1; j < tree.getChildren().size(); j++) {
                    if (tree.getChildren().get(i).getType().equals("VAR")
                            && tree.getChildren().get(i).getNodeId() == -tree.getChildren().get(j).getNodeId()) {
                        containsVarAndNegVar = true;
                        break;
                    }
                }

            }
            if (containsVarAndNegVar) {
                myWriter.write("p cnf" + " " + nr_inputs + " " + 1 + "\n");
                myWriter.write("1 0\n");
                myScanner.close();
                myWriter.close();
                return;
            }
        }

        // combine AND in AND gates
        tree.combineAndInAndGates();

        // combine OR in OR gates
        tree.combineOrInOrGates();

        // convert the tree to CNF
        tree = tree.convertToCNF();

        // combine remaining OR in OR gates (if any)
        tree.combineOrInOrGates();

        // combine remaining AND in AND gates (if any)
        tree.combineAndInAndGates();

        // if the formula is empty, it is always satisfiable
        if (tree.getChildren().isEmpty()) {
            myWriter.write("p cnf" + " " + nr_inputs + " " + 1 + "\n");
            myWriter.write("1 0\n");
            myScanner.close();
            myWriter.close();
            return;
        }

        // get the CNF representation of the formula
        StringBuilder CNF = new StringBuilder();
        tree.getCNF(CNF);
        String CNFString = CNF.toString();

        CNFString = CNFString.replaceAll("\\(", "( ");
        String[] CNFStringInfo = CNFString.split(" ");

        // count the number of clauses
        tree.countClauses(CNFStringInfo);

        // print the tree in a readable CNF format
        myWriter.write("p cnf" + " " + nr_inputs + " " + tree.getNrClauses() + "\n");
        tree.printFormattedCNF(CNFStringInfo, myWriter);

        myScanner.close();
        myWriter.close();
    }
}
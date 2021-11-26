import java.util.ArrayList;

public class BayesBallAlgo {


    public static String Bayes(Network net, String question){
        // The first part is to take from the String all the data we need about the question.
        String[] question_split1 = question.split("-");
        String q1 = question_split1[0];
        int ind = question_split1[1].indexOf("|");
        String q2 = question_split1[1].substring(0, ind);
        Variable Q1 = net.getVariable(q1);
        Variable Q2 = net.getVariable(q2);
        if(question.contains("=")) {
            String[] question_split2 = question.split("\\|")[1].split(",");
            for (String s : question_split2) {
                String evidence = s.split("=")[0];
                net.getVariable(evidence).setEvidence();
            }
        }
        ArrayList<Variable> colored = new ArrayList<>();
        boolean independent = BayesBall(Q1, Q2, null, colored);
        net.resetEvidence();
        return (independent) ? "yes" : "no";
    }

    /**
     * This method checks whether two Variables in the Network are Conditionally Independent or not.
     * The rules of moving foreword in the Network (according to BayesBall Algorithm) is:
     * in a current node there are two things that determine where can we go, 1) where we came from(father or child),
     * 2) is the current node is evidence in the question or not.
     * According to this information we can determine where to go.
     * @param src - Variable src, where we start to check if route exists.
     * @param dest - Variable dest, where we want to reach.
     * @param came_from - Variable came_from, where we came from.
     * @param colored - ArrayList<Variable> of the Variables we've been that we can't visit again.
     * @return return TRUE if independent, dependent return FALSE.
     */
    private static boolean BayesBall(Variable src, Variable dest, Variable came_from, ArrayList<Variable> colored) {
        if (src.getVar_name().equals(dest.getVar_name())) {
            return false;
        }
        Variable var = null;
        if (src.isEvidence()) { // If source is given ->
            // If came from a child -> STUCK
            if (src.getChildren().contains(came_from)) {
                /* The source is the parent of the last Var we came from
                    and because its evidence we are stuck a.k.a. independent.
                 */
                return true;
            } else { // Came from a parent.
                for (int i = 0; i < src.getParents().size(); i++) {
                    var = src.getParents().get(i);
                    if (!colored.contains(var)) {
                        colored.add(src);
                        // Because we came from a prent we can go to parents only.
                        if (!BayesBall(var, dest, src, colored)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } else { // Source is not given.
            // Came from a child -> can go to children and parents.
            if (src.getChildren().contains(came_from) || came_from == null) {
                for (int i = 0; i < src.getParents().size(); i++) {
                    var = src.getParents().get(i);
                    if (!colored.contains(var)) {
                        colored.add(src);
                        // Check on all the parents.
                        if (!BayesBall(var, dest, src, colored)) {
                            return false;
                        }
                    }
                }
                for (int i = 0; i < src.getChildren().size(); i++) {
                    var = src.getChildren().get(i);
                    if (!colored.contains(var)) {
                        colored.add(src);
                        // Check on all the children.
                        if (!BayesBall(var, dest, src, colored)) {
                            return false;
                        }
                    }
                }
            } else { // Came from a parent.
                // came from a parent -> can go to all the children.
                for (int i = 0; i < src.getChildren().size(); i++) {
                    var = src.getChildren().get(i);
                    if (!colored.contains(var)) {
                        // Check on all the children.
                        if (!BayesBall(var, dest, src, colored)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }


}

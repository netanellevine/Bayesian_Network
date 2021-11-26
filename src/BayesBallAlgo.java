import java.util.ArrayList;

public class BayesBallAlgo {



    // independent return TRUE dependent return FALSE
    public static String Bayes(Network net, String question){
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
//        colored.add(null);
        boolean independent = BayesBall(Q1, Q2, null, colored);
        net.resetEvidence();
        return (independent) ? "yes" : "no";
    }


//     independent return TRUE, dependent return FALSE
    private static boolean BayesBall(Variable src, Variable dest, Variable came_from, ArrayList<Variable> colored) {

//        if (came_from != null) {
//            System.out.println("src is: " + src.getVar_name() + ", dest is: " + dest.getVar_name() + ", came from is: " + came_from.getVar_name());
//        }
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
//            // If came from a parent -> can go to all the children.
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
                return true;
            } else { // Came from a child -> can go to children and parents.
                // If came from a parent -> can go to all the children.
                for (int i = 0; i < src.getChildren().size(); i++) {
                    var = src.getChildren().get(i);
                    if (!colored.contains(var)) {
                        // Check on all the children.
                        if (!BayesBall(var, dest, src, colored)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
    }
}

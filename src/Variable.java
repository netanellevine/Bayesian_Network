import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Variable {
    // Fields:

    // ArrayList from type of Variables that keeps the parents of this Variable.
    private final ArrayList<Variable> parents;

    // ArrayList from type of Variables that keeps the children of this Variable.
    private final ArrayList<Variable> children;

    // String of the Variable name.
    private final String Var_name;

    // Array from type of String that keeps all the outcomes of this Variable (T/F, v1/v2/v3...).
    private final String[] outcomes;

    // HashMap that the key is a String and the value is a double.
    // CPT = Conditional Probability Table, this HashMap keeps all the probabilities
    // of this Variables given his parents (if there are parents).
    private final HashMap<String, Double> CPT;

    // Boolean which used only in the BayesBall Algorithm,
    // while running the BayesBall Algorithm @evidence is true only if this
    // Variable is an evidence in the question.
    private boolean evidence = false;


    // Constructor.
    public Variable(String var_name, String[] outcomes) {
        Var_name = var_name;
        parents = new ArrayList<>();
        children = new ArrayList<>();
        this.outcomes = outcomes;
        CPT = new HashMap<>();
    }

    /**
     *
     * @return - String of the Variable name.
     */
    public String getVar_name() {
        return this.Var_name;
    }

    /**
     *
     * @return - ArrayList from type of Variables with all the parents of this Variable.
     */
    public ArrayList<Variable> getParents() {
        return this.parents;
    }

    /**
     *
     * @return - ArrayList from type of Variables with all the children of this Variable.
     */
    public ArrayList<Variable> getChildren() {
        return this.children;
    }

    /**
     *
     * @return - Array from type of String with all the outcomes of this Variable.
     */
    public String[] getOutcomes() {
        return this.outcomes;
    }

    /**
     *
     * @return - HashMap that the key is a String and the value is a double.
     */
    public HashMap<String, Double> getCPT() {
        return this.CPT;
    }

    /**
     *
     * @return - true if this Variable is an evidence(given) in the BayesBall question.
     */
    public boolean isEvidence() {
        return this.evidence;
    }

    /**
     * This method resets this Variable field evidence to be false.
     * used at the end of the question.
     */
    public void resetEvidence() {
        this.evidence = false;
    }

    /**
     * In the BayesBall Algorithm if this Variable is given set @evidence to be true.
     */
    public void setEvidence(){ this.evidence = true;}

    /**
     *
     * @param parent - Variable of a parent of this Variable.
     */
    public void add_parent(Variable parent){this.parents.add(parent);}

    /**
     *
     * @param child - Variable of a child of this Variable.
     */
    public void add_children(Variable child){this.children.add(child);}

    /**
     *
     * @return - true if this Variable has parents, false otherwise.
     */
    public boolean hasParent(){
        return parents.size() > 0;
    }

    /**
     *
     * @return - String that keeps all this Variable data: Var_name, Outcomes, Parents, Children, CPT.
     */
    @Override
    public String toString(){
        String result = "\nVar_name- " + this.Var_name + "\nOutcomes- ";
        result += Arrays.toString(this.outcomes) + "\nParents- [";
        for(Variable parent: this.parents){
            result += parent.getVar_name() + ",";
        }
        result = this.parents.size() > 0 ? result.substring(0, result.length() -1) + "]," : result + "],";
        result += "\nChildren- [";
        for(Variable child: this.children){
            result += child.getVar_name() + ",";
        }
        result = this.children.size() > 0 ? result.substring(0, result.length() -1) + "]," : result + "],";
        result +=  "\nCPT- " + CPT;
        return result + "\n";
    }



}


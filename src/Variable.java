import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Variable {
    private final ArrayList<Variable> parents;
    private final ArrayList<Variable> children;
    private final String Var_name;
    private final String[] outcomes;
    private final HashMap<String, Double> CPT;
    private boolean evidence = false;


    public Variable(String var_name, String[] outcomes) {
        Var_name = var_name;
        parents = new ArrayList<>();
        children = new ArrayList<>();
        this.outcomes = outcomes;
        CPT = new HashMap<>();
    }


    public String getVar_name() {
        return this.Var_name;
    }

    public ArrayList<Variable> getParents() {
        return this.parents;
    }

    public ArrayList<Variable> getChildren() {
        return this.children;
    }

    public String[] getOutcomes() {
        return this.outcomes;
    }

    public HashMap<String, Double> getCPT() {
        return this.CPT;
    }

    public boolean isEvidence() {
        return this.evidence;
    }

    public void resetEvidence() {
        this.evidence = false;
    }

    public void setEvidence(){ this.evidence = true;}

    public void add_parent(Variable parent){this.parents.add(parent);}

    public void add_children(Variable child){this.children.add(child);}

    public boolean hasParent(){
        return parents.size() > 0;
    }


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


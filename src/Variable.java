import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Variable {
    private ArrayList<Variable> parents;
    private ArrayList<Variable> children;
    private String Var_name;
    private String[] outcomes;
    private HashMap<String, Double> CPT;
    private int var_ind = 0;
    private boolean evidence = false;
    private boolean colored = false;


    public Variable(String var_name, String[] outcomes) {
        Var_name = var_name;
        parents = new ArrayList<>();
        children = new ArrayList<>();
        this.outcomes = outcomes;
        CPT = new HashMap<>();
        this.var_ind++;
    }

    private Variable(ArrayList<Variable> p, ArrayList<Variable> c, String var_name, String[] o, HashMap<String, Double> CPT, boolean e) {
        this.parents = p;
        this.children = c;
        this.Var_name = var_name;
        this.outcomes = o;
        this.CPT = CPT;
        this.evidence = e;
    }

    public String getVar_name() {
        return this.Var_name;
    }

    public ArrayList<Variable> getParents() {
        return this.parents;
    }

    public void setParents(ArrayList<Variable> parents) {
        this.parents = parents;
    }

    public ArrayList<Variable> getChildren() {
        return this.children;
    }

    public void setChildren(ArrayList<Variable> children) {
        this.children = children;
    }

    public void setVar_name(String var_name) {
        this.Var_name = var_name;
    }

    public String[] getOutcomes() {
        return this.outcomes;
    }

    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    public HashMap<String, Double> getCPT() {
        return this.CPT;
    }

    public void setCPT(HashMap<String, Double> CPT) {
        this.CPT = CPT;
    }

    public int getVar_ind() {
        return this.var_ind;
    }

    public void setVar_ind(int var_ind) {
        this.var_ind = var_ind;
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

    public boolean isColored() {
        return this.colored;
    }

    public void setColored() {
        this.colored = true;
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
        result +=  "\nCPT- " + CPT.toString();
        return result + "\n";
    }



}


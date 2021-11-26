import java.util.ArrayList;

public class Network {
    private ArrayList<Variable> net;

    public Network() {
        this.net = new ArrayList<>();
    }

    public Network(ArrayList<Variable> net) {
        this.net = net;
    }

    public void add_variable(Variable var){
        this.net.add(var);
    }

    public boolean isVariable(String var_name){
        for (Variable variable : this.net) {
            if (variable.getVar_name().equals(var_name)) {
                return true;
            }
        }
        return false;
    }

    public Variable getVariable(String var_name){
        if(isVariable(var_name)) {
            for (Variable variable : this.net) {
                if (variable.getVar_name().equals(var_name)) {
                    return variable;
                }
            }
        }
            String[] a = {};
           return null;
    }

    public void resetEvidence(){
        for (Variable variable : this.net) {
            variable.resetEvidence();
        }
    }

    public ArrayList<Variable> getNet() {
        return this.net;
    }

    public void removeVariable(Variable var){
        this.net.remove(var);
    }

    @Override
    public String toString(){
        String out = "";
        for (Variable v: net) {
            out += v.toString() + "\n";
        }
        return out;
    }
}

import java.util.ArrayList;

public class Network {
    // Field.
    private final ArrayList<Variable> net;

    // Constructor.
    public Network() {
        this.net = new ArrayList<>();
    }

    /**
     * This method adds a new Variable to the Network.
     * @param var - Variable var.
     */
    public void add_variable(Variable var){
        this.net.add(var);
    }

    /**
     * This method checks whether var_name given is belongs to the Network as Variable.
     * @param var_name - String var_name.
     * @return - true if the Network contains, false otherwise.
     */
    public boolean isVariable(String var_name){
        for (Variable variable : this.net) {
            if (variable.getVar_name().equals(var_name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method receives a String var_name and returns a Variable from this Network with the name of var_name.
     * @param var_name - String var_name.
     * @return - Variable from the Network, returns null can't find the variable.
     */
    public Variable getVariable(String var_name){
        if(isVariable(var_name)) {
            for (Variable variable : this.net) {
                if (variable.getVar_name().equals(var_name)) {
                    return variable;
                }
            }
        }
           return null;
    }

    /**
     * This method resets all the Evidence of each Variable in the Network.
     * It uses the method resetEvidence() from the Variable class.
     */
    public void resetEvidence(){
        for (Variable variable : this.net) {
            variable.resetEvidence();
        }
    }

    /**
     * This method returns the Network.
     * @return - Network net.
     */
    public ArrayList<Variable> getNet() {
        return this.net;
    }

    /**
     * This method removes a Variable from the Network.
     * @param var - Variable var.
     */
    public void removeVariable(Variable var){
        this.net.remove(var);
    }

    /**
     * This method Override Object.toString()
     * @return - String of the Network.
     */
    @Override
    public String toString(){
        String out = "";
        for (Variable v: net) {
            out += v.toString() + "\n";
        }
        return out;
    }
}

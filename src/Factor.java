import java.util.HashMap;

public class Factor {
    // Fields:

    // String represents the Factor name, format is: "f(v1,v2...vn)i"
    // v1,v2...vn -> Variables in this Factor.
    // i -> the index of this Factor.
    private String name;

    // HashMap that the key is a String and the value is a double,
    // at the first initiate the HashMap is identical to the CPT of the Variable.
    // Throughout the Variable Elimination Algorithm the table can be
    // joined/eliminate with other factors and modified into a new table.
    private HashMap<String, Double> table;

    // Size of this Factor HashMap.
    private int size;

    // Index of this Factor.
    private String index;

    // Constructor.
    public Factor(HashMap<String, Double> CPT, int ind) {
        this.table = CPT;
        this.index = Integer.toString(ind);
        String temp = CPT.keySet().toString().split("\\),")[0];
        this.size = CPT.size();
        this.name = "";
        int i = 3;
        while(temp.length() > 0){
            String temp2 = "";
            if (!temp.contains("(")){
                i = 0;
            }
            temp2 = temp.substring(i, temp.indexOf('='));
            this.name = this.name + "," + temp2 + "";
            if(temp.indexOf('=') + 1 == temp.length()){
                break;
            }
            if (temp.contains("|")) {
                temp = temp.substring(temp.indexOf('|') + 1);
            }
            else if (temp.contains(",")){
                temp = temp.substring(temp.indexOf(',') + 1);
            }
            else{
                break;
            }
//            i++;
        }
        this.name = this.name.substring(1);
    }

    /**
     * This method remove a line from the Factor CPT.
     * @param key - String represents the key of the value that needs to be deleted.
     * @param s - String represents the name of the variable-outcome that needs to be deleted.
     */
    public void removeValue(String key, String s){
        this.table.remove(key);
        String[] name = this.name.split(",");
        String newName = "";
        for(String n: name){
            if(!n.equals(s)) {
                newName += "," + n;
            }
        }
        if(newName.length() > 1) {
            this.name = newName.substring(1);
        }
        this.size = this.table.size();
    }


    public String getName(){
        return "f(" + this.name + ")" + this.index;
    }

    public String getCleanName(){
        return this.name;
    }




    public String toString(){
        String output = "Name: " + getName() + "\n";
        output += "CPT: " + this.table.toString() + "\n";
        output += "CPT size: " + this.size + "\n";
        return output;
    }


    public HashMap<String, Double> getTable() {
        return this.table;
    }


    public int getSize() {
        return this.size;
    }


    public String getIndex() {
        return this.index;
    }

}

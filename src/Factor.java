import java.util.HashMap;

public class Factor {
    public String name;
    public HashMap<String, Double> factor;
    public int size;
    String index;


    public Factor(HashMap<String, Double> CPT, int ind) {
        this.factor = CPT;
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


    public void removeValue(String key, String s){
        this.factor.remove(key);
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
        this.size = this.factor.size();
    }


    public String getName(){
        return "f(" + this.name + ")" + this.index;
    }



    public String toString(){
        String output = "Name: " + getName() + "\n";
        output += "CPT: " + this.factor.toString() + "\n";
        output += "CPT size: " + this.size + "\n";
        return output;
    }
}

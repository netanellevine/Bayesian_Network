import java.util.*;

public class VariableElimination {
    private Variable query;
    private String queryOutcome;
    private ArrayList<Variable> evidence;
    private String copyOfEvidence;
    private ArrayList<Factor> factors;
    private ArrayList<String> hiddenByOrder;
    private final Network network;
    private int multiplyActions = 0;
    private int addActions = 0;
    private String answer;
    private String question;



    public VariableElimination(String question, Network n) {
        this.network = n;
        this.evidence = new ArrayList<>();
        this.hiddenByOrder = new ArrayList<>();
        double answerIsKnown = ifAnswerIsKnown(question);
        if (answerIsKnown == -1) {
            parseToQuestion(question);
            this.factors = new ArrayList<>();
            int ind = 1;
            this.question = question;
            copyEvidence(question);
            removeIrrelevant(question);
            for (Variable v : this.network.getNet()) {
                Factor f = new Factor(v.getCPT(), ind++);
                this.factors.add(f);
            }
            this.answer = VariableEliminationAlgo();
        } else {
            this.answer = String.valueOf((double)Math.round(answerIsKnown * 100000d) / 100000d);
        }
//        System.out.println(this.answer);
    }

    /**
     * This method checks if the question given is already known in the network
     * it iterates through all the variables and if it finds a key that is identical to the question
     * it returns the variable name who belongs the value.
     * @param q - String of question.
     * @return - returns String of variable name if found a match "NO!" otherwise.
     */
    private double ifAnswerIsKnown(String q) {
        String split = q.split(" ")[0];
        if(split.indexOf("|") == split.length() - 2){
            split = split.replace("|", "");
        }
        for (Variable v : this.network.getNet()) {
            if (v.getCPT().containsKey(split)) {
                return v.getCPT().get(split);
            }
        }
        return -1;
    }



    /**
     * This method receives a String q represent the question we've been asked about.
     * Parsing this String into all the data we need to know about this question and add the relevant data
     * to the class field which this data belongs to.
     * @param q - String of question,
     *          the format of the String is "P(Q1=O1...Qn=On|E1=o1,...,En=on) Hv1-Hv2-...Hvn".
     *          Q1...Qn -> the queries.
     *          O1...On -> the appropriate outcome for each of the queries.
     *          E1...En -> the evidence given.
     *          o1...on -> the appropriate outcome for each of the evidence.
     *          Hv1...Hvn -> the hidden variable by the order of the elimination.
     *          E.g. "P(B=T|J=T,M=T) A-E".
     */
    private void parseToQuestion(String q){
        // extract query from the string.
        int ind1 = q.indexOf("(");
        int ind2 = q.indexOf("=");
        String query_name = q.substring(ind1 + 1, ind2);
        this.query = network.getVariable(query_name);

        // extract query outcome from the string.
        ind1 = q.indexOf("|");
        this.queryOutcome = q.substring(ind2 + 1, ind1);

        // extract all the evidence from the string.
//        copyEvidence(q);
        q = q.substring(ind1 + 1);
        String evidence_name;
        while(q.indexOf(" ") != 0){
            ind1 = q.indexOf("=");
            evidence_name = q.substring(0, ind1);
            Variable e = this.network.getVariable(evidence_name);
            e.setEvidence();
            this.evidence.add(e);
            ind2 = q.contains(",") ? q.indexOf(",") : q.indexOf(")");
            q = q.substring(ind2 + 1);
        }

        // extract elimination order from the string.
        this.hiddenByOrder.addAll(List.of(q.replace(" ", "").split("-")));
    }



    private void copyEvidence(String q){
        int start = q.indexOf("|");
        int end = q.indexOf(")");
        this.copyOfEvidence = q.substring(start, end);
    }



    private void removeIfOneValued(){
        for(int i = 0; i < this.factors.size(); i++){
            if(this.factors.get(i).size == 1){
                this.factors.remove(i);
                i--;
            }
        }
    }


    /**
     * This method goes over all the factors, and reduce them by deleting all the irrelevant lines (values).
     * Irrelevant lines are lines that one or more of the @evidence of this class outcomes are not as given in the Query.
     * Therefore, because we have an outcome that we know already. It's unnecessary to keep all the values of the other outcomes
     * because we know that they can't happen.
     */
    private void removeIrrelevantLines() {
        String[] evidence = this.copyOfEvidence.substring(1).split(",");
        String name;
        if (evidence.length > 1) {
            for (int i = 0; i < this.factors.size(); i++) {
                for(String e: evidence){
                    name = e.substring(0, e.indexOf("="));
                    Factor f = this.factors.get(i);
                    if(f.name.contains(name)){
                        String[] keys = f.factor.keySet().toArray(new String[0]);
                        for(String key: keys){
                            if(!key.contains(e)){
                                f.removeValue(key, name);
                                if(f.size == 1){
                                    this.factors.remove(f);
                                    i--;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            name = evidence[0].substring(0, evidence[0].indexOf("="));
            for (int i = 0; i < this.factors.size(); i++) {
                Factor f = this.factors.get(i);
                if(f.name.contains(name)){
                    String[] keys = f.factor.keySet().toArray(new String[0]);
                    for(String key: keys){
                        if(!key.contains(evidence[0])){
                            f.removeValue(key, name);
                            if(f.size == 1){
                                this.factors.remove(f);
                                i--;
                                break;
                            }
                        }
                    }
                }

            }
        }
    }



    private String VariableEliminationAlgo(){
        removeIrrelevantLines();
        while(!this.hiddenByOrder.isEmpty()){
            String h = this.hiddenByOrder.get(0) + "";
            int hidden_outcomes = this.network.getVariable(h).getOutcomes().length;
//            this.hiddenByOrder.remove(h);
            ArrayList<Factor> factors_to_join = new ArrayList<>();
            for(Factor f: this.factors){
                if(f.getName().contains(h)){
                    factors_to_join.add(f);
                }
            }
            while(factors_to_join.size() > 1){
                sortFactorsByOrder(factors_to_join);
                factors_to_join.add(Join(factors_to_join.get(0), factors_to_join.get(1)));
                factors_to_join.remove(0);
                factors_to_join.remove(0);
            }
            if(factors_to_join.get(0).size / hidden_outcomes == 1){
                this.factors.remove(factors_to_join.get(0));
            }
            else {
                Eliminate(factors_to_join.get(0), h);
                removeIfOneValued();
            }
            this.hiddenByOrder.remove(h);
        }
        while(this.factors.size() > 1){
            Join(this.factors.get(0), this.factors.get(1));
        }
        double sum_of_outcomes = 0;
        Factor f = this.factors.get(0);
        String[] f_keys = f.factor.keySet().toArray(new String[0]);
        String key = "";
        for(String k: f_keys){
            sum_of_outcomes += f.factor.get(k);
            if(k.contains(this.query.getVar_name() + "=" + this.queryOutcome)){
                key = k;
            }
        }
        this.addActions += f_keys.length - 1;
        double value = f.factor.get(key) / sum_of_outcomes;
        return String.valueOf((double)Math.round(value * 100000d) / 100000d);
    }


    /**
     * This method checks whether a given Variable @ancestor is ancestor of a given Variable @var.
     * @param var1 - the Variable to check if he is successor of @ancestor.
     * @param ancestor - the Variable to check if he is ancestor of @var1.
     * @return - True if @ancestor is ancestor of @var1 , False otherwise.
     */
    private static boolean isAncestor(Variable var1, Variable ancestor){
        if(ancestor == null || ancestor.getChildren().isEmpty()){
            return false;
        }
        if(ancestor.getChildren().contains(var1)){
            return true;
        }
        ArrayList<String> inheritances = new ArrayList<>();
        for(Variable child : ancestor.getChildren()){
            if(child.getChildren().contains(var1)){
                return true;
            }
            inheritances.add(child.getVar_name());
            ArrayList<Variable> children = new ArrayList<>();
            children.add(child);
            while(!children.isEmpty()) {
                if(children.get(0).getChildren().contains(var1)){
                    return true;
                }
                for (int i = 0; i < children.get(0).getChildren().size(); i++) {
                    Variable var = children.get(0).getChildren().get(i);
                    children.add(var);
                    inheritances.add(var.getVar_name());
                }
                children.remove(0);
            }
        }
        return inheritances.contains(var1.getVar_name());
    }


    private void removeIrrelevant(String question){
        for (int i = 0; i < this.hiddenByOrder.size(); i++) {
            String hidden = this.hiddenByOrder.get(i);
            if(this.network.getVariable(hidden) == null){
                this.hiddenByOrder.remove(hidden);
                i--;
                continue;
            }
            String q = this.query.getVar_name() + "-" + hidden + this.copyOfEvidence;
            boolean ancestor = false;
            if(BayesBallAlgo.Bayes(this.network, q).equals("yes")){
                deleteChildren(this.network.getVariable(hidden));
                this.network.removeVariable(this.network.getVariable(hidden));
                this.hiddenByOrder.remove(hidden);
                i--;
            }
            else{
                if(isAncestor(this.query, this.network.getVariable(hidden))){
                    ancestor = true;
                }
                else {
                    for(Variable evi : this.evidence) {
                        if (isAncestor(evi, this.network.getVariable(hidden))) {
                            ancestor = true;
                            break;
                        }
                    }
                }
                if(!ancestor) {
                    deleteChildren(this.network.getVariable(hidden));
                    this.network.removeVariable(this.network.getVariable(hidden));
                    this.hiddenByOrder.remove(hidden);
                    i--;
                }
            }
        }
    }

    private void deleteChildren(Variable hidden) {
        if(hidden != null) {
            if (hidden.getChildren().size() > 0) {
                boolean deleteEvidence = false;
                String deleted_evi = "";
                for (int i = 0; i < hidden.getChildren().size(); i++) {
                    deleteEvidence = this.evidence.contains(hidden.getChildren().get(i));
                    if(deleteEvidence){
                        deleted_evi = hidden.getChildren().get(i).getVar_name();
                        int start = this.question.indexOf("|");
                        int end = this.question.indexOf(")");
                        this.copyOfEvidence = "";
                        String[] evi = this.question.substring(start + 1, end).split(",");
                        for(int j = 0; j < evi.length; j++){
                            if(!evi[j].contains(deleted_evi)){
                                this.copyOfEvidence +=  "," + evi[j];
                            }
                        }
                        this.copyOfEvidence = "|" + this.copyOfEvidence.substring(1);
                        this.evidence.remove(hidden.getChildren().get(i));
                    }
                    this.network.removeVariable(hidden.getChildren().get(i));
                }
            }
        }
    }


    /**
     * This method takes 2 factors and joining them into one.
     * the Join process is similar to a Cartesian Multiplication,
     * it multiplies only those who have the same outcome for one or more values.
     * The Join method can increase the size of the table up to: original_size * sum_of_outcomes (of all the variables that are not in the Intersection between the two factors).
     * @param f1 - Factor number 1.
     * @param f2 - Factor number 2.
     * @return new_factor - Factor that combines the CPT of both of the f1 and f2.
     */
    private Factor Join(Factor f1, Factor f2){
        String[] f1_keys = f1.factor.keySet().toArray(new String[0]);
        String[] f2_keys = f2.factor.keySet().toArray(new String[0]);
        String[] vars2Join = whichVariableToJoin(f1, f2);
        HashMap<String, Double> new_factor = new HashMap<>();
        for (int i = 0; i < vars2Join.length; i++) {
            for (String f1_key : f1_keys) {
                for (String f2_key : f2_keys) {
//                String[] temp1 = f1_key.substring(2).split("[|,)]+");
//                String[] temp2 = f2_key.substring(2).split("[|,)]+");
                    String[] t = vars2Join[i].split(",");
                    int counter = 0;
                    for(int j = 1; j < t.length; j++){
                        if(f1_key.contains(t[j]) && f2_key.contains(t[j])){
                            counter++;
                        }
                        else{
                            break;
                        }
                    }
                    if(counter == t.length -1){
                        double v1 = f1.factor.get(f1_key);
                        double v2 = f2.factor.get(f2_key);
                        double val = v1 * v2;
                        this.multiplyActions++;
                        String key = generateNewKey(f1_key, f2_key);
                        new_factor.put(key, val);
                    }
                }
//                for (String t1 : temp1) {
//                    for (String t2 : temp2) {
//                        if (t1.equals(t2)) {
//                            double v1 = f1.factor.get(f1_key);
//                            double v2 = f2.factor.get(f2_key);
//                            double val = v1 * v2;
//                            this.multiplyActions++;
//                            String key = generateNewKey(f1_key, f2_key);
//                            new_factor.put(key, val);
//                        }
//                    }
//                }
            }
        }
        int ind = Integer.parseInt(this.factors.get(this.factors.size() - 1).index) + 1;
        this.factors.remove(f1);
        this.factors.remove(f2);
        Factor new_f = new Factor(new_factor, ind);
        this.factors.add(new_f);
        return new_f;
    }


    private String[] whichVariableToJoin(Factor f1, Factor f2){
        ArrayList<String> vars2Join = new ArrayList<>();
        String[] f1_vars = f1.name.split(",");
        String[] f2_vars = f2.name.split(",");
        for(int i = 0; i < f1_vars.length; i++){
            for(int j = 0; j < f2_vars.length; j++){
                if(!f1_vars[i].equals("") && !f2_vars[j].equals("")) {
                    if (f1_vars[i].equals(f2_vars[j])) {
//                        String[] r_outcomes = this.network.getVariable(f1_vars[i]).getOutcomes();
//                        for(int k = 0; k < r_outcomes.length; k++){
//                            String temp = f1_vars[i] + "=" + r_outcomes[k];
//                            vars2Join.add(temp);
//                        }
                        vars2Join.add(f1_vars[i]);
                        f1_vars[i] = "";
                        f2_vars[j] = "";
                    }
                }
            }
        }
        ArrayList<String[]> allOutcomes = new ArrayList<>();
        for (String s : vars2Join) {
            String[] v_outcomes = this.network.getVariable(s).getOutcomes();
            allOutcomes.add(v_outcomes);
        }
        int sumOutcomes = 1;
        for(String[] r: allOutcomes){
            sumOutcomes *= r.length;
        }
        String[] ans = new String[sumOutcomes];
        for(int i = 0; i < sumOutcomes; i++){
            ans[i] = "";
        }
        int jump = sumOutcomes;
        for(int i = 0; i < vars2Join.size(); i++) {
            String[] outcomes = allOutcomes.get(i);
            jump = jump / vars2Join.get(i).length();
            int k = 0, j = 0, counter = 0;
            while(k < sumOutcomes) {
                if (counter < jump) {
                    String temp = vars2Join.get(i) + "=" + outcomes[j];
                    ans[k] += "," + temp;
                    k++;
                    counter++;
                } else {
                    j++;
                    j = j % outcomes.length;
                    counter = 0;
                }
            }
        }


        return ans;
    }


    /**
     * This method eliminate the hidden variable from the CPT of a given factor.
     * The purpose of this method is to reduce the size of the CPT which will save run time in the next calculations.
     * @param f - Factor represents a CPT of an event.
     * @param hidden_name - String represents the name of the value to eliminate from the CPT of the Factor f.
     * therefore the size of the CPT is now: (size_of_original_CPT / amount_of_outcomes_of_hidden_var).
     */
    private void Eliminate(Factor f, String hidden_name){
        HashMap<String, Double> new_CPT = new HashMap<>();
        String[] keys = f.factor.keySet().toArray(new String[0]);
        ArrayList<String> values_to_sum = new ArrayList<>();
        for(int i = 0; i < keys.length; i++){
            values_to_sum.add(keys[i]);
            String[] t1 = keys[i].substring(2, keys[i].length() - 1).split(",");
            String add_by = "";
            for(String t: t1){
                if(!t.contains(hidden_name)){
                    add_by += "," + t;
                }
            }
            if(add_by.length() > 1) {
                add_by = add_by.substring(1);
            }
            String key = "P(" + add_by + ")";
            for(int j = 0; j < keys.length; j++){
                if(i != j) {
                    if (keys[j].contains(add_by) && !values_to_sum.contains(keys[j])) {
                        values_to_sum.add(keys[j]);
                    }
                }
            }
            double value = 0;
            for(String val: values_to_sum){
                value += f.factor.get(val);
            }
            if(!new_CPT.containsKey(key)) {
                this.addActions += values_to_sum.size() - 1;
            }
            new_CPT.put(key, value);
            values_to_sum.clear();
        }
        int ind = Integer.parseInt(this.factors.get(this.factors.size() - 1).index) + 1;
        this.factors.remove(f);
        Factor new_factor = new Factor(new_CPT, ind);
        this.factors.add(new_factor);
    }


    /**
     * This method purpose is to generate a new key for the CPT of a new factor.
     * This method is used in the @Join method to create a new_key of a two given factors.
     * @param k1 - the key of the first factor.
     * @param k2 - the key of the second factor.
     * @return String new_key represents the new key.
     */
    private String generateNewKey(String k1, String k2){
        String[] temp1 = k1.substring(2).split("[|,)]+");
        String[] temp2 = k2.substring(2).split("[|,)]+");
        String[] s =  this.copyOfEvidence.substring(1).split(",");
        ArrayList<String> evidence = new ArrayList<>();
        Collections.addAll(evidence, s);
        String new_key = "";
        for (String t1 : temp1) {
            for (String t2 : temp2) {
                if (!new_key.contains(t1) && !evidence.contains(t1)) {
                    new_key += "," + t1;
                }
                if (!new_key.contains(t2) && !evidence.contains(t2)) {
                    new_key += "," + t2;
                }
            }
        }
        new_key = "P(" + new_key.substring(1) + ")";
        return new_key;
    }


    /** This method receives a String of a factor_name and returns
     * the identical Factor itself.
     * @param factor_name - String of a factor name.
     * @return Factor curr_factor - the factor of the given name factor_name.
     * In case of factor_name wasn't found in the factors list @return null.
     */
    private Factor getFactor(String factor_name){
        for(Factor curr_factor : this.factors){
            if(curr_factor.getName().equals(factor_name)){
                return curr_factor;
            }
        }
        return null;
    }


    private void sortFactorsByOrder(ArrayList<Factor> factors){
        factors.sort((o1, o2) -> {
            Integer s1 = o1.size;
            Integer s2 = o2.size;
            int sComp = s1.compareTo(s2);
            if (sComp != 0) {
                return sComp;
            }
            Integer str1 = SumByAscii(o1.name);
            Integer str2 = SumByAscii(o2.name);
            return str1.compareTo(str2);
        });
    }


    private int SumByAscii(String str) {
        str = str.replaceAll("[f()1-9]+", "");
        String[] arr = str.split(",");
        int sum = 0;
        for (String s : arr) {
            for (int j = 0; j < s.length(); j++) {
                int asciiValue = s.charAt(j);
                sum += asciiValue;
            }
        }
        return sum;
    }


    public String getAnswer(){
        return this.answer;
    }


    public int getMultiplyActions() {
        return this.multiplyActions;
    }


    public int getAddActions() {
        return this.addActions;
    }

}

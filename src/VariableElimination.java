import java.util.*;

public class VariableElimination {
    // Fields:

    // Variable that the question is being asked about.
    private Variable query;

    // The outcome that we need to calculate for the question.
    private String query_outcome;

    // List with all the given Variables in the question.
    private final ArrayList<Variable> evidence;

    // List with all the Factors from the Network that are relevant for this question.
    private ArrayList<Factor> factors;

    // List of String with all the names of the hidden Variable (sorted by the elimination order),
    // that needs to be eliminated during the Algorithm.
    private final ArrayList<String> hidden_by_order;

    // The Bayesian Network with all the data.
    private final Network network;

    // The amount of multiply actions were made during the Algorithm.
    private int multiply_actions = 0;

    // The amount of add actions were made during the Algorithm.
    private int add_actions = 0;

    // The answer to the question in String, rounded to 5 digits after the point.
    private final String answer;

    // The question asked, the format is:
    // P(q1=T|e1=T,e2=T) h1-h2 , q1 -> is the query , e1,e2 -> are the evidence, h1,h2 -> are the hidden.
    // for example: P(B=T|J=T,M=T) A-E.
    private String question;

    // String that copies the evidence just like the format of the question,
    // used only to narrow down the amount of calculations by deleting irrelevant Variables.
    private String copyOfEvidence;



    // Constructor.
    public VariableElimination(String question, Network n) {
        this.network = n;
        this.evidence = new ArrayList<>();
        this.hidden_by_order = new ArrayList<>();
        double answerIsKnown = ifAnswerIsKnown(question);
        if (answerIsKnown == -1) {
            parseToQuestion(question);
            this.factors = new ArrayList<>();
            int ind = 1;
            this.question = question;
            copyEvidence(question);
            removeIrrelevant();
            for (Variable v : this.network.getNet()) {
                Factor f = new Factor(v.getCPT(), ind++);
                this.factors.add(f);
            }
            this.answer = String.valueOf((double)Math.round(VariableEliminationAlgo() * 100000d) / 100000d);
        } else {
            this.answer = String.valueOf((double)Math.round(answerIsKnown * 100000d) / 100000d);
        }
    }

    /**
     * This method checks if the question given is already known in the network,
     * it iterates through all the variables and if it finds a key that is identical to the question
     * it returns its value.
     * <p><b>This method is one of the ways to try reducing the runtime of this Algorithm</b>
     * @param q - String of question.
     * @return - returns the value if found, otherwise returns -1.
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
     * This method receives a String @q represents the question we've been asked about.
     * Parsing this String into all the data we need to know about this question and add the relevant data
     * to the class fields whom this data belongs to.
     * @param q - String of question,
     *          the format of the String is:<p> <b>"P(Q1=O1...Qn=On|E1=o1,...,En=on) Hv1-Hv2-...Hvn"</b>.<ul>
     *          <li> Q1...Qn -> the queries.
     *          <li> O1...On -> the appropriate outcome for each of the queries.
     *          <li> E1...En -> the evidence given.
     *          <li> o1...on -> the appropriate outcome for each of the evidence.
     *          <li> Hv1...Hvn -> the hidden variable by the order of the elimination.
     *          </ul>
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
        this.query_outcome = q.substring(ind2 + 1, ind1);

        // extract all the evidence from the string.
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
        String[] h = q.replace(" ", "").split("-");
        Collections.addAll(this.hidden_by_order, h);
    }


    /**
     * This method removes a Factor from the Factors list if the Factor table has only 1 value.
     * <p><b>This method is one of the ways to try reducing the runtime of this Algorithm</b>
     */
    private void removeIfOneValued(Factor f){
            if(f.getSize() == 1){
                this.factors.remove(f);
        }
    }


    /**
     * This method goes over all the factors, and reduce them by deleting all the irrelevant lines (values).
     * Irrelevant lines are lines that at least 1 outcome (of an evidence variable)
     * from the key is not the same as the outcome of the evidence outcome given.
     * Therefore, because we have an outcome that we know already, it's unnecessary to keep all the values of the other outcomes
     * because we know that they can't happen.
     * <p><b>This method is one of the ways to try reducing the runtime of this Algorithm</b>
     */
    private void removeIrrelevantLines() {
        String[] evidence = this.copyOfEvidence.substring(1).split(",");
        String name;
        if (evidence.length > 1) {
            for (int i = 0; i < this.factors.size(); i++) {
                for(String e: evidence){
                    name = e.substring(0, e.indexOf("="));
                    Factor f = this.factors.get(i);
                    if(f.getName().contains(name)){
                        String[] keys = f.getTable().keySet().toArray(new String[0]);
                        for(String key: keys){
                            if(!key.contains(e)){
                                f.removeValue(key, name);
                                if(f.getSize() == 1){
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
                if(f.getName().contains(name)){
                    String[] keys = f.getTable().keySet().toArray(new String[0]);
                    for(String key: keys){
                        if(!key.contains(evidence[0])){
                            f.removeValue(key, name);
                            if(f.getSize() == 1){
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


    /**
     * <h1>VariableEliminationAlgo() method</h1>
     * This method is the purpose of this class, based on a known Algorithm called Variable Elimination.<p>
     * <b>steps of this method:</b>
     * <p>1) Call method removeIrrelevantLines() to delete irrelevant lines.
     * <p>2) First loop iterates @hidden_by_order until its empty, at each iteration it eliminates one
     * hidden from @hidden_by_order.
     * <p>3) Inside the main loop, another loop that iterates all over the factors and adds
     * a Factor to @factors_to_join if it contains the hidden Variable.
     * <p>4) Join -> while @factors_to_join is bigger than 1 (that means we can continue to do Join).
     * <p>4.1) Call method sortFactorsByOrder(factors_to_join), to sort the list so that the Join
     * will always be on the 2 smallest Factors.
     * <p>4.2) Call method Join() with the first two factors on the list and add the factor returned
     * from Join() to @factors_to_join.
     * <p>4.3) Delete the first two factors from @factors_to_join. <b>repeat step 4</b>
     * <p>5) Eliminate -> call method Eliminate() with the remaining factor from @factors_to_join and the
     * String @h from @hidden_by_order.
     * <p>6) Call method removeIfOneValued() to check if the factor created from the Eliminate method
     * has a single line, if so delete it.
     * <p>7) Remove @h from @hidden_by_order list, <b>repeat step 2</b>.
     * <p>8) After finished with all the Join and Eliminate on the hidden variables, there may be more
     * than 1 factor remaining. Keep doing Join on the first two factors from @factors till there is only 1 left.
     * <p>9) In order to get the value we desire first needs to sum all the values in the remaining factor.
     * Second take the value of @query with the @query_outcome and normalize it with the @sum_of_values
     * (as in @value_ans / @sum_of_values).
     * <p>10) return @value_ans.
     * @return - The answer to @question as a double.
     * @see   <a href="https://en.wikipedia.org/wiki/Variable_elimination">Variable Elimination</a>
     */
    private double VariableEliminationAlgo(){
        removeIrrelevantLines();
        while(!this.hidden_by_order.isEmpty()){
            String h = this.hidden_by_order.get(0) + "";
            int hidden_outcomes = this.network.getVariable(h).getOutcomes().length;
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
            if(factors_to_join.get(0).getSize() / hidden_outcomes == 1){
                this.factors.remove(factors_to_join.get(0));
            }
            else {
                Eliminate(factors_to_join.get(0), h);
                removeIfOneValued(this.factors.get(this.factors.size() -1));
            }
            this.hidden_by_order.remove(h);
        }
        while(this.factors.size() > 1){
            sortFactorsByOrder(this.factors);
            Join(this.factors.get(0), this.factors.get(1));
        }
        double sum_of_values = 0;
        double value_ans = 0;
        Factor f = this.factors.get(0);
        String[] f_keys = f.getTable().keySet().toArray(new String[0]);
        for(String k: f_keys){
            sum_of_values += f.getTable().get(k);
            if(k.contains(this.query.getVar_name() + "=" + this.query_outcome)){
                value_ans = f.getTable().get(k);
            }
        }
        this.add_actions += f_keys.length - 1;
        value_ans = value_ans / sum_of_values;
        return value_ans;
    }


    /**
     * This method checks whether a given Variable @ancestor is ancestor of a given Variable @var.
     * @param var - the Variable to check if he is successor of @ancestor.
     * @param ancestor - the Variable to check if he is ancestor of @var1.
     * @return - True if @ancestor is ancestor of @var1 , False otherwise.
     */
    private static boolean isAncestor(Variable var, Variable ancestor){
        if(ancestor == null || ancestor.getChildren().isEmpty()){
            return false;
        }
        if(ancestor.getChildren().contains(var)){
            return true;
        }
        ArrayList<String> inheritances = new ArrayList<>();
        for(Variable c : ancestor.getChildren()){
            if(c.getChildren().contains(var)){
                return true;
            }
            inheritances.add(c.getVar_name());
            ArrayList<Variable> children = new ArrayList<>();
            children.add(c);
            while(!children.isEmpty()) {
                if(children.get(0).getChildren().contains(var)){
                    return true;
                }
                for (int i = 0; i < children.get(0).getChildren().size(); i++) {
                    Variable child = children.get(0).getChildren().get(i);
                    children.add(child);
                    inheritances.add(child.getVar_name());
                }
                children.remove(0);
            }
        }
        return inheritances.contains(var.getVar_name());
    }

    /**
     *This method checks if there are some Variables in the Network that are irrelevant for this question,
     * if so it deletes them from the Network.
     * <p>Irrelevant Variables are:
     * <p>1) Variables that are conditionally independent with @query Variable given the @evidence Variables.
     * <p>2) Variables that are not an ancestor of @query Variable or @evidence Variables.
     * <p>3) Variables that are children of one of the Variables that is not relevant for this question
     * <b>A.k.a. from steps 1 and 2 </b>.
     * <ul>
     * <li> In order to check conditionally independence we use BayesBallAlgo() method.
     * <li> In order to check if a Variable is not an ancestor we use isAncestor() method.
     * <li> In order to delete the children of the irrelevant variables we use deleteChildren() method.
     * </ul>
     * <b>This method is one of the ways to try reducing the runtime of this Algorithm</b>
     *
     */
    private void removeIrrelevant(){
        for (int i = 0; i < this.hidden_by_order.size(); i++) {
            String hidden = this.hidden_by_order.get(i);
            if(this.network.getVariable(hidden) == null){
                this.hidden_by_order.remove(hidden);
                i--;
                continue;
            }
            String q = this.query.getVar_name() + "-" + hidden + this.copyOfEvidence;
            boolean ancestor = false;
            if(BayesBallAlgo.Bayes(this.network, q).equals("yes")){
                deleteChildren(this.network.getVariable(hidden));
                this.network.removeVariable(this.network.getVariable(hidden));
                this.hidden_by_order.remove(hidden);
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
                    this.hidden_by_order.remove(hidden);
                    i--;
                }
            }
        }
    }

    /**
     * This method receives a Variable that need to be deleted from the Network and checks if it has children,
     * if so the method deletes them from this Network.
     * @param hidden - Variable that are going to be deleted.
     */
    private void deleteChildren(Variable hidden) {
        if(hidden != null) {
            if (hidden.getChildren().size() > 0) {
                boolean deleteEvidence;
                String deleted_evi;
                for (int i = 0; i < hidden.getChildren().size(); i++) {
                    deleteEvidence = this.evidence.contains(hidden.getChildren().get(i));
                    if(deleteEvidence){
                        deleted_evi = hidden.getChildren().get(i).getVar_name();
                        int start = this.question.indexOf("|");
                        int end = this.question.indexOf(")");
                        this.copyOfEvidence = "";
                        String[] evi = this.question.substring(start + 1, end).split(",");
                        for (String s : evi) {
                            if (!s.contains(deleted_evi)) {
                                this.copyOfEvidence += "," + s;
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
     * the Join process is similar to a Cartesian Multiplication.
     * It multiplies between the line with the same outcome for one or more values.
     * <p>The Join method can increase the size of the table up to: original_size * sum_of_outcomes
     * (of all the variables that are not in the Intersection between the two factors).
     * <p>At the end of the method it deletes @f1 and @f2 from @factors list and adds @new_factor to @factors list.
     * @param f1 - Factor number 1.
     * @param f2 - Factor number 2.
     * @return new_factor - Factor that combines the CPT of both of the f1 and f2.
     */
    private Factor Join(Factor f1, Factor f2){
        String[] f1_keys = f1.getTable().keySet().toArray(new String[0]);
        String[] f2_keys = f2.getTable().keySet().toArray(new String[0]);
        String[] vars2Join = whichVariableToJoin(f1, f2);
        HashMap<String, Double> new_factor = new HashMap<>();
        for (String var2Join : vars2Join) {
            for (String f1_key : f1_keys) {
                for (String f2_key : f2_keys) {
                    String[] v = var2Join.split(",");
                    int counter = 0;
                    for (int j = 1; j < v.length; j++) {
                        if (f1_key.contains(v[j]) && f2_key.contains(v[j])) {
                            counter++;
                        } else {
                            break;
                        }
                    }
                    if (counter == v.length - 1) {
                        double v1 = f1.getTable().get(f1_key);
                        double v2 = f2.getTable().get(f2_key);
                        double val = v1 * v2;
                        this.multiply_actions++;
                        String key = generateNewKey(f1_key, f2_key);
                        new_factor.put(key, val);
                    }
                }
            }
        }
        int ind = Integer.parseInt(this.factors.get(this.factors.size() - 1).getIndex()) + 1;
        this.factors.remove(f1);
        this.factors.remove(f2);
        Factor new_f = new Factor(new_factor, ind);
        this.factors.add(new_f);
        return new_f;
    }

    /**
     * This method is a helper method to Join(), the purpose of this method is
     * to return an array of String which will keep the Variables names that are stored both in @f1 and @f2.
     * @param f1 - Factor number 1.
     * @param f2 - Factor number 2.
     * @return @ans - and Array of String Variables names.
     */
    private String[] whichVariableToJoin(Factor f1, Factor f2){
        ArrayList<String> vars2Join = new ArrayList<>();
        String[] f1_vars = f1.getCleanName().split(",");
        String[] f2_vars = f2.getCleanName().split(",");
        for(int i = 0; i < f1_vars.length; i++){
            for(int j = 0; j < f2_vars.length; j++){
                if(!f1_vars[i].equals("") && !f2_vars[j].equals("")) {
                    if (f1_vars[i].equals(f2_vars[j])) {
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
            jump = jump / outcomes.length;
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
     * The steps of this method are:<p>
     * 1) initialize @new_CPT, @keys[], @values_to_sum (@values_to_sum size needs to be the number of the hidden Variable outcomes).<p>
     * 2) iterate through all the keys (main loop).<p>
     * 3) add the first key.<p>
     * 4) create a String (@add_by) with all the outcomes of the key you've added except the part with the hidden variable and it's outcome.<p>
     * 5) iterate through all the keys (inside the main loop) until you've added enough keys the @values_to_sum.<p>
     * 6) if found a matching key with difference only in the outcome of the hidden Variable add it to @values_to_sum.<p>
     * 7) break the loops if the size of @values_to_sum is equal to @number_of_hidden_outcomes.<p>
     * 8) sum all the values in @values_to_sum and add the value given with @key to @new_CPT.<p>
     * 9) <b>repeat step 2</b>.
     * @param f - Factor represents a CPT of an event.
     * @param hidden_name - String represents the name of the value to eliminate from the CPT of the Factor f.
     * therefore the size of the CPT is now: (size_of_original_CPT / amount_of_outcomes_of_hidden_var).
     */
    private void Eliminate(Factor f, String hidden_name){
        HashMap<String, Double> new_CPT = new HashMap<>();
        String[] keys = f.getTable().keySet().toArray(new String[0]);
        ArrayList<String> values_to_sum = new ArrayList<>();
        // iterate through all the keys.
        for(int i = 0; i < keys.length; i++){
            values_to_sum.add(keys[i]); // always add the first key.
            // split @keys[i] by "," in order to get all the Variables name that are in this key.
            String[] split_key = keys[i].substring(2, keys[i].length() - 1).split(",");
            // @add_by purpose is to show us which variable from the key we need to keep after the elimination.
            String add_by = "";
            // iterate through all the variables in @split_key.
            for(String var_name: split_key){
                // if @var_name does not contain the @hidden_name
                if(!var_name.contains(hidden_name)){
                    add_by += "," + var_name;
                }
            }
            add_by = add_by.length() > 1 ? add_by.substring(1): add_by;
            String key = "P(" + add_by + ")";
            // iterate through all the keys.
            for(int j = 0; j < keys.length; j++){
                // no need to check if @keys[j] needs to be added if i == j and @values_to_sum already contains
                // @keys[j], this if help to avoid putting the same key twice.
                if(i != j && !values_to_sum.contains(keys[j])) {
                    // same as for @keys[i].
                    split_key = keys[j].substring(2, keys[j].indexOf(")")).split(",");
                    String add_by_copy = "";
                    // same as @add_by.
                    for(String var: split_key){
                        if(add_by.contains(var)){
                            add_by_copy += "," + var;
                        }
                    }
                    add_by_copy = add_by_copy.length() > 0 ? add_by_copy.substring(1): add_by_copy;
                    // if @add_by equals to @add_by_copy it means that keys[j] has the same outcomes
                    // that need to be sum, and we can sum keys[i] with keys[j].
                    if(add_by.equals(add_by_copy)){
                        values_to_sum.add(keys[j]);
                    }
                    int number_of_hidden_outcomes = this.network.getVariable(hidden_name).getOutcomes().length;
                    // if the size of @values_to_sum has the same number of the hidden outcomes it
                    // means we've added enough keys to @values_to_sum.
                    if(values_to_sum.size() == number_of_hidden_outcomes){
                        break;
                    }
                }
            }
            double value = 0;
            // just for precaution.
            if (!new_CPT.containsKey(key)) {
                // sum all the values in @values_to_sum.
                for (String val : values_to_sum) {
                    value += f.getTable().get(val);
                }
                // the amounts of add actions is the size of @values_to_sum - 1.
                this.add_actions += values_to_sum.size() - 1;
                new_CPT.put(key, value);
            }
            values_to_sum.clear();
        }
        int ind = Integer.parseInt(this.factors.get(this.factors.size() - 1).getIndex()) + 1;
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


    /**
     * This method sorting a List of Factors in order to determine the order of their Join.
     * <p>There are two keys to determine the sort:<p> 1) table size.<p> 2) sum by ascii.<p>
     * if f1 size is smaller than f2 size then f1 index is smaller than f2 index.
     * if they have the same size -> compare them by the sum of their ascii value.
     * <p>the sort is by ascending order.
     * @param factors - List of Factors.
     */
    private void sortFactorsByOrder(ArrayList<Factor> factors){
        factors.sort((f1, f2) -> {
            Integer s1 = f1.getSize();
            Integer s2 = f2.getSize();
            int sComp = s1.compareTo(s2);
            if (sComp != 0) {
                return sComp;
            }
            Integer sum1 = SumByAscii(f1.getName());
            Integer sum2 = SumByAscii(f2.getName());
            return sum1.compareTo(sum2);
        });
    }


    /**
     * This method is used to sum the ascii value of a factor in order to sort it later.
     * @param factor_name - String of the factor_name.
     * @return sum - int of the sum by ascii of the factor name.
     */
    private int SumByAscii(String factor_name) {
        factor_name = factor_name.replaceAll("[f()0-9]+", "");
        String[] arr = factor_name.split(",");
        int sum = 0;
        for (String s : arr) {
            for (int j = 0; j < s.length(); j++) {
                int asciiValue = s.charAt(j);
                sum += asciiValue;
            }
        }
        return sum;
    }


    private void copyEvidence(String q){
        int start = q.indexOf("|");
        int end = q.indexOf(")");
        this.copyOfEvidence = q.substring(start, end);
    }


    public String getAnswer(){
        return this.answer;
    }


    public int getMultiply_actions() {
        return this.multiply_actions;
    }


    public int getAdd_actions() {
        return this.add_actions;
    }

}

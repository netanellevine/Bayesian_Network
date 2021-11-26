import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class XmlParser {
    private final Network network;


    public XmlParser(String xml_file_name){
        File xml_file = new File(xml_file_name);
        this.network = parse(xml_file);
    }

    /**
     * This method receives xml file of Bayesian Network and parse it into an Object -> Network -> Variable.
     * The parsing is divided into two main phases: A) parse all the Variables. B) parse all the CPT's.
     * @param  xml - xml file.
     * @return  - Network net represents the Bayesian network.
     */
    private Network parse(File xml) {
        Network net = new Network();
        try {
            Scanner xml_reader = new Scanner(xml);
            boolean flag = false;
            String chunk = "";
            // A) Starting with the variables.
            while (xml_reader.hasNextLine()) {
                String line_step1 = xml_reader.nextLine();
                if (line_step1.contains("<DEFINITION>")) {
                    break;
                }
                line_step1 = line_step1.stripLeading();
                // chunk is the String that keeps all the data of a single variable
                // once there is a line with '/' means that now it's </VARIABLE>
                // which means we finish with this variable.
                if (!line_step1.isEmpty() && line_step1.charAt(1) != '/') {
                    chunk += line_step1 + "\n";
                } else {
                    chunk += line_step1;
                    String[] values_step1 = chunk.split("\n");
                    if (values_step1.length > 1) {
                        // only for the first time.
                        if (!flag) {
                            values_step1 = Arrays.copyOfRange(values_step1, 1, values_step1.length);
                            flag = true;
                        }
                        String name = "";
                        String[] outcomes = new String[values_step1.length - 3];
                        for (int i = 1; i < values_step1.length - 1; ++i) {
                            String[] split_step1 = values_step1[i].split("[><]");
                            if (i == 1) { // if i == 1 it's the line with the variable name.
                                name = split_step1[2];
                            } else { // else it's lines with the variable outcomes.
                                String outcome = split_step1[2];
                                outcomes[i - 2] = outcome;
                            }
                        }
                        net.add_variable(new Variable(name, outcomes));
                    }
                    chunk = "";
                }
            }


            Variable curr_variable = null;
            // B) Moving to the definitions.
            while (xml_reader.hasNextLine()) {
                // step 1:  get line type (name, parent, table)
                String line_step2 = xml_reader.nextLine();
                if (!line_step2.contains("EFINITION>")) {
                    if (line_step2.contains("FOR")) {
                        // get the name and save it until the next <DEFINITION>
                        String current_name = line_step2.split("[><]")[2];
                        curr_variable = net.getVariable(current_name);

                    } else if (line_step2.contains("GIVEN")) {
                        // add parent to the current variable
                        // add the curr variable to be the child of the given variable.
                        String parent_name = line_step2.split("[><]")[2];
                        Variable parent = net.getVariable(parent_name);
                        curr_variable.add_parent(parent);
                        parent.add_children(curr_variable);

                        // The CPT of each variable is represented as a HashMap<String,Double>
                        // where each key is a String and each value is a double.
                        // Key format example: "P(v1=T|v2=T,V3=F)" v1,v2,v3 are variables and T/F are their outcomes.
                        // In this format it's certain that each value will have a single key, and it's
                        // easy to understand the logic of the key.
                    } else if (line_step2.contains("TABLE")) {
                        // create the table(CPT) of the current variable
                        String[] curr_CPT_values = line_step2.split("[><]")[2].split(" ");
                        String[] curr_keys = new String[curr_CPT_values.length];
                        Arrays.fill(curr_keys, "");

                        if (curr_variable.hasParent()) {
                            int size_of_steps = curr_keys.length;
                            for (Variable parent : curr_variable.getParents()) {
                                // Because CPT is basically a truth table and the outcomes of each variable
                                // must be in different order, so @size_of_steps will determine for each
                                // variable in the table how many times to stay on the same outcome until the switch.
                                // Also, @size_of_steps is modified to each variable.
                                size_of_steps = size_of_steps / parent.getOutcomes().length;
                                int k = 0, i = 0, counter = 0;
                                while (k < curr_keys.length) {
                                    if (counter < size_of_steps) {
                                        curr_keys[k] += parent.getVar_name() + "=" + parent.getOutcomes()[i] + ",";
                                        counter++;
                                        k++;
                                    } else {
                                        i++;
                                        i = i % parent.getOutcomes().length;
                                        counter = 0;
                                    }
                                }
                            }
                        }
                        boolean hasParent = curr_variable.hasParent();
                        for (int i = 0; i < curr_keys.length; i++) {
                            for (int j = 0; j < curr_variable.getOutcomes().length; j++) {
                                if(hasParent) {
                                    curr_keys[i] = curr_variable.getVar_name() + "=" + curr_variable.getOutcomes()[j] + "|" + curr_keys[i];
                                }
                                else{
                                    curr_keys[i] = curr_variable.getVar_name() + "=" + curr_variable.getOutcomes()[j];
                                }
                                i++;
                            }
                            i--;
                        }

                        for (int i = 0; i < curr_keys.length; i++) {
                            if (curr_keys[i].contains("|")) {
                                curr_keys[i] = "P(" + curr_keys[i].substring(0, curr_keys[i].length() - 1) + ")";
                            } else {
                                curr_keys[i] = "P(" + curr_keys[i] + ")";
                            }
                            curr_variable.getCPT().put(curr_keys[i], Double.parseDouble(curr_CPT_values[i]));
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Xml file not found!");
        }
        return net;
    }


    public Network getNetwork() {
        return this.network;
    }
}

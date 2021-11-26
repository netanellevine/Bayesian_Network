import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class XmlParser {
    private Network network;
    private File xml_file;


    public XmlParser(String xml_file_name){
        this.xml_file = new File(xml_file_name);
        this.network = parse(xml_file);
    }

    private Network parse(File xml) {
        Network net = new Network();
        try {
            Scanner xml_reader = new Scanner(xml);
            boolean flag = false;
            int linesCounter = 0;
            String chunk = "";
            // Starting with the variables.
            while (xml_reader.hasNextLine()) {
                String line_step1 = xml_reader.nextLine();
                if (line_step1.contains("<DEFINITION>")) {
                    break;
                }

                line_step1 = line_step1.stripLeading();

                if (!line_step1.isEmpty() && line_step1.charAt(1) != '/') {
                    chunk += line_step1 + "\n";
                } else {
                    chunk += line_step1;
                    String[] values_step1 = chunk.split("\n");

                    if (values_step1.length > 1) {
                        if (!flag) {
                            values_step1 = Arrays.copyOfRange(values_step1, 1, values_step1.length);
                            flag = true;
                        }
                        String name = "";

                        String[] outcomes = new String[values_step1.length - 3];

                        for (int i = 1; i < values_step1.length - 1; ++i) {
                            String[] split_step1 = values_step1[i].split("[><]");
                            if (i == 1) {
                                name = split_step1[2];
                            } else {
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
            // Moving to the definitions.
            while (xml_reader.hasNextLine()) {
                // step 1:  get line type (name, parent, table)
                String line_step2 = xml_reader.nextLine();
                if (!line_step2.contains("EFINITION>")) {
                    if (line_step2.contains("FOR")) {
                        // get the name and save it until the next definition
                        String current_name = line_step2.split("[><]")[2];
                        curr_variable = net.getVariable(current_name);
                    } else if (line_step2.contains("GIVEN")) {
                        // add parent to the current variable
                        // add the curr variable to be the child of the given var
                        String parent_name = line_step2.split("[><]")[2];
                        Variable parent = net.getVariable(parent_name);
                        curr_variable.add_parent(parent);
                        parent.add_children(curr_variable);
                    } else if (line_step2.contains("TABLE")) {
                        // create the table for the current variable
//                        String[] temp = line_step2.split("[><]");
                        String[] curr_CPT_values = line_step2.split("[><]")[2].split(" ");
                        String[] curr_keys = new String[curr_CPT_values.length];
                        for(int i = 0 ; i < curr_keys.length; i++){
                            curr_keys[i] = "";
                        }

                        // first add the outcomes of the current variable


                        if (curr_variable.hasParent()) {
                            int size_of_jump = curr_keys.length;
                            for (Variable parent : curr_variable.getParents()) {
                                size_of_jump = size_of_jump / parent.getOutcomes().length;
                                int k = 0, i = 0, counter = 0;
                                while (k < curr_keys.length) {
                                    if (counter < size_of_jump) {
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
            System.out.println();

        } catch (
                FileNotFoundException e) {
            System.out.println("Xml file not found!");
        }
        return net;
    }


//    private int sumOfOutcomes(Variable var){
//        int sum = var.getOutcomes().length;
//        for (Variable parent: var.getParents()) {
//            sum += parent.getOutcomes().length;
//        }
//        return sum;
//    }


    public Network getNetwork() {
        return this.network;
    }
}

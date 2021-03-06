import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Ex1 {
    public static void main(String[] args) {
        try{
//            if (args[0].equals("input.txt") || args[0].equals("input1.txt") || args[0].equals("input2.txt")){
                writeToOutput(args[0]);
//            }
        }
        catch (Exception e){
            writeToOutput("input.txt");
        }
    }

    public static void writeToOutput(String file_name){
        // parse the file in the class inputReader which activates the class XmlParser for the Xml parsing.
        inputReader firstParse = new inputReader(file_name);
//        System.out.println( firstParse.getNet());
        // all the Variable Elimination question.
        ArrayList<String> VEQ = firstParse.getVE_questions();
        // all the Bayes Ball questions.
        ArrayList<String> BBQ = firstParse.getBB_questions();
        String output = "";
        // go over on all the Bayes Ball questions.
        for (String item : BBQ) {
            Network net = firstParse.getNet();
            String curr_ans = BayesBallAlgo.Bayes(net, item);
            output += (curr_ans) + "\n";
            System.out.println(item + " -> " + curr_ans);
        }
        // go over on all the Variable Elimination questions.
        for (String value : VEQ) {
            inputReader parser = new inputReader(file_name);
            Network net = parser.getNet();
            VariableElimination VEA = new VariableElimination(value, net);
            output += (VEA.getAnswer() + "," + VEA.getAdd_actions() + "," + VEA.getMultiply_actions()) + "\n";
            System.out.println(value + " -> answer=" + VEA.getAnswer() + ", total add actions=" + VEA.getAdd_actions() + ", total multiply actions=" + VEA.getMultiply_actions());
        }
        try {
            // write to output.
            FileWriter myWriter = new FileWriter("output.txt");
            myWriter.write(output);
            myWriter.close();
            System.out.println("\nSuccessfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

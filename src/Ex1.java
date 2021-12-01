import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Ex1 {
    public static void main(String[] args) {
        writeToOutput("input2.txt");
    }

    public static void writeToOutput(String file_name){
        // parse the file in the class inputReader which activates the class XmlParser for the Xml parsing.
        inputReader firstParse = new inputReader(file_name);
        System.out.println( firstParse.getNet());
        // all the Variable Elimination question.
        ArrayList<String> VEQ = firstParse.getVE_questions();
        // all the Bayes Ball questions.
        ArrayList<String> BBQ = firstParse.getBB_questions();
        String output = "";
        // go over on all the Bayes Ball questions.
        for (String item : BBQ) {
            Network net = firstParse.getNet();
            output += (BayesBallAlgo.Bayes(net, item)) + "\n";
        }
        // go over on all the Variable Elimination questions.
        for (String value : VEQ) {
            inputReader parser = new inputReader(file_name);
            Network net = parser.getNet();
            VariableElimination VEA = new VariableElimination(value, net);
            output += (VEA.getAnswer() + "," + VEA.getAdd_actions() + "," + VEA.getMultiply_actions()) + "\n";
        }
        try {
            // write to output.
            FileWriter myWriter = new FileWriter("output.txt");
            myWriter.write(output);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

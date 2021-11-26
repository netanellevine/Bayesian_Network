import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Ex1 {
    public static void main(String[] args) {
        writeToOutput("src/input.txt");
    }

    public static void writeToOutput(String file_name){
        inputReader parserOrig = new inputReader(file_name);
        ArrayList<String> VEQ = parserOrig.getVE_questions();
        ArrayList<String> BBQ = parserOrig.getBB_questions();
        String output = "";
        for (String item : BBQ) {
            Network net = parserOrig.getNet();
            output += (BayesBallAlgo.Bayes(net, item)) + "\n";
        }
        for (String value : VEQ) {
            inputReader parser = new inputReader(file_name);
            Network net = parser.getNet();
            VariableElimination VEA = new VariableElimination(value, net);
            output += (VEA.getAnswer() + "," + VEA.getAddActions() + "," + VEA.getMultiplyActions()) + "\n";

        }
        try {
            FileWriter myWriter = new FileWriter("src/output.txt");
//            System.out.println(output);
            myWriter.write(output);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 *
 */
public class inputReader {
    private File xml_file;
    private File input_file;
    private Network net;
    private ArrayList<String> VE_questions;
    private ArrayList<String> BB_questions;


    /**
     * This constructor receives a text file which includes:
     * 1) At the first line the name of the xml file who represents the given bayesian network.
     * For example: "xml_file.xml" .
     * 2) Questions whether two events are conditionally independent.
     * For example: "B-E|J=T"
     * 3) Questions to calculate the probability of an event given one or more evidence.
     * For example: "P(B=T|J=T,M=T) A-E" .
     * #note - the "A-E" purpose is to tell which order we pick the variables.
     * <p>
     * Parsing the data of the input file into two types of data.
     * 1) the xml file will parse into a new Data Structure which will represent the Bayesian network.
     * 2) the questions will parse into an array of String so that every cell in the array is a question.
     * <p>
     * In order to solve the type of questions 2) we will use the Bayes Ball Algorithm.
     * In order to solve the type of questions 3) we will use the Variable elimination Algorithm.
     *
     * @param @file_name
     * @Throws FileNotFoundException if the file wasn't found.
     */
    public inputReader(String file_name) {
        try {
            this.input_file = new File((file_name));
            Scanner input_reader = new Scanner(this.input_file);
            // This part deals with the first line i.e. the xml file name.
            String xml_file_name = "";
            if (input_reader.hasNextLine()) {
                xml_file_name = input_reader.nextLine();
            }
            // Parse the name of the xml file to XmlParser class in order to create the Bayesian Network
//            this.net = new XmlParser("src/" + xml_file_name).getNetwork();
            this.net = new XmlParser(xml_file_name).getNetwork();

            String quest_num = "";
            // This part deals with all the questions by order:
            // 1) Bayes Ball questions -> is two Variables or more are conditionally independent given one or more evidence?
            // 2) Variable Elimination questions ->
            while (input_reader.hasNextLine()) {
                quest_num += input_reader.nextLine() + "\n";
            }
            String[] questions = quest_num.split("\n");
            this.BB_questions = new ArrayList<>();
            this.VE_questions = new ArrayList<>();
            for (String question : questions) {
                if (question.contains("P(")) {
                    this.VE_questions.add(question);
                } else {
                    this.BB_questions.add(question);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found!");
        }
    }

    public ArrayList<String> getVE_questions() {
        return this.VE_questions;
    }

    public ArrayList<String> getBB_questions() {
        return this.BB_questions;
    }

    public Network getNet() {return this.net; }

}

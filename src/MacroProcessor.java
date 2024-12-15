import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MacroProcessor {
    String fileName;
    File inputFile;
    Scanner reader;
    FileWriter writer;

    MacroProcessor(String fileName) {
        this.fileName = fileName;
        try {
            this.inputFile = new File(fileName);
            this.reader = new Scanner(inputFile);
            this.writer = new FileWriter("processor_output.txt");
            int i = 1;
            ArrayList<String> lines = new ArrayList<>();
            while(i <= 5) {
                lines.add("test" + i);
                i++;
            }
            for(String s : lines) {
                writer.write(s + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void process() {

    }

}

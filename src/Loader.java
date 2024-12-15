import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Loader {
    String fileName;
    File inputFile;
    Scanner reader;
    FileWriter writer;

    Loader(String fileName) {
        this.fileName = fileName;
        try {
            this.inputFile = new File(fileName);
            this.reader = new Scanner(inputFile);
            this.writer = new FileWriter("loader_output.txt");
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void passOne() {

    }

    public void passTwo() {

    }
}

import javafx.util.Pair;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class MacroProcessor {
    String fileName;
    File inputFile;
    Scanner reader;
    FileWriter writer;
    ArrayList<String> expandedProgram;
    boolean expanding;
    String currentLine, macroName, opCode, label;
    String[] currentLineTokens;
    int firstIndex, lastIndex;
    int definitionTableIndex;

    MacroProcessor(String fileName) {
        this.fileName = fileName;
        firstIndex = lastIndex = 0;
        expandedProgram = new ArrayList<>();
        try {
            this.inputFile = new File(fileName);
            this.reader = new Scanner(inputFile);
            this.writer = new FileWriter("processor_output.txt");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        System.out.println("---MACRO PROCESSOR DATA STRUCTURES---");
    }

    public void process() {
        expanding = false;
        try {
            while(reader.hasNext()) {
                getLine();
                processLine();
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    public void showResult() {
        System.out.println("\nName Table: ");
        DataStructures.showNameTable();
        System.out.println("\nDefinition Table: ");
        DataStructures.showDefinitionTable();
        try {
            for(String line : expandedProgram) writer.write(line + "\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private void getLine() {
        if(expanding) { //get next line from the Definition Table
            currentLine = DataStructures.definitionTable.get(definitionTableIndex);
            definitionTableIndex++;
        } else { //get next line from the input file
            currentLine = reader.nextLine().trim();
        }
        currentLineTokens = currentLine.split("\\s+");
    }

    private void processLine() {
        if(currentLine.startsWith(".") || currentLine.isEmpty()) return;
        if(currentLineTokens.length == 3) {
            opCode = currentLineTokens[1];
        } else {
            opCode = currentLineTokens[0];
        }
        if(DataStructures.nameTable.containsKey(opCode)) expand();
        else if(opCode.equals("MACRO")) define();
        else { // add the line to the expanded file
            if(expanding) return;
            if(opCode.equals(currentLineTokens[0]) && currentLineTokens.length > 1) {
                currentLine = String.format("%-14s%-18s%-14s", "", currentLineTokens[0], currentLineTokens[1]);
            }
            expandedProgram.add(currentLine);
        }
    }

    private void define() {
        int level = 1;
        macroName = currentLineTokens[0];
        DataStructures.nameTable.put(macroName, new Pair<>(0, 0));
        currentLine = String.format("%-18s%-14s", currentLineTokens[0], currentLineTokens[2]);
        DataStructures.definitionTable.add(currentLine); //add macro prototype to the Definition Table
        String[] arguments = currentLineTokens[2].split(",");
        String argument = "";
        while(level > 0) {
            getLine();
            if(currentLine.startsWith(".") || currentLine.isEmpty()) continue;
            if(currentLineTokens.length == 3) {
                opCode = currentLineTokens[1];
                argument = currentLineTokens[2];
            } else if(currentLineTokens.length == 2) {
                opCode = currentLineTokens[0];
                argument = currentLineTokens[1];
            } else {
                opCode = currentLineTokens[0];
            }

            for(int i=0; i<arguments.length; ++i) {
                if(!argument.isEmpty() && argument.contains(arguments[i])) argument = argument.replace(arguments[i], "?"+(i+1));
            }

            if(currentLineTokens.length == 3) {
                currentLine = String.format("%-14s%-18s%-14s", currentLineTokens[0], opCode, argument);
            } else if(currentLineTokens.length == 2) {
                currentLine = String.format("%-18s%-14s", opCode, argument);
            } else {
                currentLine = opCode;
            }

            if(opCode.equals("MACRO")) level++;
            if(opCode.equals("MEND")) level--;

            DataStructures.definitionTable.add(currentLine);
        }
        lastIndex = DataStructures.definitionTable.size()-1;
        DataStructures.nameTable.put(macroName, new Pair<>(firstIndex, lastIndex));
        firstIndex = lastIndex + 1;
    }

    private void expand() {
        String argument = "";
        label = "";
        if(currentLineTokens.length == 3) {
            label = currentLineTokens[0];
            macroName = currentLineTokens[1];
            argument = currentLineTokens[2];
            expandedProgram.add("." + String.format("%-13s%-18s%-14s", currentLineTokens[0], currentLineTokens[1], currentLineTokens[2]));
        } else if(currentLineTokens.length == 2) {
            macroName = currentLineTokens[0];
            argument = currentLineTokens[1];
            expandedProgram.add(String.format("%-13s%-19s%-14s", "", "."+currentLineTokens[0], currentLineTokens[1]));
        } else {
            macroName = currentLineTokens[0];
            expandedProgram.add("." + currentLine);
        }

        expanding = true;
        DataStructures.argumentTable = argument.split(",");
        Pair<Integer, Integer> indexes = DataStructures.nameTable.get(macroName);

        for(definitionTableIndex = indexes.getKey()+1; definitionTableIndex < indexes.getValue();) {
            getLine();
            processLine();
            String expandedLine = currentLine;
            if(currentLine.contains("?")) {
                int indexOfNumber = currentLine.indexOf("?") + 1;
                int indexOfArgument = currentLine.charAt(indexOfNumber) - '0';
                expandedLine = currentLine.replaceAll("\\?\\w", DataStructures.argumentTable[indexOfArgument - 1]);
            }
            if(definitionTableIndex-1 == indexes.getKey()+1) {
                if(!label.isEmpty()) {
                    expandedProgram.add(String.format("%-14s%-14s", label, expandedLine));
                    continue;
                }
            }
            expandedProgram.add(String.format("%-14s", "") + expandedLine);
        }
        expanding = false;
    }
}

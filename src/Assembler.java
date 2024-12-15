import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler {
    String inputFileName;
    File inputFile;
    Scanner reader;
    FileWriter writer;
    String programName;
    int startingAddress;
    int locationCounter;
    int programLength;

    Assembler(String inputFileName) {
        this.inputFileName = inputFileName;
        this.programName = "";
        this.startingAddress = 0;
        this.programLength = 0;
        this.locationCounter = 0;
        try {
            this.inputFile = new File(inputFileName);
            this.reader = new Scanner(inputFile);
            this.writer = new FileWriter("assembler_output.txt");
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void passOne() {
        String currentLine, label, opCode, operand;
        String[] currentLineTokens;
        try {
            while(reader.hasNext()) {
                currentLine = reader.nextLine().trim();
                if(currentLine.startsWith(".")) continue;
                label=""; opCode=""; operand="";
                currentLineTokens = currentLine.split("\\s+");
                if(currentLineTokens.length == 3) {
                    label = currentLineTokens[0];
                    opCode = currentLineTokens[1];
                    operand = currentLineTokens[2];
                } else if (currentLineTokens.length == 2) {
                    opCode = currentLineTokens[0];
                    operand = currentLineTokens[1];
                } else {
                    opCode = currentLineTokens[0];
                }
                if(opCode.equals("START")) {
                    programName = label;
                    startingAddress = Integer.parseInt(operand, 16);
                    locationCounter = startingAddress;
                    continue;
                }
                if(opCode.equals("END")) break;
                if(!label.isEmpty()) {
                    if(DataStructures.symbolTable.containsKey(label)) {
                        throw new Exception(".\n*ERROR* Duplicate Symbol: \"" + label + "\"\n.");
                    } else {
                        DataStructures.symbolTable.put(label, locationCounter);
                    }
                }
                if(DataStructures.operationTable.containsKey(opCode)) {
                    locationCounter += 3;
                } else if(opCode.equals("WORD")) {
                    locationCounter += 3;
                } else if(opCode.equals("RESW")) {
                    locationCounter += 3 * Integer.parseInt(operand);
                } else if(opCode.equals("RESB")) {
                    locationCounter += Integer.parseInt(operand);
                } else if(opCode.equals("BYTE")) {
                    if(operand.startsWith("C'") && operand.endsWith("'")) {
                        locationCounter += operand.length()-3;
                    } else if(operand.startsWith("X'") && operand.endsWith("'")) {
                        locationCounter += (operand.length()-3) / 2;
                    }
                } else {
                    throw new Exception(".\n*ERROR* Invalid Operation Code: \"" + opCode + "\"\n.");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            reader.close();
        }
        programLength = locationCounter - startingAddress;
        System.out.println("Program Name: " + programName);
        System.out.println("Starting Address: " + Integer.toHexString(startingAddress).toUpperCase());
        System.out.println("Program Length: " + Integer.toHexString(programLength).toUpperCase());
        System.out.println("\nSymbol Table:\n");
        System.out.println("Symbol" + "\t  " + "Location");
        DataStructures.showSymbolTable();
    }

    public void passTwo() {

        locationCounter = startingAddress;
        String currentLine, label, opCode, operand;
        String[] currentLineTokens = new String[]{""};

        ArrayList<String> objectProgram = new ArrayList<>();
        String headerRecord = "H | " + programName + " | " + Integer.toHexString(startingAddress).toUpperCase() + " | " + Integer.toHexString(programLength).toUpperCase() + "\n";
        objectProgram.add(headerRecord);

        int LENGTH_LIMIT = 30;
        String objectCode = "";
        StringBuilder currentTextRecord = new StringBuilder();
        int currentRecordStartingAddress = startingAddress;
        int bytesCount = 0;

        try {
            this.reader = new Scanner(inputFile);
            while(reader.hasNext()) {
                currentLine = reader.nextLine().trim();
                if (currentLine.startsWith(".")) continue;
                opCode = "";
                operand = "";
                objectCode = "";
                currentLineTokens = currentLine.split("\\s+");
                if (currentLineTokens.length == 3) {
                    opCode = currentLineTokens[1];
                    operand = currentLineTokens[2];
                } else if (currentLineTokens.length == 2) {
                    opCode = currentLineTokens[0];
                    operand = currentLineTokens[1];
                } else {
                    opCode = currentLineTokens[0];
                }
                if (opCode.equals("START")) continue;
                if (opCode.equals("END")) break;
                if(opCode.equals("RESW") || opCode.equals("RESB")) {
                    if(bytesCount > 0) {
                        currentTextRecord = new StringBuilder("T | " + Integer.toHexString(currentRecordStartingAddress).toUpperCase() + " | " + String.format("%2s", Integer.toHexString(bytesCount).toUpperCase()).replace(" ", "0") + " | " + currentTextRecord + "\n");
                        objectProgram.add(currentTextRecord.toString());
                        currentTextRecord = new StringBuilder();
                        currentRecordStartingAddress += bytesCount;
                        bytesCount = 0;
                    }
                    if(opCode.equals("RESW")) currentRecordStartingAddress += 3 * Integer.parseInt(operand);
                    if(opCode.equals("RESB")) currentRecordStartingAddress += Integer.parseInt(operand);
                    continue;
                } else if(opCode.equals("WORD")) {
                    objectCode = String.format("%6s" ,Integer.toHexString(Integer.parseInt(operand))).replace(" ", "0") + " ";
                } else if(opCode.equals("BYTE")) {
                    if(operand.startsWith("X'")) {
                        operand = operand.substring(2, operand.length()-1);
                        objectCode = operand + " ";
                    } else if(operand.startsWith("C'")) {
                        operand = operand.substring(2, operand.length()-1);
                        for(char c : operand.toCharArray()) objectCode += DataStructures.ASCIITable.get(Character.toString(c));
                        objectCode += " ";
                    }
                } else if(DataStructures.operationTable.containsKey(opCode)) {
                    if(opCode.equals("RSUB")) {
                        objectCode = DataStructures.operationTable.get("RSUB")  + "0000 ";
                    } else {
                        boolean indexed = false;
                        if(operand.endsWith(",X")) {
                            indexed = true;
                            operand = operand.substring(0, operand.length()-2);
                        }
                        objectCode = DataStructures.operationTable.get(opCode) + Integer.toHexString(DataStructures.symbolTable.get(operand)).toUpperCase() + " ";
                        if(indexed) { // add 0x8000
                            int newObjectCode = Integer.parseInt(objectCode.trim()) + 8000;
                            objectCode = newObjectCode + " ";
                        }
                    }
                } else {
                    throw new Exception(".\n*ERROR* Invalid Operation Code: \"" + opCode + "\"\n.");
                }
                if(objectCode.trim().length()/2 + bytesCount > LENGTH_LIMIT) {
                    currentTextRecord = new StringBuilder("T | " + Integer.toHexString(currentRecordStartingAddress).toUpperCase() + " | " + String.format("%2s", Integer.toHexString(bytesCount).toUpperCase()).replace(" ", "0") + " | " + currentTextRecord + "\n");
                    objectProgram.add(currentTextRecord.toString());
                    currentTextRecord = new StringBuilder();
                    currentRecordStartingAddress += bytesCount;
                    bytesCount = 0;
                }
                currentTextRecord.append(objectCode);
                bytesCount += objectCode.trim().length()/2;
            }
            currentTextRecord = new StringBuilder("T | " + Integer.toHexString(currentRecordStartingAddress).toUpperCase() + " | " + String.format("%2s", Integer.toHexString(bytesCount).toUpperCase()).replace(" ", "0") + " | " + currentTextRecord + "\n");
            objectProgram.add(currentTextRecord.toString());
            String endRecord = "E | " + String.format("%6s", Integer.toHexString(startingAddress)).replace(" ", "0")  + "\n";
            objectProgram.add(endRecord);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            for(String record : objectProgram) { // print the records to the output file
                writer.write(record);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

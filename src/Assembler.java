import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;

public class Assembler {
    String inputFileName;
    File inputFile;
    Scanner reader;
    FileWriter writer;
    String programName;
    int startingAddress;
    int locationCounter;
    int programLength;
    ArrayList<String> objectProgram;

    Assembler(String inputFileName) {
        this.inputFileName = inputFileName;
        this.startingAddress = 0;
        this.programLength = 0;
        this.locationCounter = 0;
        try {
            this.inputFile = new File(inputFileName);
            this.reader = new Scanner(inputFile);
            this.writer = new FileWriter("assembler_output.txt");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void passOne() {
        String currentLine, label, opCode, operand;
        String[] currentLineTokens;
        try {
            while(reader.hasNext()) {
                currentLine = reader.nextLine().trim();
                if(currentLine.startsWith(".") || currentLine.isEmpty()) continue;
                label=""; operand="";
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
                        throw new Exception("\n*ERROR* Duplicate Symbol: \"" + label + "\"");
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
                    throw new Exception("\n*ERROR* Invalid Operation Code: \"" + opCode + "\"");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            reader.close();
        }
        programLength = locationCounter - startingAddress;
    }

    public void passTwo() {
        locationCounter = startingAddress;
        String currentLine, opCode, operand;
        String[] currentLineTokens;
        objectProgram = new ArrayList<>();

        String headerRecord = "H | " + programName
                + " | " + String.format("%6s", Integer.toHexString(startingAddress)).replace(" ", "0").toUpperCase()
                + " | " + String.format("%6s", Integer.toHexString(programLength)).replace(" ", "0").toUpperCase() + "\n";
        objectProgram.add(headerRecord);

        int LENGTH_LIMIT = 30; // Length Limit in Bytes for each Text Record
        StringBuilder objectCode;
        StringBuilder currentTextRecord = new StringBuilder();
        int currentRecordStartingAddress = startingAddress;
        int bytesCount = 0;

        try {
            this.reader = new Scanner(inputFile);
            while(reader.hasNext()) {
                currentLine = reader.nextLine().trim();
                if (currentLine.startsWith(".") || currentLine.isEmpty()) continue;
                operand = "";
                objectCode = new StringBuilder();
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
                    if(bytesCount > 0) { //end the current text record then reserve the desired area in memory
                        currentTextRecord = new StringBuilder(
                            "T | " + Integer.toHexString(currentRecordStartingAddress).toUpperCase()
                            + " | " + String.format("%2s", Integer.toHexString(bytesCount).toUpperCase()).replace(" ", "0")
                            + " | " + currentTextRecord + "\n"
                        );
                        objectProgram.add(currentTextRecord.toString());
                        currentTextRecord = new StringBuilder();
                        currentRecordStartingAddress += bytesCount;
                        bytesCount = 0;
                    }
                    if(opCode.equals("RESW")) currentRecordStartingAddress += 3 * Integer.parseInt(operand);
                    if(opCode.equals("RESB")) currentRecordStartingAddress += Integer.parseInt(operand);
                    continue;
                } else if(opCode.equals("WORD")) {
                    objectCode = new StringBuilder(String.format("%6s", Integer.toHexString(Integer.parseInt(operand))).replace(" ", "0") + " ");
                } else if(opCode.equals("BYTE")) {
                    if(operand.startsWith("X'")) {
                        operand = operand.substring(2, operand.length()-1);
                        objectCode = new StringBuilder(operand + " ");
                    } else if(operand.startsWith("C'")) {
                        operand = operand.substring(2, operand.length()-1);
                        for(char c : operand.toCharArray()) objectCode.append(DataStructures.ASCIITable.get(Character.toString(c)));
                        objectCode.append(" ");
                    }
                } else if(DataStructures.operationTable.containsKey(opCode)) {
                    if(opCode.equals("RSUB")) {
                        objectCode = new StringBuilder(DataStructures.operationTable.get("RSUB") + "0000 ");
                    } else {
                        if(operand.endsWith(",X")) { //indexed addressing mode
                            operand = operand.substring(0, operand.length()-2);
                            objectCode = new StringBuilder(DataStructures.operationTable.get(opCode)
                                    + Integer.toHexString(DataStructures.symbolTable.get(operand) | 0x8000).toUpperCase() + " ");
                        } else { //direct addressing mode
                            objectCode = new StringBuilder(DataStructures.operationTable.get(opCode)
                                    + Integer.toHexString(DataStructures.symbolTable.get(operand)).toUpperCase() + " ");
                        }
                    }
                } else {
                    throw new Exception("\n*ERROR* Invalid Operation Code: \"" + opCode + "\"");
                }
                int currentLength = objectCode.toString().trim().length() / 2;
                if(currentLength + bytesCount > LENGTH_LIMIT) { //check length and end the current text record if needed
                    currentTextRecord = new StringBuilder(
                        "T | " + Integer.toHexString(currentRecordStartingAddress).toUpperCase()
                        + " | " + String.format("%2s", Integer.toHexString(bytesCount).toUpperCase()).replace(" ", "0")
                        + " | " + currentTextRecord + "\n"
                    );
                    objectProgram.add(currentTextRecord.toString());
                    currentTextRecord = new StringBuilder();
                    currentRecordStartingAddress += bytesCount;
                    bytesCount = 0;
                }
                currentTextRecord.append(objectCode);
                bytesCount += currentLength;
            }
            //add the last text record
            currentTextRecord = new StringBuilder(
                "T | " + Integer.toHexString(currentRecordStartingAddress).toUpperCase()
                + " | " + String.format("%2s", Integer.toHexString(bytesCount).toUpperCase()).replace(" ", "0")
                + " | " + currentTextRecord + "\n"
            );
            objectProgram.add(currentTextRecord.toString());
            String endRecord = "E | " + String.format("%6s", Integer.toHexString(startingAddress)).replace(" ", "0")  + "\n";
            objectProgram.add(endRecord);
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void showResult() {
        System.out.println("---ASSEMBLER DATA STRUCTURES---\n");
        System.out.println("Symbol Table:");
        System.out.println("Symbol" + "\t  " + "Location");
        DataStructures.showSymbolTable();
        System.out.println();
        try {
            for(String record : objectProgram) { // print the records into the output file
                writer.write(record);
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}

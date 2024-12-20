import javafx.util.Pair;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Loader {
    String fileName;
    File inputFile;
    Scanner reader;
    FileWriter writer;
    int startingAddress;

    Loader(String fileName, int startingAddress) {
        this.fileName = fileName;
        try {
            this.inputFile = new File(fileName);
            this.reader = new Scanner(inputFile);
            this.writer = new FileWriter("loader_output.txt");
            this.startingAddress = startingAddress;
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void passOne() {
        String currentLine;
        String[] currentLineTokens;
        String controlSectionName;
        int controlSectionAddress = startingAddress;
        int controlSectionLength = 0;

        try {
            while(reader.hasNext()) {
                currentLine = reader.nextLine();
                currentLineTokens = currentLine.split("\\s+");
                if(currentLine.startsWith("H")) {
                    controlSectionName = currentLineTokens[1];
                    controlSectionLength = Integer.parseInt(currentLineTokens[3], 16);
                    if(DataStructures.externalSymbolTable.containsKey(controlSectionName)) {
                        throw new Exception("\n*ERROR* Duplicate External Symbol: \"" + controlSectionName + "\"");
                    } else {
                        DataStructures.externalSymbolTable.put(controlSectionName, new Pair<>(controlSectionAddress, controlSectionLength));
                    }
                } else if(currentLine.startsWith("D")) {
                    int symbolAddress;
                    String symbolName;
                    for(int i=1; i<currentLineTokens.length; i+=2) {
                        symbolName = currentLineTokens[i];
                        symbolAddress = Integer.parseInt(currentLineTokens[i+1], 16);
                        if(DataStructures.externalSymbolTable.containsKey(symbolName)) {
                            throw new Exception("\n*ERROR* Duplicate External Symbol: \"" + symbolName + "\"");
                        } else {
                            DataStructures.externalSymbolTable.put(symbolName, new Pair<>(controlSectionAddress + symbolAddress, 0));
                        }
                    }
                } else if(currentLine.startsWith("E")) {
                    controlSectionAddress += controlSectionLength;
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    public void passTwo() {
        String currentLine;
        String[] currentLineTokens;
        int controlSectionAddress = startingAddress;
        int controlSectionLength = 0;
        int recordStartingAddress;
        int recordLength;
        StringBuilder recordObjectCodes;

        try {
            reader = new Scanner(inputFile);
            while(reader.hasNext()) {
                currentLine = reader.nextLine();
                currentLineTokens = currentLine.split("\\s+");
                if(currentLine.startsWith("H")) {
                    controlSectionLength = DataStructures.externalSymbolTable.get(currentLineTokens[1]).getValue();
                } else if(currentLine.startsWith("T")) {
                    recordObjectCodes = new StringBuilder();
                    recordStartingAddress = controlSectionAddress + Integer.parseInt(currentLineTokens[1], 16);
                    recordLength = Integer.parseInt(currentLineTokens[2], 16);
                    for(int i=3; i<currentLineTokens.length; ++i) recordObjectCodes.append(currentLineTokens[i]);
                    for(int i=0; i<recordLength*2; i+=2) {
                        int halfBytesValue = Integer.parseInt(recordObjectCodes.substring(i, i+2), 16);
                        DataStructures.memory.put(recordStartingAddress+(i/2), (byte)halfBytesValue);
                    }
                } else if (currentLine.startsWith("M")) {
                    int address = controlSectionAddress + Integer.parseInt(currentLineTokens[1], 16);
                    int halfBytesNumber = Integer.parseInt(currentLineTokens[2]);
                    String symbol = currentLineTokens[3].substring(1);
                    boolean isAddition = currentLineTokens[3].startsWith("+");

                    int value = 0;
                    for (int i = 0; i < 3; i++) { //get 3 bytes from memory and store them in "value"
                        value = (value << 8) | (DataStructures.memory.containsKey(address + i) ? DataStructures.memory.get(address + i) & 0xFF : 0);
                    }

                    if (DataStructures.externalSymbolTable.containsKey(symbol)) {
                        int symbolAddress = DataStructures.externalSymbolTable.get(symbol).getKey();
                        if (isAddition) { //modify the address
                            value += symbolAddress;
                        } else {
                            value -= symbolAddress;
                        }
                        for (int i = 2; i >= 0; i--) { //write back the 3 bytes to memory
                            if (i == 0 && halfBytesNumber % 2 != 0) { //modify half byte only
                                int originalValue = DataStructures.memory.getOrDefault(address, (byte)0) & 0xFF;
                                DataStructures.memory.put(address, (byte)((originalValue & 0xF0) | (value & 0x0F)));
                            } else { //modify whole byte
                                DataStructures.memory.put(address + i, (byte)(value & 0xFF));
                            }
                            value >>= 8;
                        }
                    } else {
                        throw new Exception("\n*ERROR* Undefined External Symbol: \"" + symbol + "\"");
                    }
                } else if(currentLine.startsWith("E")) {
                    controlSectionAddress += controlSectionLength;
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    public void showResult() {
        System.out.println("---LOADER DATA STRUCTURES---");
        System.out.println("\nExternal Symbol Table:");
        System.out.printf("%-10s%-10s%-10s", "Name", "Address", "Length");
        System.out.println();
        DataStructures.showExternalSymbolTable();
        DataStructures.showMemory(startingAddress, writer);
        System.out.println();
    }
}

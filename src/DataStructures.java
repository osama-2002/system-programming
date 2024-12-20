import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.FileWriter;

public class DataStructures {
    // Assembler Data Structures:
    static HashMap<String, String> operationTable = new HashMap<>();
    static HashMap<String, String> ASCIITable = new HashMap<>();
    static HashMap<String, Integer> symbolTable = new HashMap<>();
    //Macro Processor Data Structures:
    static HashMap<String, Pair<Integer, Integer>> nameTable = new HashMap<>();
    static ArrayList<String> definitionTable = new ArrayList<>();
    static String[] argumentTable;
    //Loader Data Structures:
    static HashMap<String, Pair<Integer, Integer>> externalSymbolTable = new HashMap<>();
    static HashMap<Integer, Byte> memory = new HashMap<>();
    //Visualization Functions
    static void showSymbolTable() {
        for(Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            System.out.printf("%-10s", entry.getKey());
            System.out.println(Integer.toHexString(entry.getValue()).toUpperCase());
        }
    }
    static void showNameTable() {
        for(Map.Entry<String, Pair<Integer, Integer>> entry : nameTable.entrySet()) {
            System.out.printf("%-18s", entry.getKey());
            System.out.println(entry.getValue().getKey() + " : " + entry.getValue().getValue());
        }
    }
    static void showDefinitionTable() {
        for(String line : definitionTable) System.out.println(line);
    }
    static void showExternalSymbolTable() {
        for(Map.Entry<String, Pair<Integer, Integer>> entry : externalSymbolTable.entrySet()) {
            System.out.printf(
                    "%-10s%-10s%-10s",
                    entry.getKey(),
                    Integer.toHexString(entry.getValue().getKey()).toUpperCase(),
                    Integer.toHexString(entry.getValue().getValue()).equals("0") ? "" : Integer.toHexString(entry.getValue().getValue()).toUpperCase()
            );
            System.out.println();
        }
    }
    static void showMemory(int programAddress, FileWriter writer) {
        try {
            writer.write("Memory Address                       Contents\n\n");
            for (int i = programAddress; i <= programAddress + 0x150; i += 0x10) {
                writer.write(String.format("     %04X         |     ", i));
                for (int j = 0; j < 0x10; j += 4) {
                    for (int k = 0; k < 4; k++) {
                        int address = i + j + k;
                        if (memory.containsKey(address)) {
                            writer.write(String.format("%02X", memory.get(address)));
                        } else {
                            writer.write("xx");
                        }
                    }
                    writer.write(" ");
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    static { //initialization of the Operation Table
        operationTable.put("ADD", "18");
        operationTable.put("ADDF", "58");
        operationTable.put("ADDR", "90");
        operationTable.put("AND", "40");
        operationTable.put("CLEAR", "B4");
        operationTable.put("COMP", "28");
        operationTable.put("COMPF", "88");
        operationTable.put("COMPR", "A0");
        operationTable.put("DIV", "24");
        operationTable.put("DIVF", "64");
        operationTable.put("DIVR", "9C");
        operationTable.put("FIX", "C4");
        operationTable.put("FLOAT", "C0");
        operationTable.put("HIO", "F4");
        operationTable.put("J", "3C");
        operationTable.put("JEQ", "30");
        operationTable.put("JGT", "34");
        operationTable.put("JLT", "38");
        operationTable.put("JSUB", "48");
        operationTable.put("LDA", "00");
        operationTable.put("LDB", "68");
        operationTable.put("LDCH", "50");
        operationTable.put("LDF", "70");
        operationTable.put("LDL", "08");
        operationTable.put("LDS", "6C");
        operationTable.put("LDT", "74");
        operationTable.put("LDX", "04");
        operationTable.put("LPS", "D0");
        operationTable.put("MUL", "20");
        operationTable.put("MULF", "60");
        operationTable.put("MULR", "98");
        operationTable.put("NORM", "C8");
        operationTable.put("OR", "44");
        operationTable.put("RD", "D8");
        operationTable.put("RMO", "AC");
        operationTable.put("RSUB", "4C");
        operationTable.put("SHIFTL", "A4");
        operationTable.put("SHIFTR", "A8");
        operationTable.put("SIO", "F0");
        operationTable.put("SSK", "EC");
        operationTable.put("STA", "0C");
        operationTable.put("STB", "78");
        operationTable.put("STCH", "54");
        operationTable.put("STF", "80");
        operationTable.put("STI", "D4");
        operationTable.put("STL", "14");
        operationTable.put("STS", "7C");
        operationTable.put("STSW", "E8");
        operationTable.put("STT", "84");
        operationTable.put("STX", "10");
        operationTable.put("SUB", "1C");
        operationTable.put("SUBF", "5C");
        operationTable.put("SUBR", "94");
        operationTable.put("SVC", "B0");
        operationTable.put("TD", "E0");
        operationTable.put("TIO", "F8");
        operationTable.put("TIX", "2C");
        operationTable.put("TIXR", "B8");
        operationTable.put("WD", "DC");
    }
    static { //initialization of the ASCII Table
        ASCIITable.put("NUL", "00");
        ASCIITable.put(" ", "00");
        ASCIITable.put("SOH", "01");
        ASCIITable.put("STX", "02");
        ASCIITable.put("ETX", "03");
        ASCIITable.put("EOT", "04");
        ASCIITable.put("ENQ", "05");
        ASCIITable.put("ACK", "06");
        ASCIITable.put("BEL", "07");
        ASCIITable.put("BS", "08");
        ASCIITable.put("HT", "09");
        ASCIITable.put("LF", "0A");
        ASCIITable.put("VT", "0B");
        ASCIITable.put("FF", "0C");
        ASCIITable.put("CR", "0D");
        ASCIITable.put("SO", "0E");
        ASCIITable.put("SI", "0F");
        ASCIITable.put("DLE", "10");
        ASCIITable.put("DC1", "11");
        ASCIITable.put("DC2", "12");
        ASCIITable.put("DC3", "13");
        ASCIITable.put("DC4", "14");
        ASCIITable.put("NAK", "15");
        ASCIITable.put("SYN", "16");
        ASCIITable.put("ETB", "17");
        ASCIITable.put("CAN", "18");
        ASCIITable.put("EM", "19");
        ASCIITable.put("SUB", "1A");
        ASCIITable.put("ESC", "1B");
        ASCIITable.put("FS", "1C");
        ASCIITable.put("GS", "1D");
        ASCIITable.put("RS", "1E");
        ASCIITable.put("US", "1F");
        ASCIITable.put("SP", "20");
        ASCIITable.put("!", "21");
        ASCIITable.put("\"", "22");
        ASCIITable.put("#", "23");
        ASCIITable.put("$", "24");
        ASCIITable.put("%", "25");
        ASCIITable.put("&", "26");
        ASCIITable.put("'", "27");
        ASCIITable.put("(", "28");
        ASCIITable.put(")", "29");
        ASCIITable.put("*", "2A");
        ASCIITable.put("+", "2B");
        ASCIITable.put(",", "2C");
        ASCIITable.put("-", "2D");
        ASCIITable.put(".", "2E");
        ASCIITable.put("/", "2F");
        ASCIITable.put("0", "30");
        ASCIITable.put("1", "31");
        ASCIITable.put("2", "32");
        ASCIITable.put("3", "33");
        ASCIITable.put("4", "34");
        ASCIITable.put("5", "35");
        ASCIITable.put("6", "36");
        ASCIITable.put("7", "37");
        ASCIITable.put("8", "38");
        ASCIITable.put("9", "39");
        ASCIITable.put(":", "3A");
        ASCIITable.put(";", "3B");
        ASCIITable.put("<", "3C");
        ASCIITable.put("=", "3D");
        ASCIITable.put(">", "3E");
        ASCIITable.put("?", "3F");
        ASCIITable.put("@", "40");
        ASCIITable.put("A", "41");
        ASCIITable.put("B", "42");
        ASCIITable.put("C", "43");
        ASCIITable.put("D", "44");
        ASCIITable.put("E", "45");
        ASCIITable.put("F", "46");
        ASCIITable.put("G", "47");
        ASCIITable.put("H", "48");
        ASCIITable.put("I", "49");
        ASCIITable.put("J", "4A");
        ASCIITable.put("K", "4B");
        ASCIITable.put("L", "4C");
        ASCIITable.put("M", "4D");
        ASCIITable.put("N", "4E");
        ASCIITable.put("O", "4F");
        ASCIITable.put("P", "50");
        ASCIITable.put("Q", "51");
        ASCIITable.put("R", "52");
        ASCIITable.put("S", "53");
        ASCIITable.put("T", "54");
        ASCIITable.put("U", "55");
        ASCIITable.put("V", "56");
        ASCIITable.put("W", "57");
        ASCIITable.put("X", "58");
        ASCIITable.put("Y", "59");
        ASCIITable.put("Z", "5A");
        ASCIITable.put("[", "5B");
        ASCIITable.put("\\", "5C");
        ASCIITable.put("]", "5D");
        ASCIITable.put("^", "5E");
        ASCIITable.put("_", "5F");
        ASCIITable.put("`", "60");
        ASCIITable.put("a", "61");
        ASCIITable.put("b", "62");
        ASCIITable.put("c", "63");
        ASCIITable.put("d", "64");
        ASCIITable.put("e", "65");
        ASCIITable.put("f", "66");
        ASCIITable.put("g", "67");
        ASCIITable.put("h", "68");
        ASCIITable.put("i", "69");
        ASCIITable.put("j", "6A");
        ASCIITable.put("k", "6B");
        ASCIITable.put("l", "6C");
        ASCIITable.put("m", "6D");
        ASCIITable.put("n", "6E");
        ASCIITable.put("o", "6F");
        ASCIITable.put("p", "70");
        ASCIITable.put("q", "71");
        ASCIITable.put("r", "72");
        ASCIITable.put("s", "73");
        ASCIITable.put("t", "74");
        ASCIITable.put("u", "75");
        ASCIITable.put("v", "76");
        ASCIITable.put("w", "77");
        ASCIITable.put("x", "78");
        ASCIITable.put("y", "79");
        ASCIITable.put("z", "7A");
        ASCIITable.put("{", "7B");
        ASCIITable.put("|", "7C");
        ASCIITable.put("}", "7D");
        ASCIITable.put("~", "7E");
        ASCIITable.put("DEL", "7F");
    }
}

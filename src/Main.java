public class Main {
    public static void main(String[] args) {
        //Testing the assembler
        Assembler assembler = new Assembler("src/assembler_input.txt");
        assembler.passOne();
        assembler.passTwo();

//        //Testing the loader
//        Loader loader = new Loader("src/loader_input.txt");
//        loader.passOne();
//        loader.passTwo();
//
        //Testing the macroProcessor
        MacroProcessor processor = new MacroProcessor("src/processor_input.txt");
        processor.process();
        processor.showResult();
    }
}

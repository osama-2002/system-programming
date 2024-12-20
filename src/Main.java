public class Main {
    public static void main(String[] args) {
        //Testing the Assembler
        Assembler assembler = new Assembler("src/assembler_input.txt");
        assembler.passOne();
        assembler.passTwo();
        assembler.showResult();

        //Testing the Loader
        Loader loader = new Loader("src/loader_input.txt", 0x4000);
        loader.passOne();
        loader.passTwo();
        loader.showResult();

        //Testing the Macro Processor
        MacroProcessor processor = new MacroProcessor("src/processor_input.txt");
        processor.process();
        processor.showResult();
    }
}

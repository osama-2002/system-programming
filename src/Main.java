import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //Testing the assembler
        //Assembler assembler = new Assembler("src/assembler_input.txt");
        //assembler.passOne();
        //assembler.passTwo();

        //Testing the loader
//        LinkingLoader loader = new LinkingLoader("src/loader_input.txt", 0x4000);
//        loader.pass1();
//        loader.pass2();
        Loader loader = new Loader("src/loader_input.txt", 0x4000);
        loader.passOne();
        loader.passTwo();

        //Testing the macroProcessor
        //MacroProcessor processor = new MacroProcessor("src/processor_input.txt");
        //processor.process();
        //processor.showResult();
    }
}

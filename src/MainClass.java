import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) throws FileNotFoundException {
    	//String INPUT_METHOD = "hardcoded";
    	String INPUT_METHOD = "fromtxt";
    	String SPLIT_METHOD = "greedy";
    	//String SPLIT_METHOD = "alternative";
    	
    	runSplitter(INPUT_METHOD, SPLIT_METHOD);
    	//test();
    }
    
    public static void runSplitter(String input_method, String split_method) throws FileNotFoundException {
    	Application app = new Application();
    	if (input_method == "hardcoded") {
    		app = initHardcoded();
    	}
    	else {
    		app = initFromTxt();
    	}
    	app.printTotalPaid();
        app.printBalances();
    	if (split_method == "alternative") {
    		app.split();
    	}
    	else {
    		app.splitGreedy();
    	}
    }
    
    public static void test() throws FileNotFoundException {
    	System.out.println("Testing hardcoded alternative:");
    	runSplitter("hardcoded", "alternative");
    	System.out.println();
    	System.out.println("Testing hardcoded greedy:");
    	runSplitter("hardcoded", "greedy");
    	System.out.println();
    	System.out.println("Testing fromtxt alternative:");
    	runSplitter("fromtxt", "alternative");
    	System.out.println();
    	System.out.println("Testing fromtxt greedy:");
    	runSplitter("fromtxt", "greedy");
    }
    
    public static Application initHardcoded() {
        Application app = new Application();
        app.addUser("Alice");
        app.addUser("Bob");
        app.addUser("Charlie");
        app.addUser("David");
        app.printUsers();
        app.pay("Alice", 100, "train-ticket");
        app.pay("Charlie", 50, "groceries");
        app.pay("David", 10, "ice-cream");
        return app;
    }
    
    public static Application initFromTxt() throws FileNotFoundException {
    	Scanner sc = new Scanner(new File("payments.txt"));
    	int lineCount = 0;
    	Application app = new Application();
    	while (sc.hasNextLine()) {
    		String line = sc.nextLine();
    		if (lineCount == 0) {
    			String[] names = line.split(",");
    			for (String name: names) {
    				app.addUser(name);
    			}
    			app.printUsers();
    		} else {
    			String[] fields = line.split(" ");
    			String payer = fields[0];
    			double value = Double.parseDouble(fields[2]);
    			String comment = fields[3];
    			try {
    				String[] payees = fields[4].split(",");
    				app.paySubgroup(payer, value, comment, payees);
    			}
    			catch (Exception e) {
        			app.pay(payer, value, comment);
    			}
    		}
    		lineCount++;
    	}
    	return app;
    }
}

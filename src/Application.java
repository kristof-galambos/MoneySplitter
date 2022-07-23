import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class Application {
    private ArrayList<String> users = new ArrayList<String>();
    private ArrayList<Double> balances = new ArrayList<Double>();
    private double totalPaid = 0;
    private int userCounter = 0;
    private HashMap<Integer, String> index2name = new HashMap();
    private HashMap<String, Integer> name2index = new HashMap();
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public Application() {}
    
    public Application(String[] names) {
    	for (String name: names) {
    		users.add(name);
    		balances.add(0.0);
    		index2name.put(userCounter, name);
    		name2index.put(name, userCounter);
    		userCounter++;
    	}
    }
    
    public void addUser(String name) {
        users.add(name);
        balances.add(0.0);
		index2name.put(userCounter, name);
		name2index.put(name, userCounter);
		userCounter++;
    }

    public void printUsers() {
        System.out.print("Group initialised with users: ");
        for (int i=0; i<users.size(); i++) {
            if (i!=users.size()-1) {
                System.out.print(users.get(i) + ", ");
            }
            else {
                System.out.print(users.get(i) + ".");
            }
        }
        System.out.println();
    }

    public void printBalances() {
        System.out.println("The balances before splitting are the following:");
        for (int i=0; i<users.size(); i++) {
            System.out.println(String.valueOf(users.get(i)) + ": " + df.format(balances.get(i)));
        }
    }

    public void printTotalPaid() {
        System.out.println("The total amount spent by the whole group: " + String.valueOf(totalPaid));
    }

    public void pay(String user, double amount, String comment) {
        System.out.println(user + " paid " + String.valueOf(amount) + " for " + comment + " for the group");
        totalPaid += amount;
        int index = users.indexOf(user);
        balances.set(index, balances.get(index) + amount);
        for (int i=0; i<users.size(); i++) {
            balances.set(i, balances.get(i) - amount / users.size());
        }
    }
    
    public void paySubgroup(String payer, double amount, String comment, String[] payees) {
        System.out.print(payer + " paid " +String.valueOf(amount) + " for " + comment + " for the subgroup ");
        for (int i=0; i<payees.length; i++) {
            if (i!=payees.length-1) {
                System.out.print(payees[i] + ",");
            }
            else {
                System.out.print(payees[i]);
            }
        }
        System.out.println();
        
        totalPaid += amount;
        int index = name2index.get(payer);
        balances.set(index, balances.get(index) + amount);
        for (int i=0; i<payees.length; i++) {
        	int j = name2index.get(payees[i]);
            balances.set(j, balances.get(j) - amount / payees.length);
        }
    }
    
    public void applyCorrectionFactors(String[] correctionUsers, double[] factors) {
    	System.out.print("Applying correction factor " + String.valueOf(factors[0]) + " for " + correctionUsers[0]);
    	int myIndex = users.indexOf(correctionUsers[0]);
    	double oldBalance = balances.get(myIndex);
    	double newBalance = oldBalance * factors[0];
    	//double diff = oldBalance - newBalance;
    	double diff = newBalance - oldBalance;
    	double unit = diff / (users.size() - 1);
    	for (int i = 0; i<balances.size(); i++) {
    		if (i==myIndex) {
    			continue;
    		}
    		balances.set(i, balances.get(i) - unit);
    	}
    	balances.set(myIndex, newBalance);
    	
    }

    public void split() {
        int numberOfNegatives = 0;
        for (double balance: balances) {
            if (balance < 0) {
                numberOfNegatives++;
            }
        }
        int[] sortedIds = new int[users.size()];
        double[] balancesCopy = new double[users.size()];
        for (int i=0; i<users.size(); i++) {
            balancesCopy[i] = balances.get(i);
        }
        double UPPER_CAP = 9999999999.9;
        for (int i=0; i<users.size(); i++) {
            double min = UPPER_CAP;
            int minIndex = 0;
            for (int j=0; j<users.size(); j++) {
                if (balancesCopy[j] < min) {
                    min = balancesCopy[j];
                    minIndex = j;
                }
            }
            sortedIds[i] = minIndex;
            balancesCopy[minIndex] = UPPER_CAP;
        }
        int[] sortedNegatives = new int[numberOfNegatives];
        int[] sortedPositives = new int[users.size() - numberOfNegatives];
        for (int i=0; i<numberOfNegatives; i++) {
            sortedNegatives[i] = sortedIds[i];
        }
        for (int i=0; i<users.size() - numberOfNegatives; i++) {
            sortedPositives[i] = sortedIds[numberOfNegatives+i];
        }
        HashMap<Integer, Integer> id2sortedId = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> sortedId2id = new HashMap<Integer, Integer>();
        for (int i: sortedIds) {
            id2sortedId.put(i, sortedIds[i]);
            sortedId2id.put(sortedIds[i], i);
        }
        double[] balancesChanging = new double[users.size()];
        
        for (int i=0; i<numberOfNegatives; i++) {
            balancesChanging[i] = balances.get(sortedNegatives[i]);
        }
        for (int i=0; i<users.size() - numberOfNegatives; i++) {
            balancesChanging[numberOfNegatives+i] = balances.get(sortedPositives[i]);
        }
        HashMap<int[], Double> transactions = new HashMap<int[], Double>();
        for (int i=0; i<numberOfNegatives; i++) {
            for (int j = 0; j < users.size() - numberOfNegatives; j++) {
                if (i==numberOfNegatives+j) {
                    continue;
                }
                if (balancesChanging[numberOfNegatives + j] > 0) {
                    int[] direction = new int[] {id2sortedId.get(i), id2sortedId.get(numberOfNegatives+j)};
                    if (-balancesChanging[i] <= balancesChanging[numberOfNegatives + j]) {
                    	if (balancesChanging[i] == 0.0) {
                    		continue;
                    	}
                        transactions.put(direction, -balancesChanging[i]);
                        System.out.println(users.get(id2sortedId.get(i)) + " pays " + users.get(id2sortedId.get(numberOfNegatives+j)) + " the amount of " + df.format(-balancesChanging[i]));
                        balancesChanging[numberOfNegatives + j] += balancesChanging[i];
                        balancesChanging[i] = 0;
                    }
                    else {
                        transactions.put(direction, balancesChanging[numberOfNegatives + j]);
                        System.out.println(users.get(id2sortedId.get(i)) + " pays " + users.get(id2sortedId.get(numberOfNegatives+j)) + " the amount of " + df.format(balancesChanging[numberOfNegatives + j]));
                        balancesChanging[i] += balancesChanging[numberOfNegatives + j];
                        balancesChanging[numberOfNegatives + j] = 0;
                    }
                }
            }
        }
    }
    
    public void splitGreedy() {
    	double TOLERANCE = 0.1;
    	int aCounter = 0;
        HashMap<int[], Double> transactions = new HashMap<int[], Double>();
    	while (true) {
	        int numberOfNegatives = 0;
	        for (double balance: balances) {
	            if (balance < 0) {
	                numberOfNegatives++;
	            }
	        }
	        int[] sortedIds = new int[users.size()];
	        double[] balancesCopy = new double[users.size()];
	        for (int i=0; i<users.size(); i++) {
	            balancesCopy[i] = balances.get(i);
	        }
	        double UPPER_CAP = 9999999999.9;
	        for (int i=0; i<users.size(); i++) {
	            double min = UPPER_CAP;
	            int minIndex = 0;
	            for (int j=0; j<users.size(); j++) {
	                if (balancesCopy[j] < min) {
	                    min = balancesCopy[j];
	                    minIndex = j;
	                }
	            }
	            sortedIds[i] = minIndex;
	            balancesCopy[minIndex] = UPPER_CAP;
	        }
	        int[] sortedNegatives = new int[numberOfNegatives];
	        int[] sortedPositives = new int[users.size() - numberOfNegatives];
	        for (int i=0; i<numberOfNegatives; i++) {
	            sortedNegatives[i] = sortedIds[i];
	        }
	        for (int i=0; i<users.size() - numberOfNegatives; i++) {
	            sortedPositives[i] = sortedIds[numberOfNegatives+i];
	        }
	        HashMap<Integer, Integer> id2sortedId = new HashMap<Integer, Integer>();
	        HashMap<Integer, Integer> sortedId2id = new HashMap<Integer, Integer>();
	        for (int i: sortedIds) {
	            id2sortedId.put(i, sortedIds[i]);
	            sortedId2id.put(sortedIds[i], i);
	        }
	        double[] balancesChanging = new double[users.size()];
	        for (int i=0; i<numberOfNegatives; i++) {
	        	balancesChanging[i] = balances.get(sortedNegatives[i]);
	        }
	        for (int i=0; i<users.size() - numberOfNegatives; i++) {
	            balancesChanging[numberOfNegatives+i] = balances.get(sortedPositives[i]);
	        }
	        boolean wannaBreak = true;
	        for (int i=0; i<users.size(); i++) {
	        	if (balancesChanging[i] > TOLERANCE) {
	        		wannaBreak = false;
	        	}
	        }
	        if (wannaBreak) {
	        	break;
	        }
	        double largestNegative = balancesChanging[0];
	        double largestPositive = balancesChanging[balancesChanging.length-1];
	        if (-largestNegative > largestPositive) {
	        	int payerId = id2sortedId.get(0);
	        	int payeeId = id2sortedId.get(balancesChanging.length-1);
	        	int[] direction = new int[] {payerId, payeeId};
	        	transactions.put(direction, balancesChanging[balancesChanging.length-1]);
	        	System.out.println(users.get(payerId) + " pays " + users.get(payeeId) + " the amount of " + df.format(balancesChanging[balancesChanging.length-1]));
		        balancesChanging[0] += largestPositive;
	        	balancesChanging[balancesChanging.length-1] = 0;
	        	balances.set(id2sortedId.get(0), balances.get(id2sortedId.get(0)) + largestPositive);
	        	balances.set(id2sortedId.get(balancesChanging.length-1), 0.0);
	        }
	        else {
	        	int payerId = id2sortedId.get(0);
	        	int payeeId = id2sortedId.get(balancesChanging.length-1);
	        	int[] direction = new int[] {payerId, payeeId};
	        	transactions.put(direction, -balancesChanging[0]);
	        	System.out.println(users.get(payerId) + " pays " + users.get(payeeId) + " the amount of " + df.format(-balancesChanging[0]));
		        balancesChanging[balancesChanging.length-1] += largestNegative;
		        balancesChanging[0] = 0;
		        balances.set(id2sortedId.get(0), 0.0);
		        balances.set(id2sortedId.get(balancesChanging.length-1), balances.get(id2sortedId.get(balancesChanging.length-1)) + largestNegative);
	        }
    		aCounter++;
    		if (aCounter == 10000) { // avoid infinite loops
    			break;
    		}
    	}
    }
}

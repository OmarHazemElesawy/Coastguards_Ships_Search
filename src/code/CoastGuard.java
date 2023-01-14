package code;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;

public class CoastGuard extends searchProblem {
	static int totalPassengers = 0;
	static int grid_width = 0;
	static int grid_height = 0;
	static int maxCoastGuardCapacity ;
	static boolean rescuedPassengers = false;
	static int blackBoxes = 0;
	static int[] damaged2= {0};
	static int totalDeath = 0;
	static HashSet<String> repeatedStates= new HashSet<>();
	
    public static String genGrid() {
        int m = (int) (Math.random() * ((15 - 5) + 1) + 5);
        int n = (int) (Math.random() * ((15 - 5) + 1) + 5);
        String[][] grid = new String[m][n];
        int passengers = (int) (Math.random() * ((100 - 30) + 1) + 30);
        int guard_x = (int) (Math.random() * m);
        int guard_y = (int) (Math.random() * n);
        grid[guard_x][guard_y] = "G";
        int num_station = (int) (Math.random() * (((n * m + 1) - 2)) + 1);
        int remaining = n * m - num_station - 1;
        String stations = "";
        String ships = "";
        for (int i = 1; i <= num_station; i++) {
            int station_x = (int) (Math.random() * m);
            int station_y = (int) (Math.random() * n);
            while (grid[station_x][station_y] != null) {
                station_x = (int) (Math.random() * m);
                station_y = (int) (Math.random() * n);
            }
            grid[station_x][station_y] = "ST" + i;
            stations += "," + station_x + "," + station_y;
        }
        int num_ship = (int) (Math.random() * (remaining + 1) + 1);
        for (int i = 1; i <= num_ship; i++) {
            int capacity = (int) (Math.random() * ((100 - 1) + 1) + 1);
            totalPassengers= totalPassengers+ capacity;
            int ship_x = (int) (Math.random() * m);
            int ship_y = (int) (Math.random() * n);
            while (grid[ship_x][ship_y] != null) {
                ship_x = (int) (Math.random() * m);
                ship_y = (int) (Math.random() * n);
            }
            grid[ship_x][ship_y] = "SH" + i;
            ships += "," + ship_x + "," + ship_y + ',' + capacity;

        }
        return "" + n + "," + m + ";" + passengers + ";" + guard_x + "," + guard_y
                + ";" + stations.substring(1) + ";" + ships.substring(1) + ";";
    }
    public static String solve(String Grid, String strategy, boolean visualize) {
        String solved;
        String[][] grid = ConstructGrid(Grid);
        switch(strategy){
            case "BF" :solved = bfs(grid,visualize);break;
            case "DF" :solved = dfs(grid,visualize);break;
            case "ID" :solved = iterative(grid,visualize);break;
            case "GR1" :solved = greedy1(grid,visualize);break;
            case "GR2" :solved = greedy2(grid,visualize);break;
            case "AS1" :solved = aStar1(grid,visualize);break;
            case "AS2" :solved = aStar2(grid,visualize);break;
            default: solved = "Invalid search strategy!";break;
        }

        return solved;

    }
    public static String bfs(String[][]grid ,boolean visualize){
    	Queue<Node> searchTree = new LinkedList<Node>();
    	repeatedStates.clear();
    	Node root = new Node(grid , null , "N/A" , 0,0,goalTest(grid),0,0);
    	searchTree.add(root);
		String rootString = gridToString(grid);
		String plan = "";
		repeatedStates.add(rootString);
    	int nodesExpanded=0;
    	int[] damge= {0};
    	for(int i=0;!searchTree.peek().goal ;i++) {
    		nodesExpanded++;
    		String guardPosition = findCoastGuard(searchTree.peek().state);
    		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
    		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
    		if(y_guard>0) {
				String[][] tmp = deepCopy(searchTree.peek().state);
				int d=searchTree.peek().deathPeople;
				int[] deaths = {d};
				int retrieved = searchTree.peek().retrievedBoxes;
    			String[][] resGrid= moveLeft(tmp);
    			resGrid=expirePassenger(resGrid,deaths);
    			resGrid=damageBox(resGrid,damge);
    			if(!checkRepeated(resGrid)) {  // for eliminating the repeated nodes in the search tree
    			Node node1 = new Node(resGrid ,searchTree.peek(), "left", searchTree.peek().depth+1,0,goalTest(resGrid),deaths[0],retrieved);//change grid after moving left		
    			searchTree.add(node1);
				String resGridString = gridToString(resGrid);
				repeatedStates.add(resGridString);
    		}
    		}
    		if(y_guard<grid_width-1) {
				String[][] tmp = deepCopy(searchTree.peek().state);
				int d=searchTree.peek().deathPeople;
				int[] deaths = {d};
				int retrieved = searchTree.peek().retrievedBoxes;
				String[][] resGrid= moveRight(tmp);
    			resGrid=expirePassenger(resGrid,deaths);
    			resGrid=damageBox(resGrid,damge);
    			if(!checkRepeated(resGrid)) {
    			Node node2 = new Node(resGrid ,searchTree.peek(), "right", searchTree.peek().depth+1,0,goalTest(resGrid),deaths[0],retrieved);//change grid after moving right	
    			searchTree.add(node2);
				String resGridString = gridToString(resGrid);
				repeatedStates.add(resGridString);
    		}
    		}
    		if(x_guard<grid_height-1) {
				String[][] tmp = deepCopy(searchTree.peek().state);
				int d=searchTree.peek().deathPeople;
				int[] deaths = {d};
				int retrieved = searchTree.peek().retrievedBoxes;
				String[][] resGrid= moveDown(tmp);
    			resGrid=expirePassenger(resGrid,deaths);
    			resGrid=damageBox(resGrid,damge);
    			if(!checkRepeated(resGrid)) {
    			Node node3 = new Node(resGrid ,searchTree.peek(), "down", searchTree.peek().depth+1 ,0,goalTest(resGrid),deaths[0],retrieved);//change grid after moving down	
    			searchTree.add(node3);
				String resGridString = gridToString(resGrid);
				repeatedStates.add(resGridString);
    			}
    		}
    		if(x_guard>0) {
				String[][] tmp = deepCopy(searchTree.peek().state);
				int d=searchTree.peek().deathPeople;
				int[] deaths = {d};
				int retrieved = searchTree.peek().retrievedBoxes;
				String[][] resGrid= moveUp(tmp);
    			resGrid=expirePassenger(resGrid,deaths);
    			resGrid=damageBox(resGrid,damge);
    			if(!checkRepeated(resGrid)) {
    			Node node4 = new Node(resGrid ,searchTree.peek(), "up", searchTree.peek().depth+1,0,goalTest(resGrid),deaths[0],retrieved);//change grid after movind up
    			searchTree.add(node4);
				String resGridString = gridToString(resGrid);
				repeatedStates.add(resGridString);
    		}
    		}
    		if(searchTree.peek().state[x_guard][y_guard]!=null&&searchTree.peek().state[x_guard][y_guard].contains("SH")) {
    	
				String[][] tmp = deepCopy(searchTree.peek().state);
				int d=searchTree.peek().deathPeople;
				int[] deaths = {d};
				int retrieved = searchTree.peek().retrievedBoxes;
				int cap =getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap>0) {
				String[][] resGrid= pickPassengers(tmp);
    			resGrid=expirePassenger(resGrid,deaths);
    			resGrid=damageBox(resGrid,damge);
    			if(!checkRepeated(resGrid)) {
    			Node node5 = new Node(resGrid ,searchTree.peek(), "pickup", searchTree.peek().depth+1,0,goalTest(resGrid),deaths[0],retrieved);//change grid after picking
    			searchTree.add(node5);
				String resGridString = gridToString(resGrid);
				repeatedStates.add(resGridString);
				}
				}
    		}
    		if(searchTree.peek().state[x_guard][y_guard]!=null&&searchTree.peek().state[x_guard][y_guard].contains("ST")) {
    			String[][] tmp = deepCopy(searchTree.peek().state);
				int d=searchTree.peek().deathPeople;
				int[] deaths = {d};
				int retrieved = searchTree.peek().retrievedBoxes;
    			int cap  = getNumberOfRemainCapcityCoastGuard(tmp);
    			if(cap<maxCoastGuardCapacity) {
				String[][] resGrid= dropPassengers(tmp);
    			resGrid=expirePassenger(resGrid,deaths);
    			resGrid=damageBox(resGrid,damge);
    			if(!checkRepeated(resGrid)) {
    			Node node6 = new Node(resGrid ,searchTree.peek(), "drop", searchTree.peek().depth+1,0,goalTest(resGrid),deaths[0],retrieved);//change grid after dropping	
    			searchTree.add(node6);
				String resGridString = gridToString(resGrid);
				repeatedStates.add(resGridString);			
    			}
    		}
    		}
    		if(searchTree.peek().state[x_guard][y_guard]!=null&&searchTree.peek().state[x_guard][y_guard].contains("W")) {
				String[][] tmp = deepCopy(searchTree.peek().state);
				int d=searchTree.peek().deathPeople;
				int[] deaths = {d};
				int retrieved = searchTree.peek().retrievedBoxes;
				retrieved++;
				String[][] resGrid= retrieveBox(tmp);
    			resGrid=expirePassenger(resGrid,deaths);
    			resGrid=damageBox(resGrid,damge);
    			if(!checkRepeated(resGrid)) {
    			Node node7 = new Node(resGrid ,searchTree.peek(), "retrieve",searchTree.peek().depth+1,0,goalTest(resGrid),deaths[0],retrieved);//change grid after dropping
    			searchTree.add(node7);
				String resGridString = gridToString(resGrid);
				repeatedStates.add(resGridString);
    		}
    		}
    		searchTree.remove();
    		}
    	Node node= searchTree.peek();
		while(node.operator != "N/A"){
			plan = node.operator +"," + plan;
			if(visualize) {
			printGrid(node.state);
			System.out.println("-----------------------------------");
			}
			node = node.parent;
		}

		String result = plan.substring(0, plan.length() - 1) + ";" + searchTree.peek().deathPeople + ";" + searchTree.peek().retrievedBoxes + ";" + nodesExpanded;
		searchTree.clear();
		repeatedStates.clear();
		return result;
		
     }
    public static String dfs(String[][] grid ,boolean visualize){
		String result = ""; //plan;deaths;retrieved;nodes
        Stack<Node> searchTree = new Stack<Node>();
		Node root = new Node(grid , null , "N/A" , 0,0,goalTest(grid),0,0);
		searchTree.push(root);
		String rootString = gridToString(grid);
		repeatedStates.add(rootString);
		String plan = "";
		int nodes = 0;
		int[] damge= {0};
		while(!goalTest(searchTree.peek().state) ) {
			Node current = searchTree.pop();
			String guardPosition = findCoastGuard(current.state);
			int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
			int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
			if(y_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid = moveLeft(tmp);
				resGrid = expirePassenger(resGrid,deaths);
				resGrid = damageBox(resGrid,damge);
				if(!checkRepeated(resGrid)) {
						Node node1 = new Node(resGrid, current, "left", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after moving left
						searchTree.push(node1);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			if(y_guard<grid_width-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveRight(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damge);
				if(!checkRepeated(resGrid)) {
					Node node2 = new Node(resGrid, current, "right", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after moving right
					searchTree.add(node2);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard<grid_height-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveDown(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node3 = new Node(resGrid, current, "down", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after moving down
					searchTree.add(node3);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveUp(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node4 = new Node(resGrid, current, "up", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after movind up
					searchTree.add(node4);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(current.state[x_guard][y_guard] != null && current.state[x_guard][y_guard].contains("SH")) {

				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap>0) {
					String[][] resGrid= pickPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node5 = new Node(resGrid, current, "pickup", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after picking
						searchTree.add(node5);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("ST")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap  = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap<maxCoastGuardCapacity) {
					String[][] resGrid= dropPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node6 = new Node(resGrid, current, "drop", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after dropping
						searchTree.add(node6);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("W")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= retrieveBox(tmp);
				retrieved++;
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node7 = new Node(resGrid, current, "retrieve", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after dropping
					searchTree.add(node7);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
					retrieved++;
				}
			}
			nodes++;
		}
		Node node = searchTree.peek();
		plan = "";
		while(node.operator != "N/A"){
			plan = node.operator +"," + plan;
			if(visualize) {
			printGrid(node.state);
			System.out.println("-----------------------------------");
			}
			node = node.parent;
		}
		result = plan.substring(0, plan.length() - 1) + ";" + searchTree.peek().deathPeople + ";" + searchTree.peek().retrievedBoxes + ";" + nodes;
		searchTree.clear();
		repeatedStates.clear();
		return result;
    }
    public static String iterative(String[][] grid,boolean visualize){
    	Stack<Node> searchTree = new Stack<Node>();
		Node root = new Node(grid , null , "N/A" , 0,0,goalTest(grid),0,0);
		repeatedStates.clear();
		String rootString;
		String plan = "";
		int nodes = 0;
		int depth=0;
		while(searchTree.isEmpty()) {
			repeatedStates.clear();
			searchTree.push(root);
			rootString = gridToString(grid);
			repeatedStates.add(rootString);
		for(int i=0;!searchTree.isEmpty()&&!goalTest(searchTree.peek().state);i++) {			
			Node current = searchTree.pop();
			String guardPosition = findCoastGuard(current.state);
			int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
			int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
			if(current.depth<depth) {
			if(y_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid = moveLeft(tmp);
				resGrid = expirePassenger(resGrid,deaths);
				resGrid = damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
						Node node1 = new Node(resGrid, current, "left", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after moving left
						searchTree.push(node1);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			if(y_guard<grid_width-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveRight(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node2 = new Node(resGrid, current, "right", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after moving right
					searchTree.add(node2);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard<grid_height-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveDown(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node3 = new Node(resGrid, current, "down", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after moving down
					searchTree.add(node3);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveUp(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node4 = new Node(resGrid, current, "up", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after movind up
					searchTree.add(node4);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(current.state[x_guard][y_guard] != null && current.state[x_guard][y_guard].contains("SH")) {

				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap>0) {
					String[][] resGrid= pickPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node5 = new Node(resGrid, current, "pickup", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after picking
						searchTree.add(node5);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("ST")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap  = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap<maxCoastGuardCapacity) {
					String[][] resGrid= dropPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node6 = new Node(resGrid, current, "drop", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after dropping
						searchTree.add(node6);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("W")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= retrieveBox(tmp);
				retrieved++;
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node7 = new Node(resGrid, current, "retrieve", current.depth + 1, 0, goalTest(resGrid),deaths[0],retrieved);//change grid after dropping
					searchTree.add(node7);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			}
			nodes++;
		}
		depth++;

		}
		Node node = searchTree.peek();
		while(node.operator != "N/A"){
			plan = node.operator +"," + plan;
			if(visualize) {
			printGrid(node.state);
			System.out.println("-----------------------------------");
			}
			node = node.parent;
		}
		String result = plan.substring(0, plan.length() - 1) + ";" + searchTree.peek().deathPeople + ";" + searchTree.peek().retrievedBoxes + ";" + nodes;
		return result;
    }
    public static String greedy1(String[][] grid,boolean visualize){
    	ArrayList<Node> searchTree = new ArrayList<>();
		repeatedStates.clear();
		int nodes=0;
		Node root = new Node(grid , null , "N/A" , 0,0,goalTest(grid),0,0, predictDeath(grid));
		searchTree.add(root);
		String rootString = gridToString(grid);
		repeatedStates.add(rootString);
		for(int i=0;!searchTree.get(0).goal ;i++) {
			Node current = searchTree.get(0);
			searchTree.remove(0);
			String guardPosition = findCoastGuard(current.state);
			int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
			int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
			if(y_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveLeft(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node1 = new Node(resGrid ,current, "left", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDeath(resGrid));//change grid after moving left
					searchTree.add(node1);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(y_guard<grid_width-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveRight(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node2 = new Node(resGrid ,current, "right", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDeath(resGrid));//change grid after moving right
					searchTree.add(node2);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard<grid_height-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveDown(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node3 = new Node(resGrid ,current, "down", current.depth+1 ,0,goalTest(resGrid),deaths[0],retrieved,predictDeath(resGrid));//change grid after moving down
					searchTree.add(node3);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard>0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveUp(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node4 = new Node(resGrid ,current, "up", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDeath(resGrid));//change grid after movind up
					searchTree.add(node4);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("SH")) {

				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap =getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap>0) {
					String[][] resGrid= pickPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node5 = new Node(resGrid ,current, "pickup", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDeath(resGrid));//change grid after picking
						searchTree.add(node5);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("ST")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap  = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap<maxCoastGuardCapacity) {
					String[][] resGrid= dropPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node6 = new Node(resGrid ,current, "drop", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDeath(resGrid));//change grid after dropping
						searchTree.add(node6);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("W")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				retrieved++;
				String[][] resGrid= retrieveBox(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node7 = new Node(resGrid ,current, "retrieve" ,current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDeath(resGrid));//change grid after dropping
					searchTree.add(node7);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			nodes++;
			searchTree.sort((Node n1, Node n2) -> Integer.compare(n1.heuristic, n2.heuristic));
		}
		Node node = searchTree.get(0);
		String plan="";
		while(node.operator != "N/A"){
			plan = node.operator +"," + plan;
			if(visualize) {
			printGrid(node.state);
			System.out.println("-----------------------------------");
			}
			node = node.parent;
		}
		String result = plan.substring(0, plan.length() - 1) + ";" + searchTree.get(0).deathPeople + ";" + searchTree.get(0).retrievedBoxes + ";" + nodes;
		return result;
    }
    public static String greedy2(String[][] grid,boolean visualize){
		ArrayList<Node> searchTree = new ArrayList<>();
		repeatedStates.clear();
		int nodes=0;
		Node root = new Node(grid , null , "N/A" , 0,0,goalTest(grid),0,0, predictDeath(grid));
		searchTree.add(root);
		String rootString = gridToString(grid);
		repeatedStates.add(rootString);
		for(int i=0;!searchTree.get(0).goal ;i++) {
			Node current = searchTree.get(0);
			searchTree.remove(0);
			String guardPosition = findCoastGuard(current.state);
			int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
			int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
			if(y_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveLeft(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node1 = new Node(resGrid ,current, "left", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDamage(resGrid));//change grid after moving left
					searchTree.add(node1);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(y_guard<grid_width-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveRight(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node2 = new Node(resGrid ,current, "right", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDamage(resGrid));//change grid after moving right
					searchTree.add(node2);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard<grid_height-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveDown(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node3 = new Node(resGrid ,current, "down", current.depth+1 ,0,goalTest(resGrid),deaths[0],retrieved,predictDamage(resGrid));//change grid after moving down
					searchTree.add(node3);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard>0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveUp(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node4 = new Node(resGrid ,current, "up", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDamage(resGrid));//change grid after movind up
					searchTree.add(node4);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("SH")) {

				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap =getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap>0) {
					String[][] resGrid= pickPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node5 = new Node(resGrid ,current, "pickup", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDamage(resGrid));//change grid after picking
						searchTree.add(node5);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("ST")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				int cap  = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap<maxCoastGuardCapacity) {
					String[][] resGrid= dropPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,damaged2);
					if(!checkRepeated(resGrid)) {
						Node node6 = new Node(resGrid ,current, "drop", current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDamage(resGrid));//change grid after dropping
						searchTree.add(node6);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("W")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int retrieved = current.retrievedBoxes;
				retrieved++;
				String[][] resGrid= retrieveBox(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,damaged2);
				if(!checkRepeated(resGrid)) {
					Node node7 = new Node(resGrid ,current, "retrieve",current.depth+1,0,goalTest(resGrid),deaths[0],retrieved,predictDamage(resGrid));//change grid after dropping
					searchTree.add(node7);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			nodes++;
			searchTree.sort((Node n1, Node n2) -> Integer.compare(n1.heuristic, n2.heuristic));
		}
		Node node = searchTree.get(0);
		String plan="";
		while(node.operator != "N/A"){
			plan = node.operator +"," + plan;
			if(visualize) {
			printGrid(node.state);
			System.out.println("-----------------------------------");
			}
			node = node.parent;
		}
		String result = plan.substring(0, plan.length() - 1) + ";" + searchTree.get(0).deathPeople + ";" + searchTree.get(0).retrievedBoxes + ";" + nodes;
		return result;
    	}
    public static String aStar1(String[][] grid,boolean visualize){
    	ArrayList<Node> searchTree = new ArrayList<>();
    	int nodes=0;
		repeatedStates.clear();
		Node root = new Node(grid , null , "N/A" , 0,0,goalTest(grid),0,0,0, predictDeath(grid),0);
		searchTree.add(root);
		String rootString = gridToString(grid);
		repeatedStates.add(rootString);
		for(int i=0;!searchTree.get(0).goal ;i++) {
			Node current = searchTree.get(0);
			searchTree.remove(0);
			String guardPosition = findCoastGuard(current.state);
			int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
			int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
			if(y_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveLeft(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node1 = new Node(resGrid ,current, "left", current.depth+1,calcualtePathCost(currentDamagedBoxesArray,deaths),goalTest(resGrid)
							,deaths[0],retrieved,currentDamagedBoxesArray[0],predictDeath(resGrid)
							,calculateTotalCost(calcualtePathCost(currentDamagedBoxesArray,deaths), predictDeath(resGrid)));//change grid after moving left
					searchTree.add(node1);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(y_guard<grid_width-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveRight(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node2 = new Node(resGrid ,current, "right", current.depth+1,calcualtePathCost(currentDamagedBoxesArray,deaths),goalTest(resGrid)
							,deaths[0],retrieved,currentDamagedBoxesArray[0],predictDeath(resGrid)
							,calculateTotalCost(calcualtePathCost(currentDamagedBoxesArray,deaths), predictDeath(resGrid)));//change grid after moving right
					searchTree.add(node2);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard<grid_height-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveDown(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node3 = new Node(resGrid ,current, "down", current.depth+1 
							,calcualtePathCost(currentDamagedBoxesArray,deaths),goalTest(resGrid)
							,deaths[0],retrieved,currentDamagedBoxesArray[0],predictDeath(resGrid)
							,calculateTotalCost(calcualtePathCost(currentDamagedBoxesArray,deaths), predictDeath(resGrid)));//change grid after moving down
					searchTree.add(node3);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard>0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveUp(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node4 = new Node(resGrid ,current, "up", current.depth+1,calcualtePathCost(currentDamagedBoxesArray,deaths),goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDeath(resGrid),
							calculateTotalCost(calcualtePathCost(currentDamagedBoxesArray,deaths), predictDeath(resGrid)));//change grid after movind up
					searchTree.add(node4);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("SH")) {

				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				int cap =getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap>0) {
					String[][] resGrid= pickPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,currentDamagedBoxesArray);
					if(!checkRepeated(resGrid)) {
						Node node5 = new Node(resGrid ,current, "pickup", current.depth+1,calcualtePathCost(currentDamagedBoxesArray,deaths),goalTest(resGrid)
								,deaths[0],retrieved,currentDamagedBoxesArray[0],predictDeath(resGrid)
								,calculateTotalCost(calcualtePathCost(currentDamagedBoxesArray,deaths), predictDeath(resGrid)));//change grid after picking
						searchTree.add(node5);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("ST")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				int cap  = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap<maxCoastGuardCapacity) {
					String[][] resGrid= dropPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,currentDamagedBoxesArray);
					if(!checkRepeated(resGrid)) {
						Node node6 = new Node(resGrid ,current, "drop", current.depth+1,calcualtePathCost(currentDamagedBoxesArray,deaths),goalTest(resGrid)
								,deaths[0],retrieved,currentDamagedBoxesArray[0],predictDeath(resGrid)
								,calculateTotalCost(calcualtePathCost(currentDamagedBoxesArray,deaths), predictDeath(resGrid)));//change grid after dropping
						searchTree.add(node6);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("W")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				
				retrieved++;
				String[][] resGrid= retrieveBox(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node7 = new Node(resGrid ,current, "retrieve",current.depth+1,calcualtePathCost(currentDamagedBoxesArray,deaths),goalTest(resGrid)
							,deaths[0],retrieved,currentDamagedBoxesArray[0],predictDeath(resGrid)
							,calculateTotalCost(calcualtePathCost(currentDamagedBoxesArray,deaths), predictDeath(resGrid)));//change grid after dropping
					searchTree.add(node7);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
					}
			}
			nodes++;
			searchTree.sort((Node n1, Node n2) -> Integer.compare(n1.pathCost_heuristic , n2.pathCost_heuristic));
		}
		Node node = searchTree.get(0);
		String plan="";
		while(node.operator != "N/A"){
			plan = node.operator +"," + plan;
			if(visualize) {
			printGrid(node.state);
			System.out.println("-----------------------------------");
			}
			node = node.parent;
		}
		String result = plan.substring(0, plan.length() - 1) + ";" + searchTree.get(0).deathPeople + ";" + searchTree.get(0).retrievedBoxes + ";" + nodes;
		return result;
    }
    public static String aStar2(String[][] grid,boolean visualize){
    	ArrayList<Node> searchTree = new ArrayList<>();
    	int []empty= {0,0};
    	int nodes=0;
		repeatedStates.clear();
		Node root = new Node(grid , null , "N/A" , 0,0,empty,goalTest(grid),0,0, predictDamage(grid),predictDamage(grid));
		searchTree.add(root);
		String rootString = gridToString(grid);
		repeatedStates.add(rootString);
		for(int i=0;!searchTree.get(0).goal ;i++) {
			Node current = searchTree.get(0);
			searchTree.remove(0);
			String guardPosition = findCoastGuard(current.state);
			int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
			int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
			if(y_guard > 0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveLeft(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node1 = new Node(resGrid ,current, "left", current.depth+1,calcualtePathCost(deaths,currentDamagedBoxesArray)
							,goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDamage(resGrid),
							calculateTotalCost(calcualtePathCost(deaths,currentDamagedBoxesArray),predictDamage(resGrid)));//change grid after moving left
					searchTree.add(node1);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(y_guard<grid_width-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveRight(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node2 = new Node(resGrid ,current, "right", current.depth+1,calcualtePathCost(deaths,currentDamagedBoxesArray)
							,goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDamage(resGrid),
							calculateTotalCost(calcualtePathCost(deaths,currentDamagedBoxesArray),predictDamage(resGrid)));//change grid after moving right
					searchTree.add(node2);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard<grid_height-1) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveDown(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node3 = new Node(resGrid ,current, "down", current.depth+1 ,calcualtePathCost(deaths,currentDamagedBoxesArray)
							,goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDamage(resGrid),
							calculateTotalCost(calcualtePathCost(deaths,currentDamagedBoxesArray),predictDamage(resGrid)));//change grid after moving down
					searchTree.add(node3);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(x_guard>0) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				String[][] resGrid= moveUp(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node4 = new Node(resGrid ,current, "up", current.depth+1,calcualtePathCost(deaths,currentDamagedBoxesArray)
							,goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDamage(resGrid),
							calculateTotalCost(calcualtePathCost(deaths,currentDamagedBoxesArray),predictDamage(resGrid)));//change grid after movind up
					searchTree.add(node4);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("SH")) {

				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				int cap =getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap>0) {
					String[][] resGrid= pickPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,currentDamagedBoxesArray);
					if(!checkRepeated(resGrid)) {
						Node node5 = new Node(resGrid ,current, "pickup", current.depth+1,calcualtePathCost(deaths,currentDamagedBoxesArray)
								,goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDamage(resGrid),
								calculateTotalCost(calcualtePathCost(deaths,currentDamagedBoxesArray),predictDamage(resGrid)));//change grid after picking
						searchTree.add(node5);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("ST")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				int cap  = getNumberOfRemainCapcityCoastGuard(tmp);
				if(cap<maxCoastGuardCapacity) {
					String[][] resGrid= dropPassengers(tmp);
					resGrid=expirePassenger(resGrid,deaths);
					resGrid=damageBox(resGrid,currentDamagedBoxesArray);
					if(!checkRepeated(resGrid)) {
						Node node6 = new Node(resGrid ,current, "drop", current.depth+1,calcualtePathCost(deaths,currentDamagedBoxesArray)
								,goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDamage(resGrid),
								calculateTotalCost(calcualtePathCost(deaths,currentDamagedBoxesArray),predictDamage(resGrid)));//change grid after dropping
						searchTree.add(node6);
						String resGridString = gridToString(resGrid);
						repeatedStates.add(resGridString);
					}
				}
			}
			if(current.state[x_guard][y_guard]!=null&&current.state[x_guard][y_guard].contains("W")) {
				String[][] tmp = deepCopy(current.state);
				int d=current.deathPeople;
				int[] deaths = {d};
				int currentDamagedBoxes=current.damagedBoxes;
				int[] currentDamagedBoxesArray= {currentDamagedBoxes};
				int retrieved = current.retrievedBoxes;
				retrieved++;
				String[][] resGrid= retrieveBox(tmp);
				resGrid=expirePassenger(resGrid,deaths);
				resGrid=damageBox(resGrid,currentDamagedBoxesArray);
				if(!checkRepeated(resGrid)) {
					Node node7 = new Node(resGrid ,current, "retrieve",current.depth+1,calcualtePathCost(deaths,currentDamagedBoxesArray)
							,goalTest(resGrid),deaths[0],retrieved,currentDamagedBoxesArray[0],predictDamage(resGrid),
							calculateTotalCost(calcualtePathCost(deaths,currentDamagedBoxesArray),predictDamage(resGrid)));//change grid after dropping
					searchTree.add(node7);
					String resGridString = gridToString(resGrid);
					repeatedStates.add(resGridString);
				}
			}
			nodes++;
			searchTree.sort((Node n1, Node n2) -> Integer.compare(n1.pathCost_heuristic, n2.pathCost_heuristic));
		}
		Node node = searchTree.get(0);
		String plan="";
		while(node.operator != "N/A"){
			plan = node.operator +"," + plan;
			if(visualize) {
			printGrid(node.state);
			System.out.println("-----------------------------------");
			}
			node = node.parent;
		}
		String result = plan.substring(0, plan.length() - 1) + ";" + searchTree.get(0).deathPeople + ";" + searchTree.get(0).retrievedBoxes + ";" + nodes;
		return result;
    }
    public static int calcualtePathCost(int [] x ,int[]y) {
    	int res=0;
    	res= x[0]+y[0];
    	return res;
    }
    public static int calculateTotalCost(int x, int y) {
    	int res=0;
    	res=x+y;
    	return res;
    }
    public static String[][] ConstructGrid(String GridStr) {
        String[] Split = GridStr.split(";", 0);

        String[] dimensions = Split[0].split(",", 0);
        int cols = Integer.parseInt(dimensions[0]);
        int rows = Integer.parseInt(dimensions[1]);
        grid_height=rows;
        grid_width=cols;
        maxCoastGuardCapacity= Integer.parseInt(Split[1]);
        int maxPassengers = Integer.parseInt(Split[1]);

        String[] guardPosSplit = Split[2].split(",", 0);
        int guardPosRow = Integer.parseInt(guardPosSplit[0]);
        int guardPosCol = Integer.parseInt(guardPosSplit[1]);

        String[] StationsPosAll = Split[3].split(",", 0);
        List<Integer> stationsPosRow = new ArrayList<Integer>();
        List<Integer> stationsPosCol = new ArrayList<Integer>();

        for (int i = 0; i < StationsPosAll.length; i++) {
            if (i % 2 == 0)
                stationsPosRow.add(Integer.parseInt(StationsPosAll[i]));
            else
                stationsPosCol.add(Integer.parseInt(StationsPosAll[i]));
        }

        String[] shipsPosAll = Split[4].split(",", 0);
        List<Integer> shipsPosRow = new ArrayList<Integer>();
        List<Integer> shipsPosCol = new ArrayList<Integer>();
        List<Integer> capacity = new ArrayList<Integer>();

        for (int i = 0; i < shipsPosAll.length; i += 3)
            shipsPosRow.add(Integer.parseInt(shipsPosAll[i]));

        for (int i = 1; i < shipsPosAll.length; i += 3)
            shipsPosCol.add(Integer.parseInt(shipsPosAll[i]));

        for (int i = 2; i < shipsPosAll.length; i += 3)
            capacity.add(Integer.parseInt(shipsPosAll[i]));

        String[][] resGrid = new String[rows][cols];
        resGrid[guardPosRow][guardPosCol] = "G" + maxPassengers;

        while (stationsPosRow.size() != 0) {
            resGrid[stationsPosRow.get(0)][stationsPosCol.get(0)] = "ST";
            stationsPosRow.remove(0);
            stationsPosCol.remove(0);
        }
        while (shipsPosRow.size() != 0) {
            resGrid[shipsPosRow.get(0)][shipsPosCol.get(0)] = "SH" + capacity.get(0);
            shipsPosRow.remove(0);
            shipsPosCol.remove(0);
            capacity.remove(0);
        }
        return resGrid;
    }
    public static String findCoastGuard(String[][] grid) {
    	String res="";
    	for(int i =0 ; i < grid.length; i++) {
    		for(int j = 0 ; j<grid[i].length;j++) {
    			if(grid[i][j]!=null&&grid[i][j].contains("G")) {
    				res = i+","+j;
    			}
    		}
    	}

    	return res;
    }
    public static String[][] moveLeft(String[][] grid){
    	String[][] res;
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
		String guard="";
		if(grid[x_guard][y_guard].contains(",")) {
		 guard = grid[x_guard][y_guard].split(",")[1];
		 grid[x_guard][y_guard]=grid[x_guard][y_guard].split(",")[0];
		}
		else {
		 guard = grid[x_guard][y_guard];
		 grid[x_guard][y_guard]=null;
		}
		if(grid[x_guard][y_guard-1]!=null)
		grid[x_guard][y_guard-1]=grid[x_guard][y_guard-1]+","+guard;
		else
			grid[x_guard][y_guard-1]=guard;
		res= grid;
    	return res;
    }
    public static String[][] moveRight(String[][] grid){
    	String[][] res;
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
		String guard="";
		if(grid[x_guard][y_guard].contains(",")) {
			 guard = grid[x_guard][y_guard].split(",")[1];
			 grid[x_guard][y_guard]=grid[x_guard][y_guard].split(",")[0];
			}
		else {
			 guard = grid[x_guard][y_guard];
			 grid[x_guard][y_guard]=null;
			}
		if(grid[x_guard][y_guard+1]!=null)
		grid[x_guard][y_guard+1]=grid[x_guard][y_guard+1]+","+guard;
		else
			grid[x_guard][y_guard+1]=guard;
		res= grid;
    	return res;
    }
    public static String[][] moveUp(String[][] grid){
    	String[][] res;
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
		String guard="";
		if(grid[x_guard][y_guard].contains(",")) {
			 guard = grid[x_guard][y_guard].split(",")[1];
			 grid[x_guard][y_guard]=grid[x_guard][y_guard].split(",")[0];
			}
		else {
			 guard = grid[x_guard][y_guard];
			 grid[x_guard][y_guard]=null;
			}
		if(grid[x_guard-1][y_guard]!=null)
		grid[x_guard-1][y_guard]=grid[x_guard-1][y_guard]+","+guard;
		else
			grid[x_guard-1][y_guard]=guard;
		res= grid;
    	return res;
    }
    public static String[][] moveDown(String[][] grid){
    	String[][] res;
    	
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
		String guard="";
		if(grid[x_guard][y_guard].contains(",")) {
			 guard = grid[x_guard][y_guard].split(",")[1];
			 grid[x_guard][y_guard]=grid[x_guard][y_guard].split(",")[0];
			}
		else {
			 guard = grid[x_guard][y_guard];
			 grid[x_guard][y_guard]=null;
			}
		if(grid[x_guard+1][y_guard]!=null)
		grid[x_guard+1][y_guard]=grid[x_guard+1][y_guard]+","+guard;
		else
			grid[x_guard+1][y_guard]=guard;
		res= grid;
    	return res;
    }
    public static int getNumberOfRemainCapcityCoastGuard(String [][] grid) {
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
		int res=0;
		if(grid[x_guard][y_guard].contains(",")) {
			String[] splitter = grid[x_guard][y_guard].split(",");
			String guardNum = splitter[1];
			if(guardNum.length()==2)
				res=Integer.parseInt(guardNum.charAt(1)+"");
			else if(guardNum.length()==3)
				res=Integer.parseInt(guardNum.charAt(1)+""+guardNum.charAt(2));
			else if(guardNum.length()==4)
				res=Integer.parseInt(guardNum.charAt(1)+""+guardNum.charAt(2)+guardNum.charAt(3));
		}else {
			if(grid[x_guard][y_guard].length()==2)
				res=Integer.parseInt(grid[x_guard][y_guard].charAt(1)+"");
			else if(grid[x_guard][y_guard].length()==3)
				res=Integer.parseInt(grid[x_guard][y_guard].charAt(1)+""+grid[x_guard][y_guard].charAt(2));
			else if(grid[x_guard][y_guard].length()==4)
				res=Integer.parseInt(grid[x_guard][y_guard].charAt(1)+""+grid[x_guard][y_guard].charAt(2)+grid[x_guard][y_guard].charAt(3));
		}
		return res;
    }
    public static String[][] pickPassengers(String [][] grid){
    	String[][] res;
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
		int passengerOnShip;
		passengerOnShip= Integer.parseInt((grid[x_guard][y_guard].split(",")[0]).substring(2));
		int cap = getNumberOfRemainCapcityCoastGuard(grid);
		
		if(cap>=passengerOnShip) {
			cap=cap-passengerOnShip;
			grid[x_guard][y_guard]="WD0,G"+cap;
			blackBoxes++;
		}
		else {
			passengerOnShip=passengerOnShip-cap;
			cap=0;
			grid[x_guard][y_guard]="SH"+passengerOnShip+",G"+cap;
		}
		res=grid;
    	return grid;
    }
    public static String[][] dropPassengers(String [][] grid){
    	String[][] res=grid;
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
		rescuedPassengers=false;
		grid[x_guard][y_guard]="ST,"+"G"+maxCoastGuardCapacity;
		res=grid;
		return res;
    }
    public static boolean goalTest(String [][] grid) {
		int cap = getNumberOfRemainCapcityCoastGuard(grid);
    	for(int i =0 ; i < grid.length; i++) {
    		for(int j = 0 ; j<grid[i].length;j++) {
				if(grid[i][j] != null) {
					if (cap != maxCoastGuardCapacity || grid[i][j].contains("SH") || grid[i][j].contains("W")) {
						return false;
					}
				}
    		}
    	}
    	return true;
    }
    public static String[][] expirePassenger(String[][] grid , int[] x){
    	String[][] res ;
		int cap = getNumberOfRemainCapcityCoastGuard(grid);
    	for(int i =0 ; i < grid.length; i++) {
    		for(int j = 0 ; j<grid[i].length;j++) {
    			if(grid[i][j]!=null&&grid[i][j].contains("SH")) {
    				if(grid[i][j].contains(",")) {
					String [] splitter=grid[i][j].split(",");
					String ship= splitter[0];
					int p = Integer.parseInt(ship.substring(2));
					p--;
					x[0]++;
					if(p>0) 
						grid[i][j]="SH"+p+","+splitter[1];
					else
						grid[i][j]="WD0"+","+splitter[1];
    				}
    				else {
    					String ship =grid[i][j];
    					int p = Integer.parseInt(ship.substring(2));
    					p--;
    					x[0]++;
    					if(p>0) 
    						grid[i][j]="SH"+p;
    					else
    						grid[i][j]="WD0";
    				}
    			}
    		}
    	}
    	
    	res=grid;
    	return res;
    }
    public static String[][] retrieveBox(String[][] grid){
    	String[][] res;
		int cap = getNumberOfRemainCapcityCoastGuard(grid);
    	String guardPosition=findCoastGuard(grid);
		int x_guard=Integer.parseInt(guardPosition.charAt(0)+"");
		int y_guard=Integer.parseInt(guardPosition.charAt(2)+"");
    	grid[x_guard][y_guard]="G"+cap;
    	blackBoxes--;
    	res=grid;
    	return res;
    }
    public static String[][] damageBox(String[][]grid, int [] damagedBoxes){
    	String[][] res;
		int cap = getNumberOfRemainCapcityCoastGuard(grid);
    	for(int i =0 ; i < grid.length; i++) {
    		for(int j = 0 ; j<grid[i].length;j++) {
    			if(grid[i][j]!=null&&grid[i][j].contains("WD")) {
    				if(grid[i][j].contains(",")) {
    					String[] splitter = grid[i][j].split(",");
    					int p = Integer.parseInt(splitter[0].substring(2));
    					p++;
    					if(p==20) {
    						grid[i][j]=splitter[1];
    						damagedBoxes[0]++;
    					}
    					else
    						grid[i][j]="WD"+p+","+splitter[1];
    				}
    				else {
    					int p = Integer.parseInt(grid[i][j].substring(2));
    					p++;
    					if(p==20) {
    						grid[i][j]=null;
    						damagedBoxes[0]++;
    					}
    					else
    						grid[i][j]="WD"+p;
    				}
    			}
    		}
    		}
    	res= grid;
    	return res;
    }
  
	public static String gridToString(String[][] grid) {
//		String res = ""; //guardPosition;guardCapacity;numOfShips;numOfPassengers;noOfWrecks;blackBoxHealth
//		String guardPosition = findCoastGuard(grid);
//		String guardCapacity = "";
//		int numOfShips = 0;
//		int numOfPassengers = 0;
//		int noOfWrecks = 0;
//		int blackBoxHealth = 0;
//		for(int i =0 ; i < grid.length; i++) {
//			for(int j = 0 ; j < grid[i].length; j++) {
//				if (grid[i][j] != null && grid[i][j].contains("G")) {
//					if(grid[i][j].contains(",")) {
//						guardCapacity = (grid[i][j].split(",")[1]).substring(1);
//					}else{
//						guardCapacity = grid[i][j].substring(1);
//					}
//				}
//				if(grid[i][j] != null && grid[i][j].contains("SH")){
//					if(grid[i][j].contains(",")) {
//						numOfPassengers += Integer.parseInt((grid[i][j].split(",")[0]).substring(2));
//					}else{
//						numOfPassengers += Integer.parseInt(grid[i][j].substring(2));
//					}
//					numOfShips++;
//				}
//				if(grid[i][j] != null && grid[i][j].contains("WD")){
//					if(grid[i][j].contains(",")) {
//						blackBoxHealth += Integer.parseInt((grid[i][j].split(",")[0]).substring(2));
//					}else{
//						blackBoxHealth += Integer.parseInt(grid[i][j].substring(2));
//					}
//					noOfWrecks++;
//				}
//			}
//		}
//		res = guardPosition + ";" + guardCapacity + ";" + numOfShips + ";" + numOfPassengers + ";" + noOfWrecks + ";" + blackBoxHealth;
//		return res;
		String res = "";
		for (String[] row : grid) {
			for (String element : row) {
					res += element;
			}
		}
		return res;
	}
	public static boolean checkRepeated(String[][] grid){
		String gridString = gridToString(grid);
		if(repeatedStates.contains(gridString))
			return true;
		return false;
	}
	public static int predictDeath(String [][] grid) {
		int totalDeaths = 0;
		String guardPosition = findCoastGuard(grid);
		int x_guard = Integer.parseInt(guardPosition.charAt(0) + "");
		int y_guard = Integer.parseInt(guardPosition.charAt(2) + "");
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				if (grid[i][j] != null && grid[i][j].contains("SH"))
					totalDeaths += Math.abs(j - y_guard) + Math.abs(i - x_guard);
			}
		}
		return totalDeaths;
	}
	public static int predictDamage(String [][] grid) {
		int totalDamage = 0;
		String guardPosition = findCoastGuard(grid);
		int x_guard = Integer.parseInt(guardPosition.charAt(0) + "");
		int y_guard = Integer.parseInt(guardPosition.charAt(2) + "");
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				if (grid[i][j] != null && grid[i][j].contains("WD"))
					totalDamage += Math.abs(j - y_guard) + Math.abs(i - x_guard);
			}
		}
		return totalDamage;
	}
	public static String[][] deepCopy(String[][] original) {
		if (original == null) {
			return null;
		}
		String[][] result = new String[original.length][];
		for (int i = 0; i < original.length; i++) {
			result[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return result;
	}
    public static void printGrid(String grid[][]) {
		for (String[] strings : grid) {
			for (String string : strings)
				System.out.print(string + " | ");
			System.out.println();
		}
    }

    public static void main(String[] args) throws IOException {
		String test = genGrid();
		Runtime rt = Runtime.getRuntime();
    	String grid0 = "7,5;40;2,3;3,6;1,1,10,4,5,90;";
		System.out.println(grid0);
        String[][] Grid = ConstructGrid(grid0);
        printGrid(Grid);
		System.out.println("\n");
		//calculating memory usage and time before running solve function
		long total_mem_before = rt.totalMemory();
		long free_mem_before = rt.freeMemory();
		long used_mem_before = (total_mem_before - free_mem_before);
		long nanoBefore = System.nanoTime();
		String result = solve(grid0, "ID", true);
		//calculating memory usage and time after running solve function
		long nanoAfter = System.nanoTime();
		long total_mem_after = rt.totalMemory();
		long free_mem_after = rt.freeMemory();
		long used_mem_after = (total_mem_after - free_mem_after);
		long time = nanoAfter - nanoBefore;
		System.out.println(result);
		System.out.println("Runtime: "+time/1_000_000_000.0 +" sec");
		System.out.println("Memory usage before: "+((double)used_mem_before/total_mem_before) * 100 + " %");
		System.out.println("Memory usage after: "+((double)used_mem_after/total_mem_after) * 100 + " %");
	}
}
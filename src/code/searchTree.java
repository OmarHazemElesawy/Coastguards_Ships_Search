package code;
abstract class searchTree {
}
class Node extends searchTree{
   public String[][] state;
   public Node parent;
   public String operator;
   public int depth;
   public int pathCost;
   public boolean goal;			// if it is a goal state or not
   public int deathPeople;			//to keep track the number of dead people in each state 
   public int damagedBoxes;			//to keep track the number of damaged boxes in each state
   public int retrievedBoxes;		// to keep track the number of retrieved boxes in every state
   public int[] arrayOfCost;
   public int heuristic;			// Value of the heuristic function
   public int pathCost_heuristic;	// Summation of heuristic value and the path cost for every node (A*)


    Node(){
        this.state = null;
        this.parent = null;
        this.operator = "";
        this.depth = -1;
        this.pathCost = -1;
        this.goal = false;
        this.deathPeople=0;
        this.retrievedBoxes=0;

    }

    Node(String[][] state, Node parent, String operator, int depth, int pathCost, boolean goal,int deathPeople,int retrievedBoxes){
        this.state = state;
        this.parent = parent;
        this.operator = operator;
        this.depth = depth;
        this.pathCost = pathCost;
        this.goal=goal;

        this.deathPeople=deathPeople;
        this.retrievedBoxes=retrievedBoxes;       		
    }
    Node(String[][] state, Node parent, String operator, int depth, int pathCost, boolean goal,int deathPeople,int retrievedBoxes,int damagedBoxes ,int heuristic,int pathCost_heuristic){
        this.state = state;
        this.parent = parent;
        this.operator = operator;
        this.depth = depth;
        this.pathCost = pathCost;
        this.goal=goal;
        this.deathPeople=deathPeople;
        this.damagedBoxes=damagedBoxes;
        this.retrievedBoxes=retrievedBoxes; 
        this.heuristic=heuristic;
        this. pathCost_heuristic= pathCost_heuristic;
    }
    Node(String[][] state, Node parent, String operator, int depth, int pathCost,int[] arrayOfCost, boolean goal,int deathPeople,int retrievedBoxes, int heuristic,int pathCost_heuristic){
        this.state = state;
        this.parent = parent;
        this.operator = operator;
        this.depth = depth;
        this.pathCost = pathCost;
        this.goal=goal;
        this.arrayOfCost=arrayOfCost;
        this.deathPeople=deathPeople;
        this.retrievedBoxes=retrievedBoxes; 
        this.heuristic=heuristic;
        this. pathCost_heuristic= pathCost_heuristic;
    }
    Node(String[][] state, Node parent, String operator, int depth, int pathCost, boolean goal,int deathPeople,int retrievedBoxes, int heuristic){
        this.state = state;
        this.parent = parent;
        this.operator = operator;
        this.depth = depth;
        this.pathCost = pathCost;
        this.goal=goal;
        this.deathPeople=deathPeople;
        this.retrievedBoxes=retrievedBoxes;
        this.heuristic = heuristic;
    }

}

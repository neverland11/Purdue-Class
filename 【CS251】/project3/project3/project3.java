import com.sun.javafx.scene.control.skin.VirtualFlow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Stack;


public class project3 {
	private int graphSize;
	private int[] color;
	Scanner s = new Scanner(System.in);
    ArrayList<Integer> ent = new ArrayList<Integer>(); //X partition
    ArrayList<Integer> ang = new ArrayList<Integer>(); //Y partition
    int dfn = 1; //for dfs


	public Graph createGraph(){
		/*TODO: Task 1.1 */
		int graphSize = s.nextInt();
		//if input is 0
		if(graphSize == 0){
			System.out.println("Warning: empty graph");
			return null;
		}

        System.out.println("Number of vertices: " + graphSize);
        graphSize++;
		s.nextLine();
		return new Graph(graphSize);

	}


	public boolean validateGraph(Graph g) {
		/*TODO: Task 1.2 */

        for (int n = 0; n < g.V() - 1; n++) {
            String input = s.nextLine();

            int pos = input.indexOf(':'); //: 's position
            int i = Integer.parseInt(input.substring(0, pos)); //change the number before : to int
            //a: between 1 and n
            if (i > g.V() || i < 1) {
                System.out.println("Error: vertex number out of range");
                return false;
            }

            String[] in = input.substring(pos + 1, input.length()).split(","); //split string after : by ,
            int[] v = new int[in.length];
            for (int j = 0; j < in.length; j++) { //change string[]in to int[]v,v stores numbers after :
                int temp = Integer.parseInt(in[j]);
                v[j] = temp;
            }

            //c:could not be same to each other and p,between 1 and n
            for (int j = 0; j < v.length; j++) {
                //System.out.println(v.length);
                //System.out.println(v[j]);
                if (v[j] > g.V() || v[j] < 1) {
                    System.out.println("Error: vertex number out of range");
                    return false;
                }
                if (v[j] == i) {
                    System.out.println("Error: illegal adjacency");
                    return false;
                }
                for (int k = 0; k < j; k++) {
                    if (v[j] == v[k]) {
                        System.out.println("Error: illegal adjacency");
                        return false;
                    }
                }

                int flag = 0; //not exist
                //if the number is connected
                Iterator<Integer> iter = g.adj(i).iterator();
                while (iter.hasNext()) {
                    int cmp = iter.next();
                    if (v[j] == cmp) {
                        flag = 1; //has existed
                        //break;
                        //System.out.println("flag = " + flag);
                    }
                }

                if (flag == 0) { //if not exist, connect
                    g.addEdge(i, v[j]); //connect i and v[j] (i: v[0],v[1]...)
                }
            }
        }

        //whether connected, use DFS
        //all vertex unmarked
        int[] mark = new int[g.V()];
        for (int i = 0; i < g.V(); i++) {
            mark[i] = 0;
        }
        dfs(g, 1, mark);//from 1 do dfs

        if (dfn != g.V()){
            System.out.println("Error: graph is not connected");
            return false;
        }


        //print
        if (g.V() < 11) {
            for (int j = 1; j < g.V(); j++) {
                int[] pri = new int[g.degree(j)];
                System.out.printf("%d:", j);
                Iterator<Integer> iter = g.adj(j).iterator();
                int count = 0;
                while (iter.hasNext()) {
                    pri[count] = iter.next();
                    count++;
                }
                Arrays.sort(pri);
                System.out.print(pri[0]);
                for (int k = 1; k < pri.length; k++) {
                    System.out.print("," + pri[k]);
                }
                System.out.println();
            }
        } else {
            System.out.println("Graph passes.");
        }
        return true;
    }

    //do dfs
    public void dfs(Graph g, int v, int[]mark){
        mark[v] = ++dfn;
        Iterator<Integer> iter = g.adj(v).iterator();
        while(iter.hasNext()){
            int i = iter.next();
            if(mark[i] == 0){
                dfs(g, i, mark);
            }
        }
    }



	public boolean checkBipartiteBfs(Graph g) {
		/*TODO: Task 2 */

        Queue<Integer> q = new Queue<Integer>();
        ent.add(1); //vertex 1 is entrepreneur
        q.enqueue(1);
        color = new int[g.V()]; //markers
        for(int i = 0; i < g.V(); i++){
            color[i] = 0;
        }

        color[1] = 1;
        while (!q.isEmpty()) {
            int i = q.dequeue();
            //System.out.println(i);
            Iterator<Integer> iter = g.adj(i).iterator(); //adj(i) stores the points connect to vertex i
            while (iter.hasNext()) {
                int num = iter.next();
                if (ent.contains(num) || ang.contains(num)) { //if is marked, skip
                    continue;
                }

                if(color[i] == 1){ //q.dequeue = 1, so i is an entrepreneur.
                    color[num] = 2; //mark these vertexes as ang
                    ang.add(num);//vertex connected to ent is angel
                    q.enqueue(num);
                }

                if(color[i] == 2){
                    color[num] = 1; //mark these vertexes as ent
                    ent.add(num);
                    q.enqueue(num);
                }
            }
        }

        for(int i = 0; i < ent.size(); i++){ //check ent list
            int tem = ent.get(i);
            Iterator<Integer> iter = g.adj(tem).iterator();
            while(iter.hasNext()){
                int cmp = iter.next();
                if(ent.contains(cmp)){
                    System.out.println("Error: Graph not bipartite!");
                    return false;
                }
            }
        }

        for(int i = 0; i < ang.size(); i++){ //check ent list
            int tem = ang.get(i);
            Iterator<Integer> iter = g.adj(tem).iterator();
            while(iter.hasNext()){
                int cmp = iter.next();
                if(ang.contains(cmp)){
                    System.out.println("Error: Graph not bipartite!");
                    return false;
                }
            }
        }
        Collections.sort(ent);
        Collections.sort(ang);

        //print X
        if(g.V() < 11) {
            System.out.print("Partition X:");
            System.out.print(ent.get(0));
            for (int i = 1; i < ent.size(); i++) {
                System.out.print("," + " " + ent.get(i));
            }
            System.out.println();
            //print Y
            System.out.print("Partition Y:");
            System.out.print(ang.get(0));
            for (int i = 1; i < ang.size(); i++) {
                System.out.print("," + " " + ang.get(i));
            }
            System.out.println();
        } else {
            System.out.println("Graph is bipartite.");
        }

        //test
        //System.out.println(ent.size());
        //System.out.println(ang.size());

		return true;
	}

	public FlowNetwork createFlowNetwork(Graph g){
		/* TODO: Task 3*/
        //new FlowNetwork,add 2 more vertexes
		FlowNetwork flowNet = new FlowNetwork(g.V() + 2);

        //remove all Y partition
        for(int i = 0; i < ang.size(); i++){
            g.removeAll(ang.get(i));
            Iterator<Integer> iter = g.adj(ang.get(i)).iterator();
            if(iter.hasNext()){
                System.out.println("Not Cleaned!");
            }
        }


        //add (0,v) to all ent in FlowNetwork
        //add (v, v[i]) in FlowNetwork
        for(int i = 0; i < ent.size(); i++){
            FlowEdge e = new FlowEdge(0, ent.get(i), 1);
            flowNet.addEdge(e);
            Iterator<Integer> iter = g.adj(ent.get(i)).iterator();
            while(iter.hasNext()){
                e = new FlowEdge(ent.get(i), iter.next(), 1);
                flowNet.addEdge(e);
            }
        }

        //add all Y partition to vertex (n+1)
        for(int i = 0; i < ang.size(); i++){
            FlowEdge e = new FlowEdge(ang.get(i), flowNet.V() - 2, 1);
            flowNet.addEdge(e);
        }

        //print
        if (g.V() < 20) {
            for (int j = 0; j < flowNet.V() - 1; j++) {
                //store the points that v[j] connect to
                ArrayList<Integer> list = new ArrayList<Integer>();
                System.out.printf("%d:", j);
                Iterator<FlowEdge> iter = flowNet.adj(j).iterator();
                while (iter.hasNext()) {
                    list.add(iter.next().to());
                }
                Collections.sort(list);

                //exclude self loops
                int flag = 0; //not print the first one
                if(list.get(0) != j) {
                    System.out.print(list.get(0));
                    flag = 1; //print the first one
                }
                for (int k = 1; k < list.size(); k++) {
                    if(flag == 1) {
                        if (list.get(k) != j) {
                            System.out.print("," + list.get(k));
                        }
                    } else {
                        if(list.get(k) != j){
                            System.out.print(list.get(k));
                            flag = 1;
                        }
                    }
                }

                //shrink point
                if(j == (flowNet.V() - 2)) {
                    System.out.print(" ");
                }
                System.out.println();
            }
        } else {
            System.out.println("Flow network built.");
        }

		return flowNet;
	}


	public static void main(String args[])  {
		/* TODO: Task 4 and 5: You may want to create helper
		 * methods to reduce the size of main method*/
		project3 pro = new project3();
        System.out.println("Input the size of the graph: Input lines of adjacencies: (V0: V1, V2, V3...)");
		Graph g = pro.createGraph();
		if(!pro.validateGraph(g)){
            return;
        }
        if(!pro.checkBipartiteBfs(g)){
            return;
        }
        FlowNetwork flow = pro.createFlowNetwork(g);
        FordFulkerson f = new FordFulkerson(flow, 0, flow.V() - 2);

        //print
        System.out.println("Matching found");
        int flag = 0;//first one
        for(int i = 0; i < pro.ent.size(); i ++) {
            Iterator<FlowEdge> iter = flow.adj(pro.ent.get(i)).iterator();
            while (iter.hasNext()) {
                FlowEdge e = iter.next();
                if (flag == 0) { //first print
                    if (e.flow() == 1 && e.from()!= 0) {
                        System.out.print("(" + e.from() + "," + e.to() + ")");
                        flag = 1;
                    }
                } else {
                    if (e.flow() == 1&& e.from()!= 0) {
                        System.out.print("," + "(" + e.from() + "," + e.to() + ")");
                    }
                }
            }
        }
        System.out.print(" Number of edges " + (int) (f.value()));
        System.out.println();
    }
}

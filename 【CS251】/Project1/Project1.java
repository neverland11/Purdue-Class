import java.awt.*;
import java.awt.Point;
import java.lang.Integer;
import java.lang.System;
import java.util.ArrayList;
import java.util.Scanner;

public class Project1 {
    private int m;
    private int n;
    private WeightedQuickUnionUF qu;
    private int[][] grid;
    private ArrayList<Point> connections;
    public static Scanner s = new Scanner(System.in);

    /**
     * initializes UnionFind structure, grid and connection list
     *
     * @param m
     * @param n
     */
    public Project1(int m, int n) {
        this.m = m;
        this.n = n;
        qu = new WeightedQuickUnionUF(m * n);
        grid = new int[m][n];
        connections = new ArrayList<>();
    }

    /**
     * Reads input from user (pair of connections presented as points), store the input in a list
     */
    public void read_input() {
        int count = 0;
        int x = 0;
        int y = 0;
        Point p = new Point();
        while (s.hasNextInt()) {
            int i = s.nextInt();
            //System.out.print(i);
            if (count % 2 == 0) { // for all x-axis
                x = i;
                p = new Point(x, 0);
                //System.out.println("x = " + x);
            } else if (count % 2 == 1) {// for all y-axis
                y = i;
                p.move(x, y);
                connections.add(p);
                //System.out.println("y = " + y);
            }
            count++;
        }
    }

    /**
     * converts point into an integer
     *
     * @param p
     * @return
     */

    public int map(Point p) {
        int row = (int) p.getX();
        int col = (int) p.getY();
        int result = row * n + col;
        return result;
    }

    /***
     * converts integer into a point
     *
     * @param i
     * @return
     */
    public Point unmap(int i) {
        int row = i / n;
        int col = i % n;
        return new Point(row, col);
    }

    /***
     * scans connections and populates UnionFind structure
     */
    public void process_connections() {
        int count = connections.size();
        for (int i = 0; i < count; i++) {
            //System.out.print(i);
            Point p1 = connections.get(i);
            Point p2 = connections.get(i + 1);
            int i1 = map(p1);
            int i2 = map(p2);
            if (!is_adjacent(p1, p2)) {
                qu.union(i1, i2);
            }
            i = i + 1;
        }
    }

    /**
     * retrieve the connected sets from the UnionFind structure
     *
     * @return connected sets
     */
    public ArrayList<Point> retrieve_connected_sets() {
        ArrayList<Point> connected_sets = new ArrayList<Point>(); //store the connected sets
        //ArrayList<Integer> numbers = new ArrayList<Integer>(); //store sizes of connected sets
        for (int i = 0; i < m * n; i++) {
            int temp = qu.find(i);
            Point p = unmap(temp);
            if(!connected_sets.contains(p)) {
                connected_sets.add(p);
                //numbers.add(qu.getSize(i));
            }
        }
        return connected_sets;
    }

    /**
     * Tests whether two Cells are connected in the grid
     *
     * @param p1
     * @param p2
     * @return
     */
    public boolean is_adjacent(Point p1, Point p2) {
        int i = map(p1);
        int j = map(p2);
        return qu.connected(i, j);
    }

    /**
     * outputs the boundaries and size of each connected set
     *
     * @param sets
     */
    public void output_boundaries_size(ArrayList<Point> sets) {
        int count = sets.size();
        int[] size = new int[count]; //record the size of parents
        int[] xmin = new int[count]; //record the x of parents
        int[] xmax = new int[count];
        int[] ymin = new int[count]; // record the y of parents
        int[] ymax = new int[count];

        //initialize all the array
        for(int i = 0; i < count; i++){
            xmin[i] = n;
        }
        for(int i = 0; i < count; i++){
            xmax[i] = 0;
        }
        for(int i = 0; i < count; i++){
            ymin[i] = m;
        }
        for(int i = 0; i < count; i++){
            ymax[i] = 0;
        }

        //sizes of parents
        for(int i = 0; i < count; i++){
            size[i] = qu.getSize(map(sets.get(i)));
        }

        //get boundaries
        for(int i = 0; i < m * n; i++){ //check for all grids
            Point p = unmap(i);
            for(int j = 0; j < count; j++){ // j for all arrays
                if(qu.find(i) == map(sets.get(j))){ // the grid's parent is found. parents are in order
                    int x = (int)p.getY(); // x for rows, represents as colomn
                    int y = (int)p.getX(); // y for colomns, represents as row
                    if(x < xmin[j]){ //update x
                        xmin[j] = x;
                    } else if(x > xmax[j]){
                        xmax[j] = x;
                    }
                    if(y < ymin[j]){ //update y
                        ymin[j] = y;
                    } else if(y > ymax[j]){
                        ymax[j] = y;
                    }
                }
            }
        }

        //output
        //System.out.printf("number of sets: " + count);
        for(int i = 0; i < count; i++) {
            Point p = sets.get(i);
            System.out.println("Set (" + (int) p.getX() + "," + (int) p.getY() + ")" + " with size " + size[i]);
        }
        for(int i = 0; i < count; i++){
            Point p = sets.get(i);
            System.out.println("Boundaries for (" + (int) p.getX() + "," + (int) p.getY() + ") are " +
                    xmin[i] + "<=x<=" + xmax[i] + " and " + ymin[i] + "<=y<=" + ymax[i]);
        }
    }

    public static void main(String args[]) {
        int m, n;
        //Scanner input = new Scanner(System.in);
        //s = input;
        //System.out.println("Enter size of grid(m n): ");
        m = s.nextInt();
        n = s.nextInt();
        //System.out.print(m);
        //System.out.print(n);
        int con = s.nextInt(); //number of pairs of connections

        Project1 project1 = new Project1(m, n);
        project1.read_input();
        project1.process_connections();
        ArrayList<Point> sets = project1.retrieve_connected_sets();
        project1.output_boundaries_size(sets);
    }
}
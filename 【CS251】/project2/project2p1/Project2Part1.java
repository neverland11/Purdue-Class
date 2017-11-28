import java.util.ArrayList;
import java.util.Scanner;


public class Project2Part1 {
	public static void main(String args[]) {
		/**
		 * TODO: PART1
		 */
		Project2 proj2 = new Project2();
		ArrayList<Token> tokens = proj2.read_input();
		ArrayList<Token> temp = new ArrayList<Token>();
		int size = tokens.size();
		int i = 0;
		while(i < size){
			if(tokens.get(i).getToken() == 28 || tokens.get(i).getToken() == 20 ){
				if(temp.size() == 0){
					i++;
					continue;
				}
				Parser p1 = new Parser(temp);
				Tree t1 = p1.build_expression_tree(temp);
				Tree result = proj2.evaluate(t1);
				t1.print();
				System.out.println();
				Token tok = result.get_info();
				tok.print();
				System.out.println();
				temp.clear();
			} else {
				temp.add(tokens.get(i));
			}
			i++;
		}
	}
}
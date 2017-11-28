import java.util.ArrayList;
import java.util.Scanner;

public class Project0 {
    static ArrayList<Token> tokens = new ArrayList<Token>();
    static String input;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Project0 p0 = new Project0();
        input = p0.read_input(in);
        tokens = p0.get_tokens(input);
        p0.print_tokens(tokens);
    }

    public ArrayList<Token> get_tokens(String input) {
        /*TODO: Split the input into the seperate tokens */
        int len = input.length();
        int flag = 0; //a flag for recording the numbers
        String temp = ""; // to record the number "String";

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);

            //when read a non-number character, first judge, then do itself;
            if (!(Character.isDigit(c))) { // when it is not numbers;
                int result = 0; //result is the value of chars
				/*Token t = new Token(0,"0");
				t.tokenValue_string = "0"; //all non-number tokens' value is 0;
				t.type = 's'; // all non-number tokens' type is 's'*/

                if (c != '.') { // normal cases,no float
                    //add tokens of int
                    if (flag == 1) { // if there have numbers before,there is a int
                        int value = Integer.parseInt(temp);
                        Token bef = new Token(11, value);//the token before quoted one
                        tokens.add(bef);

                        //clear two flags
                        flag = 0;
                        temp = "";
                    }
                }
                //different characters
                switch (c) {
                    case '?':
                        result = 20;
                        break;
                    case '(':
                        result = 21;
                        break;
                    case ')':
                        result = 22;
                        break;
                    case '+':
                        result = 23;
                        break;
                    case '-':
                        result = 24;
                        break;
                    case '*':
                        result = 25;
                        break;
                    case '/':
                        result = 26;
                        break;
                    case '.':  //special cases for float
                        if (flag == 0) { //flag = 0 means there is no number before a dot
                            // the dot is a character
                            result = 27;
                            break;
                        } else { // there is a number before a dot,maybe a float
                            int j = i + 1; //the one after dot
                            if (Character.isDigit(input.charAt(j))) { //there is a float
                                String fl = ""; //for the float
                                while (Character.isDigit(input.charAt(j))) { // is the also a number
                                    fl = fl + input.charAt(j); //record all the numbers like xx.xxxx
                                    j++;
                                }
                                temp = temp + "." + fl;
                                float flo = Float.parseFloat(temp);
                                Token t = new Token(12, flo);
                                //clear the flags
                                i = j - 1; // make the pointer to the end of the float;
                                flag = 0;
                                temp = "";
                                tokens.add(t);//add the float
                                continue;
                            } else { //not a float, but a int + ".", do same thing as before
                                int value = Integer.parseInt(temp);
                                Token bef = new Token(11, value);//construct the token
                                tokens.add(bef);
                                result = 27;
                                //clear two flags
                                flag = 0;
                                temp = "";
                            }
                        }
                        break;

                    case ';':
                        result = 28;
                        break;
                    case '=':
                        result = 29;
                        break;
                    default: //ignore all other chars
                        continue;
                }
                //add the token
                Token t = new Token(result, "0");
                tokens.add(t);
            } else { //it is number
                temp = temp + c;
                flag = 1;
            }
        }
        return tokens;
    }

    public String read_input(Scanner in) {
		/*TODO: Read input until a '?' is found */
        String temp = "";
        //read the whole string to a temp string.
        while (in.hasNext()) {
            temp = temp + in.next();
            //System.out.println(temp.contains("?"));
            if (temp.contains("?")) {
                break;
            }

        }


        //make input be a string that end with '?'
        int i = 0;
        while (i >= 0) {
            input = input + temp.charAt(i);
            if (temp.charAt(i) == '?') {
                break;
            }
            i++;
        }
        return input;
    }

    public void print_tokens(ArrayList<Token> tokens) {
		/*TODO: Print all the tokens before and including the '?' token
		 *      Print tokens from list in the following way, "(token,tokenValue)"
		 * */
        int len = tokens.size();
        for (int i = 0; i < len; i++) {
            Token t = tokens.get(i);
            if (t.getType() == 'i') {
                System.out.print("(" + t.getToken() + "," + t.getValue_i() + ")");
            } else if (t.getType() == 'f') {
                System.out.print("(" + t.getToken() + "," + t.getValue_f() + ")");
            } else if (t.getType() == 's') {
                System.out.print("(" + t.getToken() + "," + t.getValue_s() + ")");
            }
        }
    }
}

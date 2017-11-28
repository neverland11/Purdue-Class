import java.util.Scanner;

class Command {
	// TODO Task 1
	// define a class to represent all the
	// commands from the user input
	String command; //the command
	int date; //the date

	public Command(String command, int date){
		this.command = command;
		this.date = date;
	}


	public int getCommandType() {
		if(command.equals("B")){
			return 1; //1 = Buy
		} else if (command.equals("T3")){
			return 2; //2 = Top 3
		} else if (command.equals("X")){
			return 3; //3 = delete
		} else if (command.substring(0,1).equals("S")){
			return 4; //4 = update
		} else if (command.equals("end")){
			return 5; //5 = end
		} else {
			return 0;
		}
	}
}

public class Project4 {
	static Scanner scan = new Scanner(System.in);
	static String[] com = new String[10];
	static int count = 0;
	static int dateb = 0; //the previous date
	static int date = 0; // the current date
	static int buy = 0; //the numbers of trans


	// get the input from the user
	// parse the input
	// construct a Command object
	// and return it
	public static Command getNextCommand() {
		// TODO Task 1
		String s = scan.nextLine();
		count++;
		com = s.split(" ");
		if(com.length < 2){
			System.out.printf("Error input syntax line %d\n",count);
			System.exit(1);
		}
		Command c = new Command(com[1],Integer.parseInt(com[0]));
		return c;
	}

	public static void main(String[] args) {
		SongCollection songCollection = new SongCollection();
		Command command = getNextCommand();

		while (null != command) {
			if(count == 1){
				dateb = command.date;
			} else {
				date = command.date;
				if(dateb >= date){
					System.out.printf("Error semantics date %d\n", date);
					System.exit(2);
				} else {
					dateb = date;
				}
			}

			switch (command.getCommandType()) {
				// TODO Task1
				case 1: //1 = buy
					if(com.length != 2){
						System.out.printf("Error input syntax line %d\n",count);
						System.exit(1);
					}

					songCollection.addSong(command.date);
					buy++;
					//Error message
					if(buy == 5){
						if(songCollection.min.size() < 3){
							System.out.println("Error min size violated\n");
							System.exit(3);
						}
					}

					break;
				case 2: //2 = T3
					if(com.length != 2){
						System.out.printf("Error input syntax line %d\n",count);
						System.exit(1);
					}

					if(songCollection.min.size() < 3){
						System.out.printf("Error semantics date %d\n",Integer.parseInt(com[0]));
						System.exit(2);
					} else {
						songCollection.popular();
					}
					break;
				case 3: //3 = delete
					if(com.length != 3){
						System.out.printf("Error input syntax line %d\n",count);
						System.exit(1);
					}

					int n = Integer.parseInt(com[2]);
					if(n > songCollection.min.size()){
						System.out.printf("Error semantics date %d\n",Integer.parseInt(com[0]));
						System.exit(2);
					} else {
						songCollection.deleteSong(n);
					}
					break;
				case 4: //4 = update
					if(com.length != 4){
						System.out.printf("Error input syntax line %d\n",count);
						System.exit(1);
					}

					try{
						int deltaN = Integer.parseInt(com[2]);
						int deltaL = Integer.parseInt(com[3]);
					} catch(NumberFormatException ne){
						System.out.printf("Error input syntax line %d\n",count);
						System.exit(1);
						throw ne;
					} catch (ArrayIndexOutOfBoundsException ae){
						System.out.printf("Error input syntax line %d\n",count);
						System.exit(1);
						throw ae;
					}

					int deltaN = Integer.parseInt(com[2]);
					int deltaL = Integer.parseInt(com[3]);
					String name = com[1];
					if(songCollection.map.containsKey(name)) { // check whether existed
						songCollection.updateSong(name, deltaN, deltaL);
					} else {
						System.out.printf("Error semantics date %d\n", Integer.parseInt(com[0]));
						System.exit(2);
					}
					break;
				case 5: //5 = end
					if(com.length != 2){
						System.out.printf("Error input syntax line %d\n",count);
						System.exit(1);
					}

					int[] minMax = songCollection.minMax();
					System.out.printf("min %d, max %d\n", minMax[1], minMax[0]);
					System.out.println();
					System.exit(0);
				default:
					System.out.printf("Error input syntax line %d\n",count);
					System.exit(1);
					break;
			}

			command = getNextCommand();
		}
	}
}

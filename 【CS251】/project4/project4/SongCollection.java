import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SongCollection {
	// TODO Task 2
	// Organize all the songs here
	IndexMinPQ min = new IndexMinPQ(500);
	IndexMaxPQ max = new IndexMaxPQ(500);
	HashMap map = new HashMap();
	int ind = 0; //index
	// add a song at date, with the name S{date}
	// and default L and N
	public void addSong(int date){
		// TODO Task 3
		Song s  = new Song(date, 20, 20, ind); //create a new song
		min.insert(s.getIndex(), s); //linked i@index key@song
		max.insert(s.getIndex(), s); //same
		ind++;
		map.put(s.getName(), s); //key@name value@song, linked
		if(max.size() > 500){
			System.out.printf("Error max size exceeded date %d\n", date);
			System.exit(4);
		}
		System.out.printf("S%d: N=%d, L=%d, pop=%d\n",date, s.getTimes(), s.getLikes(), s.getPopular());
	}

	// delete the n songs of lowest priorities
	public void deleteSong(int n) {
		// TODO Task 3
		for(int i = 0; i < n; i++){
			int del = min.delMin();
			Song s = (Song)max.keyOf(del);
			max.delete(del);
			System.out.printf("S%d deleted, pop=%d\n", s.getDate(), s.getPopular());
		}
	}

	// update song named by songName
	public void updateSong(String songName, int deltaN, int deltaL) {
		// TODO Task 3

		Song s = (Song)map.get(songName); //get the song
		//System.out.println(s.getPopular());
		s.setTimes(s.getTimes() + deltaN);
		s.setLikes(s.getLikes() + deltaL);
		//System.out.println("N = " + s.getTimes());
		//System.out.println("L = " + s.getLikes());
		//System.out.println("p = " + s.getPopular());
		System.out.printf("S%d: N=%d, L=%d, pop=%d\n", s.getDate(), s.getTimes(), s.getLikes(), s.getPopular());
		min.changeKey(s.getIndex(), s);
		max.changeKey(s.getIndex(), s);
	}

	// return the 3 most popular songs in the return value
	// with the most popular at index 0
	//      the second popular at index 1
	//      the third popular at index 2
	public Song[] popular() {
		// TODO Task 3
		Song[] list = new Song[3];
		Iterator iter = max.iterator();
		for(int i = 1; i <= 3; i++) {
			Song s = (Song)max.keyOf((Integer)iter.next()); // get the song
			list[i - 1] = s;
			System.out.printf("%d: S%d, pop=%d\n", i, s.getDate(), s.getPopular());
		}
		return list;
	}

	// return the popularity
	// of the most and least popular songs
	// the 0th integer in the returned object
	// should be the popularity of the most popular song
	// and the 1st integer should be the popularity of
	// the least popular song 
	public int[] minMax() {
		// TODO Task 3
		int[] list = new int[2];
		Song smax = (Song)max.maxKey();
		Song smin = (Song)min.minKey();
		list[0] = smax.getPopular();
		list[1] = smin.getPopular();
		return list;
	}
}
public class Song implements Comparable<Song>{
    int date; //the date of the song
    int times; //the times of being played
    int likes; //the numbers of like
    int index; //the place which in the heap

    // TODO Task 2
    // define any fields needed
    // to represent a song
    // and the getters, setters and constructors

    public Song(int date, int times, int likes, int index){
        this.date = date;
        this.times = times;
        this.likes = likes;
        this.index = index;
    }

    public int getDate(){
        return date;
    }

    public int getTimes(){
        return times;
    }

    public int getLikes(){
        return likes;
    }

    public int getPopular(){
        int popu = times + 2 * likes;
        return popu;
    }

    public String getName(){
        String name = "S" + date;
        return name;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public void setDate(int date){
        this.date = date;
    }

    public void setTimes(int times){
        this.times = times;
    }

    public void setLikes(int likes){
        this.likes = likes;
    }

    public int compareTo(Song a){
        if(this.getPopular() > a.getPopular()){ //first compare popularity
            return 1; //this > a
        } else if(this.getPopular() < a.getPopular()){
            return -1; //this < a
        } else {
            return 0; //same, then compare the date
        }
    }
}

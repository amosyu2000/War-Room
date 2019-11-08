package yu.amos.warroom.WarRoomRes;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

public class PrayerAlgorithm {
    private static String difficulty = "heavy"; //light or heavy

    // Returns a list of lists containing the names of who to pray for each day of the week
    // pList is a list of people's names
    public static String[][] weeklyPrayers(String[] pList) {
        int[] ppp = prayersPerPerson(pList.length, prayersPerWeek(pList.length));
        Collections.shuffle(Arrays.asList(pList));

        // aList will be converted from a 2-D ArrayList to a String[][] later
        ArrayList<ArrayList<String>> aList = new ArrayList<>();

        //populate aList with ArrayLists
        for(int i=0; i<7;i++) {
            aList.add(new ArrayList<String>());
        }

        int i = 0;
        for(int j=0; j<pList.length; j++) { //for each person in pList
            for(int k=0; k<ppp[j]; k++) {   //add them to aList a certain amount of times
                aList.get(i%7).add(pList[j]);
                i += 2;
            }
        }

        //convert aList to a 2-d String array a
        String[][] a = new String[7][];
        for(i=0; i<7; i++) {
            ArrayList<String> day = aList.get(i);
            a[i] = day.toArray(new String[day.size()]);
        }
        return a;
    }

    // the difficulty is a coefficient for the change in ppw per person added.
    private static int getDifficulty() {
        if (difficulty == "light") {
            return 1;
        }
        else if (difficulty == "heavy") {
            return 2;
        }
        return 1;
    }

    // Returns how many prayers in total will be prayed over the week
    private static int prayersPerWeek(int people) {
        int n = 4;
        if (people<n) {
            return 7*people;
        }

        return getDifficulty() * (people-n)+n*7;
    }

    // Returns an integer list of how many prayers people will get each week
    // i.e. if there are 15 people, the list will be 15 integers long
    // If the number does not divide evenly, some people will get 1 more prayer per week than others
    private static int[] prayersPerPerson(int people, int ppw) {
        int[] a = new int[people];
        if(people > 0)
            Arrays.fill(a, ppw/people);
        else
            Arrays.fill(a, 0);

        // loops if ppw did not divide evenly - we want the sum of all the elements in 'a'
        // to be equal to ppw.
        int i = 0, sum = 0;
        for (int num: a) {
            sum += num;
        }
        while (sum < ppw) {
            a[i++]++;
            sum = 0;
            for (int num: a) {
                sum += num;
            }
        }
        return a;
    }
}

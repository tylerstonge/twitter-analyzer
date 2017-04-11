import java.util.List;
import java.util.ArrayList;

public class App {
    private static String[] defaultAccounts = new String[] {
        "potus",
        "RogerJStoneJr",
        "KremlinRussia_E",
        "MedvedevRussia",
        "GenFlynn",
        "EricTrump",
        "mike_pence",
        "HassanRouhani",
        "Marine2017_EN",
        "RichardBSpencer",
        "oreillyfactor"
    };

    public static void main(String[] args) {
        PersistentBTree b = new PersistentBTree(32);
        Cache c = new Cache();

        ArrayList<Long> ids = new ArrayList<Long>();

        for (String acc : defaultAccounts) {
            List<Tweet> tweets = c.getTweetsFromUser(acc);
            for (Tweet t : tweets) {
                b.insert(t.getId());
            }
        }
        b.printTree();
    }
}

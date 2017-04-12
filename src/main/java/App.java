import java.util.List;

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
        
        for (String acc : defaultAccounts) {
            List<Tweet> tweets = c.getTweetsFromUser(acc);
            System.out.println(acc);
            for (Tweet t : tweets) {
                System.out.println(t.getId());
                //b.insert(t.getId());
            }
        }
        
        //b.printTree();
    }
}

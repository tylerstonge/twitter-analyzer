import java.util.List;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

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

    // @Override
    // public void start(Stage stage) throws Exception {
    //     // // Get the tree and the cache
    //     // PersistentBTree b = new PersistentBTree(32);
    //     // Cache c = new Cache();
    //     // 
    //     // // for (String acc : defaultAccounts) {
    //     // //     List<Tweet> tweets = c.getTweetsFromUser(acc);
    //     // //     for (Tweet t : tweets) {
    //     // //         b.insert(t.getId());
    //     // //     }
    //     // // }
    //     // 
    //     // Parent root = FXMLLoader.load(getClass().getResource("ui/main.xml"));
    //     // Scene scene = new Scene(root);
    //     // stage.setTitle("Twitter Analyzer");
    //     // stage.setScene(scene);
    //     // stage.show();    
    // }

    public static void main(String[] args) {
        //launch(args);
        // Get the tree and the cache
        PersistentBTree b = new PersistentBTree(32);
        Cache c = new Cache();
        
        for (String acc : defaultAccounts) {
            List<Tweet> tweets = c.getTweetsFromUser(acc);
            for (Tweet t : tweets) {
                b.insert(t.getId(), t.getAuthor());
            }
        }
        b.insert(1010112140, "testtesttest");
        b.insert(1010121230, "test");
        b.insert(1010123110, "testtest");
        b.printTree();
    }
}
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;

public class UIController implements Initializable {
    private static String[] defaultAccounts = new String[] {
        "potus",
        "RogerJStoneJr",
        "KremlinRussia_E",
        "GenFlynn",
        "EricTrump",
        "mike_pence",
        "HassanRouhani",
        "Marine2017_EN",
        "RichardBSpencer",
        "oreillyfactor"
    };
    private PersistentBTree b = new PersistentBTree(32);
    Cache c = new Cache();
    
    @FXML private TableView<DisplayableTweet> tableView;
    @FXML private TabPane tabPane;
    @FXML private Text tweet1;
    @FXML private Text tweet2;
    
    public void initialize(URL location, ResourceBundle resources) {
        updateBTree();
        ObservableList<DisplayableTweet> data = tableView.getItems();
        b.populateTweets(data, c);
        
        // Add click listener
        tableView.setRowFactory(tv -> {
            TableRow<DisplayableTweet> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    DisplayableTweet rowData = row.getItem();
                    tabPane.getSelectionModel().selectNext();
                    tweet1.setText(rowData.getAuthor() + ": " + rowData.getText());
                    
                    DisplayableTweet mostSimilar = null;
                    double similarity = 0;
                    for (DisplayableTweet t2 : tableView.getItems()) {
                        double tSimilarity = t2.getSimilarity(rowData);
                        if (tSimilarity > similarity || mostSimilar == null) {
                            similarity = tSimilarity;
                            mostSimilar = t2;
                        }
                    }
                    tweet2.setText(mostSimilar.getAuthor() + ": " + mostSimilar.getText());
                }
            });
            return row;
        });
    }
    
    private void updateBTree() {
        for (String acc : defaultAccounts) {
            if (!c.cacheIsFresh(acc)) {
                // If cache is still fresh, no need to update btree
                List<Tweet> tweets = c.getTweetsFromUser(acc);
                for (Tweet t : tweets) {
                    b.insert(t.getId(), t.getLocation());
                }
            }
        }
    }

}
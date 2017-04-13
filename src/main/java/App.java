import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ui/main.xml"));
        Scene scene = new Scene(root);
        stage.setTitle("Twitter Analyzer");
        stage.setScene(scene);
        stage.show();    
    }

    public static void main(String[] args) {
        launch(args);
    }
}
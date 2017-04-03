import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.aliasi.classify.LMClassifier;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.util.Compilable;
import com.aliasi.classify.Classified;
import com.aliasi.classify.Classification;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.corpus.ObjectHandler;

public class SentimentAnalyzer {

    private static SentimentAnalyzer instance;

    LMClassifier classifier;
    String[] categories;

    public SentimentAnalyzer() {
        // Train the classifier
        try {
            train();
            this.classifier = (LMClassifier) AbstractExternalizable.readObject(new File("classifier.txt"));
            this.categories = this.classifier.categories();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String classify(String text) {
        ConditionalClassification classification = this.classifier.classify(text);
        return classification.bestCategory();
    }

    public void train() throws IOException, ClassNotFoundException {
        File folder = new File(getClass().getClassLoader().getResource("sentiment_data").getFile());
        String[] cats = folder.list();
        int nGram = 7;
        LMClassifier classifier = DynamicLMClassifier.createNGramProcess(cats, nGram);
        for (int i = 0; i < cats.length; i++) {
            String cat = cats[i];
            Classification classification = new Classification(cat);
            File file = new File(folder, cats[i]);
            File[] trainFiles = file.listFiles();
            for (int j = 0; j < trainFiles.length; j++) {
                File trainFile = trainFiles[i];
                //String review = Files.readAllLines((Path) trainFile.getPath(), "ISO-8859-1");
                byte[] r = Files.readAllBytes(Paths.get(trainFile.getPath()));
                String review = new String(r, "ISO-8859-1");
                Classified classified = new Classified(review, classification);
                ((ObjectHandler) classifier).handle(classified);
            }
        }
        AbstractExternalizable.compileTo((Compilable) classifier, new File("classifier.txt"));
    }

    public static SentimentAnalyzer getInstance() {
        if (instance == null)
            instance = new SentimentAnalyzer();
        return instance;
    }

}

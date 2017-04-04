
import java.util.List;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import okhttp3.OkHttpClient.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Headers;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

import com.vdurmont.emoji.EmojiParser;


public class TwitterReader {
    private static final String URL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=%s&count=%d";
    private static final String credentials = "vOMaAfmmGGy1d1fqaVAhQ52Ra:tK7XqjdWNNdzuaclGhGv1iVWCfDqzyhZxFpzFgDy57iCcCj1Gt";
    
    private StringBuilder formattedUrl;
    private Formatter formatter;
    
    private OkHttpClient client;
    private String bearerToken;
    private Headers headers;
    
    public TwitterReader() {
        this.formattedUrl = new StringBuilder();
        this.formatter = new Formatter(formattedUrl, Locale.US);
        this.client = new OkHttpClient.Builder().connectTimeout(1600L, TimeUnit.SECONDS).build();
        this.bearerToken = getBearerToken();
        this.headers = new Headers.Builder()
            .add("Authorization", "Bearer " + bearerToken)
            .build();
    }
    
    /**
    * Get most recent tweets from a users Twitter feed.
    * @param user the user to retrieve recent tweets from
    * @param count the number of tweets to retrieve
    */
    public List<Tweet> getTweetsFromUser(String user, int count) {
        // Stores the current url into the class level stringbuilder
        formatter.format(URL, user, count);
        List<Tweet> t = new ArrayList<Tweet>();
        
        // Make the request
        try {
            Response res = makeRequest(formattedUrl.toString());
            JsonParser parser = new JsonParser();
            JsonArray tweets = parser.parse(res.body().string()).getAsJsonArray();
            for (JsonElement tweet : tweets) {
                JsonObject obj = tweet.getAsJsonObject();
                String author = user;
                String text = parseTweet(obj.get("text").getAsString());
                t.add(new Tweet(author, text));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return t;
    }
    
    /**
    * Performs a request on the url.
    * @param url The url to make the request to.
    * @return a response object 
    */
    private Response makeRequest(String url) throws IOException, IllegalStateException {
        // Make the request
        Request req = new Request.Builder()
            .url(url)
            .headers(headers)
            .build();
        Response res = client.newCall(req).execute();
        return res; 
    }
    
    /**
    * Performs the necessary authorization call to Twitter.
    * @return the bearer token to be used in requests
    */
    private String getBearerToken() {
        // Create the proper headers
        Headers h = new Headers.Builder()
            .add("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()))
            .set("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            .build();

        // Create POST data
        FormBody f = new FormBody.Builder().add("grant_type", "client_credentials").build();
        
        // Create the Request
        Request req = new Request.Builder()
            .url("https://api.twitter.com/oauth2/token")
            .post(f)
            .headers(h)
            .build();
        
        // Get the response
        try {
            Response res = client.newCall(req).execute();
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(res.body().string()).getAsJsonObject();
            return obj.get("access_token").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
    * Remove URLs and Emojis from tweets.
    * @param tweet the Tweet to remove URLs and Emojis from.
    * @return the cleaned Tweet text
    */
    private String parseTweet(String tweet) {
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(tweet);
        int i = 0;
        while (m.find()) {
            tweet = tweet.replaceAll(m.group(i),"").trim();
            i++;
        }
        return EmojiParser.removeAllEmojis(tweet);
    }
    
}
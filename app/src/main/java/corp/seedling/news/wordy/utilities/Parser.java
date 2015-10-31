package corp.seedling.news.wordy.utilities;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import corp.seedling.news.wordy.Meaning;
import corp.seedling.news.wordy.News;

/**
 * Created by Ankur Nigam on 15-09-2015.
 */
public class Parser {

    final static String FIELD_DOCS = "docs";
    final static String FIELD_SNIPPET = "snippet";
    final static String FIELD_HEADLINE = "headline";
    final static String FIELD_HEADLINE_MAIN = "main";
    final static String FIELD_WEB_URL = "web_url";
    final static String FIELD_TYPE = "partOfSpeech";
    final static String FIELD_MEANING = "text";
    //For BING
    final static String FIELD_D = "d";
    final static String FIELD_DATE = "Date";
    final static String FIELD_RESULTS = "results";
    final static String FIELD_TITLE = "Title";
    final static String FIELD_URL = "Url";
    final static String FIELD_DESC = "Description";
    private static final String TAG = Parser.class.getSimpleName();

    //BING/////////////
    public static ArrayList<News> getWordUsageBing(String responseJsonString) throws JSONException{
        JSONObject respJsonObject = new JSONObject(responseJsonString);
        JSONObject respFieldJsonObject = respJsonObject.getJSONObject(FIELD_D);
        JSONArray respJsonArray = respFieldJsonObject.getJSONArray(FIELD_RESULTS);

        //prepare a list of usage objects
        ArrayList<News> newsList = new ArrayList<>();

        for(int i = 0 ; i < respJsonArray.length() ; i++){

            JSONObject usageJsonObject = respJsonArray.getJSONObject(i);

            if (usageJsonObject.has(FIELD_DESC))
                newsList.add(new News(
                        usageJsonObject.getString(FIELD_URL),
                        usageJsonObject.getString(FIELD_TITLE),
                        usageJsonObject.getString(FIELD_DATE).substring(0,10)
                                + " ...."
                                + usageJsonObject.getString(FIELD_DESC)
                                + "...."
                ));
        }

        return newsList;
    }

    //BING///////////
    public static boolean isNewsToday(String responseJsonString) throws JSONException{

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateandTime = sdf.format(new Date());
        Log.e(TAG, "currentDateandTime = "+currentDateandTime);

        JSONObject respJsonObject = new JSONObject(responseJsonString);
        JSONObject respFieldJsonObject = respJsonObject.getJSONObject(FIELD_D);
        JSONArray respJsonArray = respFieldJsonObject.getJSONArray(FIELD_RESULTS);

        for(int i = 0 ; i < respJsonArray.length() ; i++) {

            JSONObject usageJsonObject = respJsonArray.getJSONObject(i);

            if (usageJsonObject.has(FIELD_DATE)) {
                Log.e(TAG, "date: "+ (usageJsonObject.getString(FIELD_DATE)).substring(0,10)
                        +"url: = "+ usageJsonObject.getString(FIELD_URL));

                if (currentDateandTime.equalsIgnoreCase(
                        (usageJsonObject.getString(FIELD_DATE)).substring(0,10) ) ){
                    return true;
                }
            }
        }
        return false;
    }

    //**************Parsing NYT **************
    public static ArrayList<News> getWordUsageNY(String responseJsonString) throws JSONException {
        JSONObject respJsonObject = new JSONObject(responseJsonString);
        JSONObject respFieldJsonObject = respJsonObject.getJSONObject("response");
        JSONArray respJsonArray = respFieldJsonObject.getJSONArray(FIELD_DOCS);

        //prepare a list of usage objects
        ArrayList<News> usageList = new ArrayList<>();

        for (int i = 0; i < respJsonArray.length(); i++) {

            JSONObject usageJsonObject = respJsonArray.getJSONObject(i);
            JSONObject usageHeadlineJsonObject = respJsonArray.getJSONObject(i).getJSONObject(FIELD_HEADLINE);

            if (usageJsonObject.has(FIELD_SNIPPET))
//                    usageList.add(new News(
//                            usageJsonObject.getString(FIELD_WEB_URL),
//                            usageHeadlineJsonObject.getString(FIELD_HEADLINE_MAIN),
//                            usageJsonObject.getString(FIELD_SNIPPET)));

                usageList.add(new News(
                        usageJsonObject.getString(FIELD_WEB_URL),
//                        usageJsonObject.getString(FIELD_HEADLINE_MAIN),
                        "",
                        usageJsonObject.getString(FIELD_SNIPPET)
                ));


        }

        return usageList;
    }

    //**************************Parsing Wordnik ******************************
    public static ArrayList<Meaning> getWordMeanings(String responseJsonString) throws JSONException {
        JSONArray respJsonArray = new JSONArray(responseJsonString);

        ArrayList<Meaning> meaningsList = new ArrayList<>();
        String type = "";

        for (int i = 0; i < respJsonArray.length(); i++) {

            JSONObject meaningJsonObject = respJsonArray.getJSONObject(i);

            //allow only one meaning of a type
            if (! type.equalsIgnoreCase(meaningJsonObject.getString(FIELD_TYPE))) {
                meaningsList.add(new Meaning(
                        meaningJsonObject.getString(FIELD_TYPE),
                        meaningJsonObject.getString(FIELD_MEANING)));

                type = meaningJsonObject.getString(FIELD_TYPE);
            }
        }

        return meaningsList;
    }


//**************************Parsing COLLINS******************************
//    public static ArrayList<Meaning> getWordMeanings(String responseJsonString) throws JSONException{
//
//        ArrayList<Meaning> meaningsList = new ArrayList<>();
//        JSONObject meaningJsonObject = new JSONObject(responseJsonString);
//
//        for(int i = 0 ; i < 1 ; i++){
//
//            meaningsList.add(new Meaning(
//                    meaningJsonObject.getString("entryContent"),
//                    ""));
//        }
//
//        return meaningsList;
//    }


}

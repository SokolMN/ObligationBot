import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class ApiWorker {

    ApiWorker(String url){
        this.URLString = url;
    }

    HttpResponse response;
    String URLString;

    public JSONObject sendRequest(String methodName){
        System.out.println("sendRequest Mehotd");
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = null;
        if(methodName.equals("GET")){
            httpGet = new HttpGet(URLString);
        }

        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
            response = null;
        }

        String jsonString = getResponseJSON(response);

        JSONParser parser = new JSONParser();
        JSONObject jsonOutput;

        try {
            jsonOutput = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
            jsonOutput = null;
        }

        return jsonOutput;
    }

    private String getResponseJSON(HttpResponse response){
        System.out.println("getResponseJSON method");
        HttpEntity entity = response.getEntity(); //Этот объект нужен, чтобы вытащить данные из response
        String responseString;
        try {
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            responseString = null;
        }

        return responseString;
    }
}

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

public class WeatherClass {

    private JSONObject weatherJsonObj;
    private String temperatura; //Температура
    private String station;     //Состояние (дождь, снег, пасмурно)

    WeatherClass(){
        ApiWorker apiWorker = new ApiWorker("http://api.openweathermap.org/data/2.5/weather?q=Moscow&units=metric&appid=9be8bd474a1c675c6dd3e03bd47bc333&lang=ru");
        weatherJsonObj = apiWorker.sendRequest("GET");
    }

    private String getResponseObjData(String upName, String downName){
        JSONObject downJsonObject = (JSONObject) weatherJsonObj.get(upName);
        String dataValue = downJsonObject.get(downName).toString();
        return dataValue;
    }

    private String getResponseArrayData(String upName, String downName){
        JSONArray jsonArray = (JSONArray) weatherJsonObj.get(upName);
        JSONObject obj = new JSONObject();

        for(int i =0; i <jsonArray.size(); i++){
            obj = (JSONObject) jsonArray.get(i);

        }

        return obj.get(downName).toString();
    }

    public String getStation(){
        station = getResponseArrayData("weather", "description");
        return  station;
    }

    public String getTemperatura() {
        temperatura = getResponseObjData("main", "temp");
        return temperatura;
    }
}

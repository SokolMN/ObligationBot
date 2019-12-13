import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WeatherClass {

    private JSONObject weatherJsonObj;
    private String temperatura; //Температура
    private String station;     //Состояние (дождь, снег, пасмурно)

    WeatherClass(JSONObject obj){
        this.weatherJsonObj = obj;
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

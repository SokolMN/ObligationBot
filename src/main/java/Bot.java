import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Bot extends TelegramLongPollingBot {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response;
    ArrayList<String> badwords = new ArrayList<String>();
    String responseString;
    final Random random = new Random();
    JSONObject jsonOutput=null;


    Bot(DefaultBotOptions options){
        super(options);
    }

    Bot(){
    }

    public synchronized void setButtons(SendMessage sendMessage) {
        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add(new KeyboardButton("Привет"));

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add(new KeyboardButton("Получить облигацию"));

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public void onUpdateReceived(Update update) {
        update.getUpdateId();

        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        System.out.println(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + ": " + update.getMessage().getText());
        setButtons(sendMessage);

        if(update.getMessage().getText().equals("Привет") ||update.getMessage().getText().equals("привет") ){
            setBadwords();
            sendMessage.setText("Привет" + badwords.get(random.nextInt(badwords.size())) + "! " + getWeather());
        }else
            sendMessage.setText(getObligation());

        try{
            execute(sendMessage);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    private String getObligation(){
        ApiWorker apiWorker = new ApiWorker();
        jsonOutput = apiWorker.sendRequest("RU000A100HE1", "GET");

        JSONObject descriptionOject;
        descriptionOject = (JSONObject) jsonOutput.get("description");

        ObligationClass obligation = new ObligationClass((JSONArray)descriptionOject.get("columns"), (JSONArray) descriptionOject.get("data"));
        obligation.setObligationInfo();

        String obligationInfo = obligation.getObligationFullName() + " " + obligation.getCouponSum() + " " + obligation.getCurrency() +" " + obligation.getCouponDate();

        return obligationInfo;
    }

    private void setBadwords(){
        badwords.add(", кстати, иди нахуй");
        badwords.add(" приёмный");
        badwords.add(" жиробас");
        badwords.add(" красавчик");
        badwords.add(" фемка");
        badwords.add(". Ты пидор!");
        badwords.add(" девка без руки, какого хрена");
        badwords.add(" красотка!");
    }

    private String getWeather(){
        HttpGet httpGet = new HttpGet("http://api.openweathermap.org/data/2.5/weather?q=Moscow&units=metric&appid=9be8bd474a1c675c6dd3e03bd47bc333&lang=ru");


        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }

       // System.out.println(response.toString());

        HttpEntity entity = response.getEntity(); //Этот объект нужен, чтобы вытащить данные из response

        try {
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
       // System.out.println("Данные из GET-запроса " + responseString);
        JSONParser parser = new JSONParser();

        try {
            jsonOutput = (JSONObject) parser.parse(responseString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String weatherInfo = "Кстати, погода! Сейчас за окном: " + getResponseObjData(jsonOutput, "main", "temp") + ", " + getResponseArrayData(jsonOutput, "weather", "description");

        return weatherInfo;
    }

    private String getResponseObjData(JSONObject jsonObject, String upName, String downName){
        JSONObject downJsonObject = (JSONObject) jsonObject.get(upName);
        String dataValue = downJsonObject.get(downName).toString();
        return dataValue;
    }

    private String getResponseArrayData(JSONObject jsonObject, String upName, String downName){
        JSONArray jsonArray = (JSONArray) jsonObject.get(upName);
        JSONObject obj = new JSONObject();

        for(int i =0; i <jsonArray.size(); i++){
            obj = (JSONObject) jsonArray.get(i);

        }

        return obj.get(downName).toString();
    }



    public String getBotUsername() {
        return "CoolSokolovBot";
    }

    public String getBotToken() {
        return "797703954:AAE0JA9ktlYExq4FQjFmUSgg133XwX8cIeI";
    }
}

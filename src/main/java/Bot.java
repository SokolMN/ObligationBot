import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Bot extends TelegramLongPollingBot {
    ArrayList<String> badwords = new ArrayList<String>();
    final Random random = new Random();
    JSONObject jsonOutput=null;
    String messageString; //Сообщение от пользователя


    Bot(DefaultBotOptions options){
        super(options);
    }

    Bot(){
    }



    public void onUpdateReceived(Update update) {
        update.getUpdateId();

        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        System.out.println(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + ": " + update.getMessage().getText());

        messageString = update.getMessage().getText();

        UserClass user = new UserClass(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName(), update.getMessage().getChatId());
        if(!user.isUserExists()){
            user.createUser();
        }

        if ("/start".equals(messageString)) {
            setButtons(sendMessage);
            sendMessage.setText("Что желаете сделать?");

        } else if ("Привет".equals(messageString)) {
            setBadwords();
            WeatherClass weather = new WeatherClass();
            sendMessage.setText("Привет" + badwords.get(random.nextInt(badwords.size())) + "! " + "За окном сейчас " + weather.getTemperatura() + ", " + weather.getStation() );

        } else if ("Получить облигацию".equals(messageString)) {
            //ObligationClass obligation = new ObligationClass();
            //sendMessage.setText(obligation.getObligationFullName() + " " + obligation.getCouponSum() + " " + obligation.getCurrency() +" " + obligation.getCouponDate());

        } else if("/addobl".equals(messageString.substring(0,7))) {
            String obligationName = messageString.substring(8);
            ObligationClass obligation = new ObligationClass(obligationName);
            obligation.setObligationOwner(user);
            if(!obligation.isObligationExistForUser()){
                obligation.createObligation();
            }

            if(obligation.getErrorFLg()){
                sendMessage.setText(obligation.getErrorMessage());
            }else{
                sendMessage.setText("Вы добавили " + obligation.getObligationFullName() + "\n" +
                        "Ближайшая дата платежа: " + obligation.getCouponDate() +
                        " в размере " + obligation.getCouponSum() + obligation.getCurrency());
            }
        } else {
            sendMessage.setText("Что-то не то");
        }


        try{
            execute(sendMessage);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
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

        KeyboardRow keyboardThiedRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add(new KeyboardButton("Оля ля ля"));

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
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



    public String getBotUsername() {
        return "CoolSokolovBot";
    }

    public String getBotToken() {
        return "797703954:AAE0JA9ktlYExq4FQjFmUSgg133XwX8cIeI";
    }
}

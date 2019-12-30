import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.*;


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
            sendMessage.setText("Ну давай начнем. Что доступно \n" +
                                "1.Можно добавить облигацию, через команду /addobl\n" +
                                "2.Можно получить список выплат по вашим облигациям, нажав кнопку 'Список выплат по облигациям'. Для этого сначала надо добавить их (п. 1)\n" +
                                "3.Узнать текущую погоду за окном, нажав кнопку 'Погода'");

        } else if ("Погода".equals(messageString)) {
            setBadwords();
            WeatherClass weather = new WeatherClass();
            sendMessage.setText("Привет" + badwords.get(random.nextInt(badwords.size())) + "! " + "За окном сейчас " + weather.getTemperatura() + ", " + weather.getStation() );
            ObligationClass obligation = new ObligationClass("RU000A100HE1", 2);
            obligation.setObligationOwner(user);
        } else if ("Список выплат по облигациям".equals(messageString)) {
            try {
                sendMessage.setText(user.getUserPayments());
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if("/addobl".equals(messageString.substring(0,7))) {
            try{
                String obligationCode = messageString.substring(8);
                Integer quantity = messageString.lastIndexOf(" ");
                ObligationClass obligation = new ObligationClass(obligationCode, quantity);

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
            }catch (StringIndexOutOfBoundsException e){
                sendMessage.setText("Похоже, вы неверно ввели команду для добавления облигации. \n" +
                        "Формат команды:" +
                        " /addobl [Код облигации]\n" +
                        "Например: /addobl RU000A100HE1");
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
        keyboardFirstRow.add(new KeyboardButton("Погода"));

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add(new KeyboardButton("Список выплат по облигациям"));

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

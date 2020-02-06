import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.sql.SQLException;
import java.util.*;


public class Bot extends TelegramLongPollingBot {
    ArrayList<String> badwords = new ArrayList<String>();
    final Random random = new Random();
    String messageString; //Сообщение от пользователя
    UserClass user;
    SendMessage sendMessage;
    ChatBot chatBot;


    Bot(DefaultBotOptions options){
        super(options);
    }

    Bot(){
        chatBot = new ChatBot();
    }

    public void onUpdateReceived(Update update) {

        update.getUpdateId();
        ChatBotReply reply;


        sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        System.out.println(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + ": " + update.getMessage().getText());

        chatBot.setChatId(update.getMessage().getChatId().toString());
        chatBot.setIncomeMessage(update.getMessage().getText());


        messageString = update.getMessage().getText();

        user = new UserClass(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName(), update.getMessage().getChatId());
        if(!user.isUserExists()){
            user.createUser();
        }
        chatBot.setUser(user);

        switch (messageString){
            case "/start":
                setButtons(sendMessage);
                doSendMessage(sendMessage, null,
                "Ну давай начнем. Что доступно \n" +
                        "1.Можно добавить облигацию, через команду /addobl\n" +
                        "2.Можно получить список выплат по вашим облигациям, нажав кнопку 'Список выплат по облигациям'. Для этого сначала надо добавить их (п. 1)\n" +
                        "3.Узнать текущую погоду за окном, нажав кнопку 'Погода'");
                break;
            case "Погода":
                setBadwords();
                WeatherClass weather = new WeatherClass();
                doSendMessage(sendMessage, null, "Привет" + badwords.get(random.nextInt(badwords.size())) + "! " + "За окном сейчас " + weather.getTemperatura() + ", " + weather.getStation() );
                break;
            case "Список выплат по облигациям":
                try {
                    doSendMessage(sendMessage, null, user.getUserPayments());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            default:
                reply = chatBot.answer();
                doSendMessage(sendMessage, reply.replyKeyboardMarkup, reply.sendMsgText);
                break;
        }

    }

    public void doSendMessage(SendMessage sendMessage, ReplyKeyboardMarkup keybord, String sendMsgText){
        sendMessage.setText(sendMsgText);
        if(keybord != null)
            sendMessage.setReplyMarkup(keybord);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    public void setButtons(SendMessage sendMessage) {
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
        keyboardFirstRow.add(new KeyboardButton("Добавить облигацию"));


        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add(new KeyboardButton("Список выплат по облигациям"));
        keyboardSecondRow.add(new KeyboardButton("Изменить облигацию"));

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

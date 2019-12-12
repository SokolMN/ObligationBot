import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {

    public static void main(String[] args){
        ApiContextInitializer.init();

        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
        botOptions.setProxyHost("127.0.0.1");
        botOptions.setProxyPort(9150);
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        //Bot bot = new Bot(botOptions);
        Bot bot = new Bot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();


        try{
            telegramBotsApi.registerBot(bot);
        }catch (TelegramApiRequestException e){
            e.printStackTrace();
        }
    }
}

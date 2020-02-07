import java.util.HashMap;

public class ChatBot {
    protected final String inChangeObligation = "Изменение облигации";
    protected final String changeObligation = "Изменить облигацию";
    protected final String inAddObligation = "Добавление облигации";
    protected final String addObligation = "Добавить облигацию";
    protected final String goHome = "Домой";
    HashMap<String, String> statesUserMap = new HashMap<String, String>(); //Мапа состояний, в которых находятся пользователи (ChatId - State)
    String incomeMessage; //Пришедшее сообщение
    String chatId; //Id чата с пользователем
    UserClass user;


   public void setUser(UserClass user){
       this.user = user;
   }

    public void setIncomeMessage(String incomeMessage) {
        this.incomeMessage = incomeMessage;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ChatBotReply answer(){
        System.out.println("Текущее положение пользователя: " + statesUserMap.get(this.chatId));
        ChatBotReply reply=null;
        reply = new ChatBotReply();

        if(!statesUserMap.containsKey(chatId)){
            switch (incomeMessage){
                case changeObligation:
                    statesUserMap.put(chatId, inChangeObligation);
                    reply.setSendMsgText("Для изменения облигации введите её код и количество");
                    reply.setgoHomeKeyBoard();
                    break;
                case goHome:
                    statesUserMap.remove(chatId);
                    reply.setStartKeyboard();
                    reply.setSendMsgText("Для продолжения нажмите кнопку");
                    break;
                case addObligation:
                    statesUserMap.put(chatId, inAddObligation);
                    reply.setSendMsgText("Для добавления облигации введите её код и количество");
                    reply.setgoHomeKeyBoard();
                    break;
            }
        }else {
            switch (statesUserMap.get(chatId)) {
                case inChangeObligation:
                    if (incomeMessage.equals(goHome)) {
                        statesUserMap.remove(chatId);
                        reply.setSendMsgText("Для продолжения нажмите кнопку");
                        reply.setStartKeyboard();
                    } else {
                        System.out.println("Мы находимся в изменении облигации, но она еще не доработана");
                    }
                    break;
                case inAddObligation:
                    if (incomeMessage.matches(".*\\s\\d*")){
                        System.out.println("Ввели облигацию");
                        String obligationCode = incomeMessage.substring(0, incomeMessage.lastIndexOf(" "));
                        int quantity = Integer.parseInt(incomeMessage.substring(incomeMessage.lastIndexOf(" ")+1));
                        ObligationClass obligation = new ObligationClass(obligationCode, quantity);

                        obligation.setObligationOwner(user);
                        if(!obligation.getErrorFLg() && !obligation.isObligationExistForUser() ){
                            obligation.createObligation();
                        }

                        if(obligation.getErrorFLg()){
                            reply.setSendMsgText(obligation.getErrorMessage() + "\nДля выхода нажмите Домой");
                        }else{
                            reply.setSendMsgText("Вы добавили " + obligation.getObligationFullName() + "\n" +
                                    "Ближайшая дата платежа: " + obligation.getConvertedCouponDate() +
                                    " в размере " + obligation.getCouponSum() + " " + obligation.getConvertedCurrency() + "\n" +
                                    "Введите новую облигацию или нажмите Домой");
                        }
                    }else if(incomeMessage.equals(goHome)){
                        statesUserMap.remove(chatId);
                        reply.setSendMsgText("Для продолжения нажмите кнопку");
                        reply.setStartKeyboard();
                    }else{
                        reply.setSendMsgText("Вы ввели некорректное значение. Введите код облигации и колиество. Например RU00012BK0S 5. Для возврата нажмите кнопку Домой");
                        reply.setgoHomeKeyBoard();
                    }
            }
        }
        return reply;
    }
}

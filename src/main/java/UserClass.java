import java.util.ArrayList;

public class UserClass {
    private String fullName; //ФИО пользователя
    private Long chatId; //Чат Id пользователя
    private Integer userId; //Id пользователя


    UserClass(String fullName, Long chatId){
        this.chatId = chatId;
        this.fullName = fullName;
    }

    public boolean isUserExists(){
        DBWorker dbWorker = new DBWorker();
        boolean existFlg = false;
        userId = dbWorker.isHaveSelectedRecord("Select * from user where chat_id = '" + this.chatId + "'");

        if(userId >0)
            existFlg = true;
        return  existFlg;
    }

    public void createUser(){
        ArrayList<String> columnNames = new ArrayList<String>();
        columnNames.add("FULL_NAME");
        columnNames.add("CHAT_ID");

        ArrayList columnValues = new ArrayList();
        columnValues.add("'" + this.fullName + "'");
        columnValues.add(this.chatId );
        DBWorker dbWorker = new DBWorker();
        userId = dbWorker.insertRecord(columnNames, columnValues, "USER");
    }

    public Long getChatId(){
        return this.chatId;
    }

    public Integer getUserId(){
        return this.userId;
    }


}

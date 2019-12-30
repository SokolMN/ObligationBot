import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Double.parseDouble;

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

    public String getUserPayments() throws SQLException {
        /*Как надо сделать!!!!!!!!
            1. Сделать новый конструктор с полями из БД
            2. Понасоздавать объекты ObligationClass в ArrayList с полями из БД
            3. Сравнить даты, и если надо, то вызвать API, чтобы обновить информацию
         */
        ArrayList<ObligationClass> oblArray = new ArrayList<ObligationClass>();
        Date couponDate=null;
        Date currentDate = new Date();
        HashMap<String, String> updateMap = new HashMap<String, String>();
        HashMap<String, String> reqMap = new HashMap<String, String>();
        String totalPayments="";

        DBWorker dbWorker = new DBWorker();
        dbWorker.selectRecord("select name, coupon_date, CURRENCY, code,coupon_Date, QUANTITY, coupon_sum from obligation  where user_id = '"
                + this.getUserId() + "' order by coupon_date");

        while(dbWorker.resultSet.next()){
            oblArray.add(new ObligationClass(dbWorker.resultSet.getString("CODE"),
                    dbWorker.resultSet.getInt("QUANTITY"),
                    dbWorker.resultSet.getString("coupon_sum"),
                    dbWorker.resultSet.getString("COUPON_DATE"),
                    dbWorker.resultSet.getString("NAME"),
                    dbWorker.resultSet.getString("CURRENCY")));
        }

        int i=1;
        for(ObligationClass obligation : oblArray ){

            try {
                couponDate = new SimpleDateFormat("yyyy-MM-dd").parse(obligation.getCouponDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int compareDateResult = currentDate.compareTo(couponDate);

            if(compareDateResult>0){ //Если текущая дата больше даты из селекта облигации
                obligation = new ObligationClass(obligation.getObligationCode(), obligation.getQuantity());
                updateMap.put("COUPON_DATE", obligation.getCouponDate());
                reqMap.put("USER_ID", this.userId +"");
                reqMap.put("CODE", obligation.getObligationCode());
                obligation.updateObligation(updateMap, reqMap);
            }
            totalPayments = totalPayments + i + ".Название: " + obligation.getObligationFullName() + "\n" +
                                                "     Дата выплаты: " + obligation.getConvertedCouponDate()  + "\n" +
                                                "     Сумма выплаты: " + parseDouble(obligation.getCouponSum())*obligation.getQuantity() + " " + obligation.getConvertedCurrency() + "\n" ;
            i++;
        }


        return totalPayments;
    }

    public Long getChatId(){
        return this.chatId;
    }

    public Integer getUserId(){
        return this.userId;
    }


}

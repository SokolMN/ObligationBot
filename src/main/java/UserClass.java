import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Double.compare;
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

        ArrayList<ObligationClass> oblArray = new ArrayList<ObligationClass>();
        Date couponDate=null;
        Date currentDate = new Date();
        HashMap<String, String> updateMap = new HashMap<String, String>();
        HashMap<String, String> reqMap = new HashMap<String, String>();
        String totalPayments="";
        double totalSumByLastDate=0; //Полная сумма к последней дате всех купонов
        String lastDateOffAllCoupons=""; //Последняя дата из всех ближайших дат

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
                    lastDateOffAllCoupons = dbWorker.resultSet.getString("COUPON_DATE");
                  //  totalSumByLastDate = totalSumByLastDate + Double.parseDouble(dbWorker.resultSet.getString("coupon_sum"))*dbWorker.resultSet.getInt("QUANTITY");
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
                                                "     Сумма выплаты: " + new BigDecimal(parseDouble(obligation.getCouponSum())*obligation.getQuantity()).setScale(2, RoundingMode.UP) + " " + obligation.getConvertedCurrency() + "\n" ;
            i++;
            lastDateOffAllCoupons = obligation.getConvertedCouponDate();
            totalSumByLastDate = totalSumByLastDate + (parseDouble(obligation.getCouponSum())*obligation.getQuantity());
        }

        totalPayments = totalPayments + "\n" + "Итого к " + lastDateOffAllCoupons + ": " + new BigDecimal(totalSumByLastDate).setScale(2, RoundingMode.UP);
        return totalPayments;
    }

    public String getUserObligationList(){
        String oblData="";

        DBWorker dbWorker = new DBWorker();
        dbWorker.selectRecord("select ROW_ID, NAME, QUANTITY from obligation where user_id = '" + this.userId + "'");

        try {
            while (dbWorker.resultSet.next()) {
                oblData = oblData + "Номер: " + dbWorker.resultSet.getString("ROW_ID") + "\n"
                        + "Название: " + dbWorker.resultSet.getString("NAME") + "\n"
                        + "Количество: " + dbWorker.resultSet.getInt("QUANTITY") + "\n\n";
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return oblData;
    }

    public Long getChatId(){
        return this.chatId;
    }

    public Integer getUserId(){
        return this.userId;
    }


}

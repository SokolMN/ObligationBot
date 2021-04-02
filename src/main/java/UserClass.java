import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Double.doubleToLongBits;
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

    public String getUserObligationList(){
        String oblList="";
        int i = 1;
        DBWorker dbWorker = new DBWorker();
        dbWorker.selectRecord("select name, code, quantity from obligation where user_id = '" + this.userId + "'" );

            try {
                while (dbWorker.resultSet.next()) {
                    oblList = oblList + i + ". Название: "  + dbWorker.resultSet.getString("NAME") + "\n" +
                              "Код:" + dbWorker.resultSet.getString("CODE") + "\n" +
                              "Количество:" + dbWorker.resultSet.getString("QUANTITY") + "\n";
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return oblList;
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
                + this.getUserId() + "'" /*order by coupon_date*"*/);

        while(dbWorker.resultSet.next()){
            oblArray.add(new ObligationClass(dbWorker.resultSet.getString("CODE"),
                    dbWorker.resultSet.getInt("QUANTITY"),
                    dbWorker.resultSet.getString("coupon_sum"),
                    dbWorker.resultSet.getString("COUPON_DATE"),
                    dbWorker.resultSet.getString("NAME"),
                    dbWorker.resultSet.getString("CURRENCY")));
                    lastDateOffAllCoupons = dbWorker.resultSet.getString("COUPON_DATE");
        }

        int i=1;
        int k=0; //Для индексации листа с облигациями, чтобы изменить её при обновлении через API
        for(ObligationClass obligation : oblArray ) {

            try {
                couponDate = new SimpleDateFormat("yyyy-MM-dd").parse(obligation.getCouponDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int compareDateResult = currentDate.compareTo(couponDate);

            if (compareDateResult > 0) { //Если текущая дата больше даты из селекта облигации
                obligation = new ObligationClass(obligation.getObligationCode(), obligation.getQuantity());
                updateMap.put("COUPON_DATE", obligation.getCouponDate());
                reqMap.put("USER_ID", this.userId + "");
                reqMap.put("CODE", obligation.getObligationCode());
                obligation.updateObligation(updateMap, reqMap);
                oblArray.set(k, obligation);
            }
        }


        sortObligatinByDate(oblArray);


        for(ObligationClass obligation : oblArray ) {
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



    private static ArrayList<ObligationClass> sortObligatinByDate(ArrayList<ObligationClass> oblArray) {
        ArrayList<ObligationClass> leftList = new ArrayList<>();
        ArrayList<ObligationClass> rightList = new ArrayList<>();
        ArrayList<ObligationClass> totalList = new ArrayList<>();
        int i = 0;
        int j = oblArray.size() - 1;
        Date date1 = null, date2=null;



        if (oblArray.size() < 2) {
            return oblArray;
        } else if (oblArray.size() == 2) {
            try {
                date1 = new SimpleDateFormat("dd.mm.yyyy").parse(oblArray.get(0).getConvertedCouponDate());
                date2 = new SimpleDateFormat("dd.mm.yyyy").parse(oblArray.get(1).getConvertedCouponDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date1.compareTo(date2) > 0) {

                ObligationClass tmp = oblArray.get(0);
                oblArray.set(0, oblArray.get(1));
                oblArray.set(1, tmp);
            }
            return oblArray;
        } else {
            int mainIndex = oblArray.size() / 2;
            ObligationClass mainItem = oblArray.get(mainIndex);

            date1 = oblArray.get(i).getConvertedDateClassCouponDate();
            date2 = oblArray.get(j).getConvertedDateClassCouponDate();


            while (i < j) {
                while (oblArray.get(i).getConvertedDateClassCouponDate().compareTo(mainItem.getConvertedDateClassCouponDate()) < 0) {
                    leftList.add(oblArray.get(i));
                    i++;
                }

                while (oblArray.get(j).getConvertedDateClassCouponDate().compareTo(mainItem.getConvertedDateClassCouponDate()) > 0) {
                    rightList.add(oblArray.get(j));
                    j--;
                }

                if (i <= j) {
                    ObligationClass tmp = oblArray.get(i);
                    oblArray.set(i, oblArray.get(j));
                    oblArray.set(j, tmp);
                    leftList.add(oblArray.get(i));
                    rightList.add(oblArray.get(j));
                    i++;
                    j--;
                }
                //   System.out.println(list.toString());

            }

            totalList.addAll(sortObligatinByDate(leftList));
            totalList.addAll(sortObligatinByDate(rightList));


            return totalList;
        }
    }

    public Long getChatId(){
        return this.chatId;
    }

    public Integer getUserId(){
        return this.userId;
    }
}

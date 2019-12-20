import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class ObligationClass {

    private JSONArray columnArray; //Название полей
    private JSONArray dataArray; //Значения полей
    private String obligationFullName;
    private String couponSum;
    private String currency;
    private String couponDate;
    private UserClass obligationOwner;
    private Integer obligationId;
    private String errorMessage;
    private boolean errorfFlg;
    HashMap <String, ObligationInfo> obligationInfoMap = new HashMap();

    public ObligationClass(String name) {
        ApiWorker apiWorker = new ApiWorker("https://iss.moex.com/iss/securities/" + name + ".json");
        JSONObject descObj = (JSONObject) apiWorker.sendRequest("GET").get("description");
        this.columnArray = (JSONArray) descObj.get("columns");
        this.dataArray = (JSONArray) descObj.get("data");
        setObligationInfo();
    }


    public boolean isObligationExistForUser(){
        DBWorker dbWorker = new DBWorker();
        boolean existFlg = false;
        obligationId = dbWorker.isHaveSelectedRecord("select * from obligation where user_id = '" + obligationOwner.getUserId() +
                "' AND NAME='" + this.getObligationFullName() + "'");

        if(obligationId >0){
            existFlg = true;
            this.errorMessage = "Облигация уже была добавлена ранее в ваш портфель";
            this.errorfFlg = true;
        }
        return existFlg;
    }

    public void createObligation(){
        DBWorker dbWorker = new DBWorker();
        ArrayList columnNames = new ArrayList();
        columnNames.add("NAME");
        columnNames.add("COUPON_DATE");
        columnNames.add("COUPON_SUM");
        columnNames.add("USER_ID");
        columnNames.add("CURRENCY");

        ArrayList columnValues = new ArrayList();
        columnValues.add("'" + this.getObligationFullName() + "'");
        columnValues.add("'" + this.getCouponDate().replace('-', '/') + "'");
        columnValues.add(this.getCouponSum());
        columnValues.add(this.obligationOwner.getUserId());
        columnValues.add("'" + this.getCurrency() + "'");

        dbWorker.insertRecord(columnNames, columnValues, "Obligation");
    }

    public void setObligationInfo(){
        JSONArray tempArray;
        for (int i=0; i<dataArray.size(); i++){
            tempArray = (JSONArray) dataArray.get(i);
            obligationInfoMap.put(tempArray.get(0).toString(), new ObligationInfo(tempArray.get(0).toString(), tempArray.get(1).toString(), tempArray.get(2).toString()));
        }
    }

    public String getCurrency() {
        currency = obligationInfoMap.get("FACEUNIT").getValue();
        return currency;
    }

    public String getCouponSum() {
        couponSum = obligationInfoMap.get("COUPONVALUE").getValue();
        return couponSum;
    }

    public String getObligationFullName() {
        obligationFullName = obligationInfoMap.get("NAME").getValue();
        return obligationFullName;
    }

    public String getCouponDate() {
        couponDate = obligationInfoMap.get("COUPONDATE").getValue();
        return couponDate;
    }

    public void setObligationOwner(UserClass user){
        this.obligationOwner = user;
    }

    public String getErrorMessage(){
        return this.errorMessage;
    }

    public boolean getErrorFLg(){
        return this.errorfFlg;
    }

}

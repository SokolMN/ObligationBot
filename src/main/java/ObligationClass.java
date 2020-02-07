import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.*;


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
    private String obligationCode;
    private Integer quantity; //Количество облигаций
    HashMap <String, ObligationInfo> obligationInfoMap = new HashMap();


    public ObligationClass(String code, int quantity) {
        ApiWorker apiWorker = new ApiWorker("https://iss.moex.com/iss/securities/" + code + ".json");
        JSONObject descObj = (JSONObject) apiWorker.sendRequest("GET").get("description");
        this.columnArray = (JSONArray) descObj.get("columns");
        this.dataArray = (JSONArray) descObj.get("data");
        this.obligationCode = code;
        this.quantity = quantity;
        setObligationInfo();
    }

    public ObligationClass(String oblCode, Integer quantity,String couponSum, String couponDate, String oblFullName, String currency ){
        this.obligationCode = oblCode;
        this.quantity = quantity;
        this.couponSum = couponSum;
        this.couponDate = couponDate;
        this.obligationFullName = oblFullName;
        this.currency = currency;
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
        columnNames.add("CODE");
        columnNames.add("QUANTITY");

        ArrayList columnValues = new ArrayList();
        columnValues.add("'" + this.getObligationFullName() + "'");
        columnValues.add("'" + this.getCouponDate().replace('-', '/') + "'");
        columnValues.add(this.getCouponSum());
        columnValues.add(this.obligationOwner.getUserId());
        columnValues.add("'" + this.getCurrency() + "'");
        columnValues.add("'" + this.obligationCode + "'");
        columnValues.add("'" + this.quantity+ "'");

        dbWorker.insertRecord(columnNames, columnValues, "Obligation");
    }


    public void updateObligation(HashMap<String, String> updateMap, HashMap<String, String> reqMap ){
        DBWorker dbWorker = new DBWorker();
        String stringUpdate;
        stringUpdate = "Update OBLIGATION SET ";

        for (String key: updateMap.keySet()){
            stringUpdate = stringUpdate + key + "= '" + updateMap.get(key) + "',";
        }

        stringUpdate = stringUpdate.substring(0, stringUpdate.length()-1) + " WHERE ";


        for (String key: reqMap.keySet()){
            stringUpdate = stringUpdate + key + "= '" + reqMap.get(key) + "' AND ";
        }
        stringUpdate = stringUpdate.substring(0, stringUpdate.length()-5);

        dbWorker.updateRecord(stringUpdate);
    }

    public void setObligationInfo(){
        JSONArray tempArray;
        if(dataArray.size()>0){
            for (int i=0; i<dataArray.size(); i++){
                tempArray = (JSONArray) dataArray.get(i);
                obligationInfoMap.put(tempArray.get(0).toString(), new ObligationInfo(tempArray.get(0).toString(), tempArray.get(1).toString(), tempArray.get(2).toString()));
            }
        }else {
            this.errorfFlg = true;
            this.errorMessage = "Не удалось найти введенную вами облигацию: " + this.obligationCode + ". Введите другой код облигации";
        }
    }



    public String getCurrency() {
        if(currency == null){
            currency = obligationInfoMap.get("FACEUNIT").getValue();
        }
        return currency;
    }

    public String getCouponSum() {
        if(couponSum == null){
            couponSum = obligationInfoMap.get("COUPONVALUE").getValue();
        }
        return couponSum;
    }

    public String getObligationFullName() {
        if(obligationFullName == null){
            obligationFullName = obligationInfoMap.get("NAME").getValue();
        }
        return obligationFullName;
    }

    public String getCouponDate() {
        if(couponDate == null){
            couponDate = obligationInfoMap.get("COUPONDATE").getValue();
        }
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

    public String getObligationCode(){
        return this.obligationCode;
    }

    public Integer getQuantity(){
        return this.quantity;
    }

    public String getConvertedCurrency(){
        String convertedCurrency;
        if(this.currency == null){
            convertedCurrency = obligationInfoMap.get("FACEUNIT").getValue();
        }else{
            convertedCurrency = this.currency;
        }
        if(convertedCurrency.equals("SUR")){
            return "RUB";
        }else{
            return convertedCurrency;
        }
    }

    public String getConvertedCouponDate(){
        if(couponDate !=null){
            return (this.couponDate.substring(this.couponDate.length()-2) + "-"+
                    this.couponDate.substring(5, 7) +"-" + this.couponDate.substring(0,4)).replace("-", ".");

        }else{
            return obligationInfoMap.get("COUPONDATE").getValue().substring(this.couponDate.length()-2) + "."+
                    obligationInfoMap.get("COUPONDATE").getValue().substring(5, 7) +"." + obligationInfoMap.get("COUPONDATE").getValue().substring(0,4);
        }

    }
}

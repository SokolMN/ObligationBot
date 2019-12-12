import org.json.simple.JSONArray;

import java.util.HashMap;
import java.util.Hashtable;

public class ObligationClass {

    private JSONArray columnArray;
    private JSONArray dataArray;
    private String obligationFullName;
    private String couponSum;
    private String currency;
    private String couponDate;
    HashMap <String, ObligationInfo> obligationInfoMap = new HashMap();

    public ObligationClass(JSONArray columnArray, JSONArray dataArray) {
       this.columnArray = columnArray;
       this.dataArray = dataArray;
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
}

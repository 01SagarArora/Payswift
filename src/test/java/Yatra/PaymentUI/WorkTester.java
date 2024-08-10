package Yatra.PaymentUI;

import org.json.JSONObject;

public class WorkTester {

    public static void main(String[] args) throws  Exception{
        String response = "{\n" +
                "\"head\":{ \n" +
                "      \"responseTimestamp\":\"71828299922\",\n" +
                "      \"version\":\"v1\" },\n" +
                "\"body\":{ \n" +
                "      \"panUniqueReference\":\"abbb-abbd-xyxx-tyzl\", \n" +
                "      \"cardScheme\" : \"MASTER\", \n" +
                "      \"cardType\": \"CC\", \n" +
                "      \"displayName\": \"ICICI BANK\",\n" +
                "      \"issuingBankName\":\"ICICI BANK\", \n" +
                "      \"issuingBankCode\":\"ICICI\",\n" +
                "      \"cardSuffix\":\"1234\",\n" +
                "      \"cin\":\"\", \n" +
                "      \"gcin\":\"\"} \n" +
                "}\n" +
                "\n" +
                " \n";
        JSONObject jsonObject = new JSONObject(response);
        System.out.println(jsonObject);

    }
}

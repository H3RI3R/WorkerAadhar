package antiCaptcha;


import org.json.JSONException;
import org.json.JSONObject;

public class BalanceResponse {
    private Integer errorId;
    private String errorCode;
    private String errorDescription;
    private Double balance;
    private Double captchaCredits;

    public BalanceResponse(JSONObject json) {
        errorId = JsonHelper.extractInt(json, "errorId");

        if (errorId != null) {
            if (errorId.equals(0)) {
                balance = JsonHelper.extractDouble(json, "balance");

                try {
                    captchaCredits =   json.getDouble("captchaCredits");
                } catch (JSONException e1) {
                    captchaCredits = Double.valueOf(0);
                }

            } else {
                errorCode = JsonHelper.extractStr(json, "errorCode");
                errorDescription = JsonHelper.extractStr(json, "errorDescription");
            }
        }
        else
        {
            DebugHelper.out("Unknown error", DebugHelper.Type.ERROR);
        }
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription == null ? "(no error description)" : errorDescription;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getCreditsBalance() {
        return captchaCredits;
    }

    public Integer getErrorId() {
        return errorId;
    }
}
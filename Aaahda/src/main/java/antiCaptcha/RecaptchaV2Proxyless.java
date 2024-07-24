package antiCaptcha;



import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class RecaptchaV2Proxyless extends AnticaptchaBase implements IAnticaptchaTaskProtocol {
    private URL websiteUrl;
    private String websiteKey;
    private String websiteSToken;
    Boolean isInvisible;
    String recaptchaDataSValue;

    public void setWebsiteUrl(URL websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setWebsiteKey(String websiteKey) {
        this.websiteKey = websiteKey;
    }

    public void setWebsiteSToken(String websiteSToken) {
        this.websiteSToken = websiteSToken;
    }

    public void setIsInvisible(Boolean isInvisible) { this.isInvisible = isInvisible; }

    public void setRecaptchaDataSValue(String recaptchaDataSValue) { this.recaptchaDataSValue = recaptchaDataSValue; }

    @Override
    public JSONObject getPostData() {
        JSONObject postData = new JSONObject();

        try {
            postData.put("type", "NoCaptchaTaskProxyless");
            postData.put("websiteURL", websiteUrl.toString());
            postData.put("websiteKey", websiteKey);
            postData.put("websiteSToken", websiteSToken);
            postData.put("recaptchaDataSValue", recaptchaDataSValue);
            postData.put("isInvisible", isInvisible);
        } catch (JSONException e) {
            DebugHelper.out("JSON compilation error: " + e.getMessage(), DebugHelper.Type.ERROR);

            return null;
        }

        return postData;
    }

    @Override
    public TaskResultResponse.SolutionData getTaskSolution() {
        return taskInfo.getSolution();
    }
}

package antiCaptcha;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class FunCaptchaProxyless extends AnticaptchaBase implements IAnticaptchaTaskProtocol {

    private URL websiteUrl;
    private String websitePublicKey;
    private String apiSubdomain;
    private String dataBlob;


    @Override
    public JSONObject getPostData() {
        JSONObject postData = new JSONObject();


        try {
            postData.put("type", "FunCaptchaTaskProxyless");
            postData.put("websiteURL", websiteUrl);
            postData.put("websitePublicKey", websitePublicKey);
            if (!this.apiSubdomain.equals(null)) {
                postData.put("funcaptchaApiJSSubdomain", this.apiSubdomain);
            }
            if (!this.dataBlob.equals(null)) {
                postData.put("data", this.dataBlob);
            }
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

    public void setWebsiteUrl(URL websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setWebsitePublicKey(String websitePublicKey) {
        this.websitePublicKey = websitePublicKey;
    }

    public void setApiSubdomain(String apiSubdomain) {
        this.apiSubdomain = apiSubdomain;
    }

    public void setDataBlob(String dataBlob) {
        this.dataBlob = dataBlob;
    }
}

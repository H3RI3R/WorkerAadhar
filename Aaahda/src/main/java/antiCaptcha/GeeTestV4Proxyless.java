package antiCaptcha;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class GeeTestV4Proxyless extends AnticaptchaBase implements IAnticaptchaTaskProtocol {
    private URL websiteUrl;
    private String websiteKey;
    private String geetestApiServerSubdomain;
    private JSONObject initParameters;

    public URL getWebsiteUrl() {
        return websiteUrl;
    }

    public String getWebsiteKey() {
        return websiteKey;
    }

    public String getGeetestApiServerSubdomain() {
        return geetestApiServerSubdomain;
    }

    public void setGeetestApiServerSubdomain(String geetestApiServerSubdomain) {
        this.geetestApiServerSubdomain = geetestApiServerSubdomain;
    }

    public void setWebsiteUrl(URL websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setWebsiteKey(String websiteKey) {
        this.websiteKey = websiteKey;
    }

    public void setInitParameters(JSONObject value) {
        this.initParameters = value;
    }

    @Override
    public JSONObject getPostData() {
        JSONObject postData = new JSONObject();

        try {
            postData.put("type", "GeeTestTaskProxyless");
            postData.put("websiteURL", websiteUrl.toString());
            postData.put("gt", websiteKey);
            postData.put("version", 4);
            if (!initParameters.equals(null)) postData.put("initParameters", initParameters);

            if (!geetestApiServerSubdomain.equals(null) && geetestApiServerSubdomain.length() > 0) {
                postData.put("geetestApiServerSubdomain", geetestApiServerSubdomain);
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
}

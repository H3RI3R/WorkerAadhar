package antiCaptcha;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;


public class JsonHelper {
    public static String extractStr(JSONObject json, String fieldName) {
        return extractStr(json, fieldName, null, false);
    }

    public static String extractStr(JSONObject json, String fieldName, Boolean silent) {
        return extractStr(json, fieldName, null, silent);
    }

    public static String extractStr(JSONObject json, String firstLevel, String secondLevel) {
        return extractStr(json, firstLevel, secondLevel, false);
    }

    public static String asString(JSONObject json) {
        try {
            return json.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static String extractStr(JSONObject json, String firstLevel, String secondLevel, Boolean silent) {
        try {
            return secondLevel == null
                    ? json.get(firstLevel).toString()
                    : json.getJSONObject(firstLevel).get(secondLevel).toString();
        } catch (JSONException e) {
            if (!silent) {
                String path = firstLevel + (secondLevel == null ? "" : "=>" + secondLevel);
                DebugHelper.jsonFieldParseError(path, json);
            }

            return null;
        }
    }

    public static Integer extractInt(JSONObject json, String firstLevel, String secondLevel, Boolean silent) {
        try {
            return secondLevel == null
                    ? json.getInt(firstLevel)
                    : json.getJSONObject(firstLevel).getInt(secondLevel);
        } catch (JSONException e) {
            if (!silent) {
                String path = firstLevel + (secondLevel == null ? "" : "=>" + secondLevel);
                DebugHelper.jsonFieldParseError(path, json);
            }

            return null;
        }
    }

    public static Integer extractInt(JSONObject json, String fieldName) {
        return extractInt(json, fieldName, false);
    }

    public static Integer extractInt(JSONObject json, String fieldName, boolean silent) {
        try {
            return json.getInt(fieldName);
        } catch (JSONException e1) {
            String str = extractStr(json, fieldName, silent);

            if (str == null) {
                if (!silent) {
                    DebugHelper.jsonFieldParseError(fieldName, json);
                }

                return null;
            }

            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e2) {
                DebugHelper.jsonFieldParseError(fieldName, json);

                return null;
            }
        }
    }

    public static Double extractDouble(JSONObject json, String fieldName) {
        try {
            return json.getDouble(fieldName);
        } catch (JSONException e1) {
            String str = extractStr(json, fieldName);

            if (str == null) {
                DebugHelper.jsonFieldParseError(fieldName, json);

                return null;
            }

            str = str.replace(',', '.');
            NumberFormat format = NumberFormat.getInstance(Locale.US);

            try {
                return format.parse(str).doubleValue();
            } catch (ParseException e2) {
                DebugHelper.jsonFieldParseError(fieldName, json);

                return null;
            }
        }
    }

    public static JSONArray extractJSONArray(JSONObject json, String firstLevel, String fieldName) {
        try {
            return json.getJSONObject(firstLevel).getJSONArray(fieldName);
        } catch (JSONException e) {
            return null;
        }
    }

    public static JSONObject extractJSONObject(JSONObject json, String firstLevel, String fieldName) {
        try {
            return json.getJSONObject(firstLevel).getJSONObject(fieldName);
        } catch (JSONException e) {
            return null;
        }
    }
}

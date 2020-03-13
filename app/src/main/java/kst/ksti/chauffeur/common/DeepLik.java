package kst.ksti.chauffeur.common;

import android.text.TextUtils;

import java.util.HashMap;

public class DeepLik {

    private static final String CHAUFFEUR_URL_SCHEME = "macaron://chauffeur";

    public DeepLik() {
    }

    public Object separator(String urlScheme) {
        if(TextUtils.isEmpty(urlScheme) || !urlScheme.startsWith(CHAUFFEUR_URL_SCHEME)) return "";

        String path = getPath(urlScheme);

        if(isQueryString(path)) {
            return getQueryString(path);
        } else {
            return path;
        }
    }

    private String getPath(String urlScheme) {
        return urlScheme.replace(CHAUFFEUR_URL_SCHEME, "");
    }

    private boolean isQueryString(String path) {
        return path.contains("?");
    }

    private Object getQueryString(String path) {
        if(!TextUtils.isEmpty(path)) {
            HashMap<String, String> hashMap = new HashMap<>();

            String tmp1[] = path.split("\\?");
            hashMap.put("path", tmp1[0]);

            String tmp2[] = tmp1[1].split("&");

            if (tmp2.length > 0) {
                for (String subTmp2 : tmp2) {
                    String tmp3[] = subTmp2.split("=");
                    hashMap.put(tmp3[0], tmp3[1]);
                }

                return hashMap;

            } else {
                return path;
            }
        }

        return "";
    }

}

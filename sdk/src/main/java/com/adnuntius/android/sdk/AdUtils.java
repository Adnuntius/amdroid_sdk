package com.adnuntius.android.sdk;

import static com.adnuntius.android.sdk.AdnuntiusEnvironment.andemu;

import java.util.Map;

final class AdUtils {
    private static final String JS_SHIM =
        "var adnSdkShim = new Object();\n" +
        "adnSdkShim.onDimsEvent = function(type, response) {\n" +
        "   if (response.hasOwnProperty('ads') && response.ads[0]) {\n" +
        "       var ad = response.ads[0]\n" +
        "       if (ad.hasOwnProperty('dims') && ad.hasOwnProperty('definedDims')) {\n" +

        "           intAndroidAdnuntius.onComplete(\n" +
        "               type,\n" +
        "               response.retAdCount || 0,\n" +
        "               ad.dims.w || 0,\n" +
        "               ad.dims.h || 0,\n" +
        "               ad.definedDims.w || 0,\n" +
        "               ad.definedDims.h || 0,\n" +
        "               ad.lineItemId || \"\",\n" +
        "               ad.creativeId || \"\"\n" +
        "           )\n" +
        "       }\n" +
        "   }\n" +
        "}\n" +
        "adnSdkShim.onVisible = function(response) {\n" +
        "   //console.log(\"onVisible:\" + JSON.stringify(response))\n" +
        "}\n" +
        "adnSdkShim.onRestyle = function(response) {\n" +
        "   //console.log(\"onRestyle:\" + JSON.stringify(response))\n" +
        "   adnSdkShim.onDimsEvent(\"restyle\", response)\n" +
        "}\n" +
        "adnSdkShim.onViewable = function(response) {\n" +
        "   //console.log(\"onViewable:\" + JSON.stringify(response))\n" +
        "}\n" +
        "adnSdkShim.onPageLoad = function(response) {\n" +
        "   //console.log(\"onPageLoad:\" + JSON.stringify(response))\n" +
        "   adnSdkShim.onDimsEvent(\"pageLoad\", response)\n" +
        "}\n" +
        "adnSdkShim.onNoMatchedAds = function(response) {\n" +
        "  intAndroidAdnuntius.onComplete(0);\n" +
        "}\n" +
        "adnSdkShim.onImpressionResponse = function(response) {\n" +
        "   //console.log(\"onImpressionResponse:\" + JSON.stringify(response))\n" +
        "}\n" +
        "adnSdkShim.onError = function(response) {\n" +
        // this is a XMLHttpRequest object with a failed response from adn.js
        "   if (response.hasOwnProperty('args') && response.args[0]) {\n" +
        "       var object = response.args[0]\n" +
        "       if ('response' in object && 'status' in object) {\n" +
        "           intAndroidAdnuntius.onFailure(object['status'], object['response'])\n" +
        "       }\n" +
        "   }\n" +
        "}\n";

    private AdUtils() {
    }

    static String getAdScript(AdnuntiusEnvironment env, final AdRequest request, final String adUnitsJson) {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> e : request.parentParameters().entrySet()) {
            if (builder.length() > 0) {
                builder.append(",\n");
            }
            builder.append("                   " + e.getKey() + ": \"" + e.getValue() + "\"");
        }

        return "<html>\n" +
                "<head>\n" +
                "   <style type=\"text/css\"> html, body { margin: 0px; padding: 0px; } </style>\n" +
                "   <script type=\"text/javascript\">\n" + JS_SHIM + "\n</script>\n" +
                "   <script type=\"text/javascript\" src=\"" + getAdnJsUrl(env) + "\" async></script>\n" +
                "</head>\n" +
                "   <body>\n" +
                "       <div id=\"adn-" + request.auId() + "\" style=\"display:none\"></div>\n" +
                "       <script type=\"text/javascript\">\n" +
                "           window.adn = window.adn || {}; adn.calls = adn.calls || [];\n" +
                "           adn.calls.push(function() {\n" +
                "               adn.request({\n" +
                "                   env: '" + env.name() + "',\n" +
                "                   sdk: 'android:" + BuildConfig.VERSION_NAME + "',\n" +
                "                   onPageLoad: adnSdkShim.onPageLoad,\n" +
                "                   onImpressionResponse: adnSdkShim.onImpressionResponse,\n" +
                "                   onNoMatchedAds: adnSdkShim.onNoMatchedAds,\n" +
                "                   onVisible: adnSdkShim.onVisible,\n" +
                "                   onViewable: adnSdkShim.onViewable,\n" +
                "                   onRestyle: adnSdkShim.onRestyle,\n" +
                "                   onError: adnSdkShim.onError,\n" +
                (request.useCookies() ? "" : "                   useCookies: false,\n")+
                (builder.length() == 0 ? "" : builder + ",\n") +
                "                   adUnits: [" + adUnitsJson + "]\n" +
                "               });\n" +
                "           });\n" +
                "       </script>" +
                "   </body>\n" +
                "</html>";
    }

    static String getAdnJsUrl(final AdnuntiusEnvironment env) {
        if (env == andemu) {
            return "http://10.0.2.2:8001/adn.src.js";
        } else {
            // currently all other envs use prod cdn
            return "https://cdn.adnuntius.com/adn.js";
        }
    }
}

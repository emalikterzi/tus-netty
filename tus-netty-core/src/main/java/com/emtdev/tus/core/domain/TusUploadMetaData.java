package com.emtdev.tus.core.domain;

import com.migcomponents.migbase64.Base64;

import java.util.HashMap;
import java.util.Map;

public class TusUploadMetaData {

    private final String clientValue;
    private final Map<String, String> map = new HashMap<String, String>();

    public TusUploadMetaData(String value) {
        this.clientValue = value;

        if (value == null || value.equals("")) {
            return;
        }

        String[] values = value.split(",");
        for (String each : values) {
            String[] keyPair = each.split("\\s+");
            if (keyPair.length == 1) {
                map.put(keyPair[0].trim(), "");
            } else {
                map.put(keyPair[0].trim(), new String(Base64.decode(keyPair[1].trim())));
            }
        }
    }

    public String getClientValue() {
        return clientValue;
    }

    public String getValue(String field) {
        return map.get(field);
    }
}

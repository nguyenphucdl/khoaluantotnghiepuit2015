/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traveleescripts;

import java.util.*;

/**
 *
 * @author nguyenphucuit
 */
public class ApiKeyPools {

    private List<String> apiKeyPools = null;
    private String keyPrefix = null;

    public ApiKeyPools(String keyPrefix) {
        apiKeyPools = new Vector<String>();
        
        Configuration conf = Configuration.getInstance();
        Properties props = conf.getProps();
        Enumeration keys = props.propertyNames();
        
        
        String conf_name = null, conf_value = null;
        while (keys.hasMoreElements()) {
            conf_name = (String)keys.nextElement();
            
            if(conf_name.startsWith(keyPrefix)) {
                String value = (String)props.getProperty(conf_name);
                
                apiKeyPools.add(value);
            }            
        }
    }

    public String getRandom() {
        int idx = Utilities.randInt(1, apiKeyPools.size() -1);
        return apiKeyPools.get(idx);
    }
}

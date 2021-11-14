package com.swannyscode;

import java.util.Properties;

public final class Constants {
    private static Properties properties = null;

    private synchronized static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try{
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("properties.txt"));
            }catch(Exception e){
                e.printStackTrace();
                throw new RuntimeException("Couldn't load properties file");
            }
        }
        return properties;
    }

    public static int getIntConstant(String constant) {
        String c = getProperties().getProperty(constant);
        try {
            return Integer.parseInt(c);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Loaded constant (" + constant + ") isn't an integer");
        }
    }
}

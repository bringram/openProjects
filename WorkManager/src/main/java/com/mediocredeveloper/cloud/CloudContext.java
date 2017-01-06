package com.mediocredeveloper.cloud;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a global mechanism for looking up our objects.
 */
public class CloudContext {
    public static volatile Map<String,Object> ctx = new ConcurrentHashMap<>();

    /**
     * Lookup an object.
     * @param group The group name the Cloud object was created with.
     * @param name The name the Cloud object was created with.
     * @param clazz The Class type.
     * @param <T> The generic type same as provied {@param clazz} parameter.
     * @return
     */
    public static <T> T lookup(String group, String name, Class<T> clazz) {
        Object obj = ctx.get(getName(group, name, clazz));

        if(obj == null){
            return null;
        }

        return clazz.cast(obj);
    }

    /**
     * Register an object.
     * @param group The group name the Cloud object was created with.
     * @param name The name the Cloud object was created with.
     * @param clazz The class type for the provided {@param obj}.
     * @param obj The object to register.
     */
    public static void register(String group, String name, Class clazz, Object obj) {
        String objName = getName(group, name, clazz);
        if(lookup(group, name, clazz) != null){
            throw new IllegalStateException("Cannot re-register an object -> "+objName);
        }
        ctx.put(getName(group, name, clazz), obj);
    }

    private static String getName(String group, String name, Class type){
        return type.getCanonicalName()+ "." + group + "." + name + ".";
    }
}

package io.chaofan.chunklauncher.auth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;

import io.chaofan.chunklauncher.util.ClassUtil;

public class AuthType {

    private static List<AuthType> values = new ArrayList<AuthType>();
    private static boolean authTypeInited = false;

    private String name;
    private Class<?> auth;
    private String alias;

    private AuthType(Class<?> auth) {
        try {
            this.name = (String)auth.getDeclaredMethod("getAuthTypeName").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.auth = auth;

        try {
            alias = (String)auth.getDeclaredMethod("getAlias").invoke(null);
            values.add(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerAuth newInstance(String name, String pass) {
        try {
            Constructor<?> constructor = auth.getConstructor(String.class, String.class);
            ServerAuth result = (ServerAuth)constructor.newInstance(name, pass);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString() {
        return name;
    }

    public String value() {
        return alias;
    }

    private static void initAuthType() {
        new AuthType(MinecraftYggdrasilServerAuth.class);
        new AuthType(OfflineServerAuth.class);

        String targetPackage = "io.chaofan.chunklauncher.auth";

        addAllAuthClasses(ClassUtil.getClassesFromPackage(targetPackage, false));

        File[] files = new File(".").listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".jar")) {
                    if (!ClassUtil.isInClassPath(f.getPath())) {
                        addAllAuthClasses(ClassUtil.getClassesFromPackage(targetPackage, f.getPath(), true));
                    }
                }
            }
        }
    }

    private static void addAllAuthClasses(Class<?>[] authClasses) {
        for(Class<?> authClass : authClasses) {
            if(authClass.getSuperclass() != null &&
                authClass.getSuperclass().equals(ServerAuth.class) &&
                !authClass.equals(OfflineServerAuth.class) &&
                !authClass.equals(MinecraftYggdrasilServerAuth.class)) {
                new AuthType(authClass);
            }
        }
    }

    public static AuthType valueOf(String value) {
        if(!authTypeInited) {
            initAuthType();
            authTypeInited = true;
        }
        for(AuthType at : values) {
            if(at.value().equals(value)) {
                return at;
            }
        }
        if(values.size() > 0)
            return values.get(0);
        return null;
    }

    public static List<AuthType> values() {
        if(!authTypeInited) {
            initAuthType();
            authTypeInited = true;
        }
        return values;
    }

}
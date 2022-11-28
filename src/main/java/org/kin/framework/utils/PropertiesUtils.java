package org.kin.framework.utils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author huangjianqin
 * @date 2019/7/6
 */
public class PropertiesUtils {
    /**
     * 读取properties文件
     * 如果字符串是file:开头, 则是基于相对路径读取properties文件
     * 否则从class path读取properties文件
     */
    public static Properties loadProperties(String propertyFileName) {
        // disk path
        if (propertyFileName.startsWith("file:")) {
            propertyFileName = propertyFileName.substring("file:".length());
            return loadFileProperties(propertyFileName);
        } else {
            return loadClassPathProperties(propertyFileName);
        }
    }

    /**
     * 从class path读取properties文件
     */
    public static Properties loadClassPathProperties(String propertyFileName) {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFileName);
            if (in == null) {
                return null;
            }

            Properties prop = new Properties();
            prop.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return prop;
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    ExceptionUtils.throwExt(e);
                }
            }
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 基于相对路径读取properties文件
     */
    public static Properties loadFileProperties(String propertyFileName) {
        InputStream in = null;
        try {
            // load file location, disk
            File file = new File(propertyFileName);
            if (!file.exists()) {
                return null;
            }

            URL url = new File(propertyFileName).toURI().toURL();
            in = new FileInputStream(url.getPath());

            Properties prop = new Properties();
            prop.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return prop;
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    ExceptionUtils.throwExt(e);
                }
            }
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 基于相对路径写properties文件
     */
    public static boolean writeProperties(Properties properties, String filePathName) {
        return writeProperties(properties, filePathName, null);
    }

    /**
     * 基于相对路径写properties文件
     */
    public static boolean writeProperties(Properties properties, String filePathName, String comment) {
        FileOutputStream out = null;
        try {

            // mk file
            File file = new File(filePathName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }

            // write data
            out = new FileOutputStream(file, false);
            properties.store(new OutputStreamWriter(out, StandardCharsets.UTF_8), comment);
            return true;
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    ExceptionUtils.throwExt(e);
                }
            }
        }

        return false;
    }

    /**
     * 加载properties内容
     */
    public static Properties loadPropertiesContent(String content) {
        return loadPropertiesContent(null, content);
    }

    /**
     * 加载properties内容, 添加到目标properties, 或者新建一个properties
     */
    public static Properties loadPropertiesContent(Properties target, String content) {
        if (Objects.isNull(target)) {
            target = new Properties();
        }
        try {
            target.load(new StringReader(content));
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        return target;
    }

    /**
     * 将properties转换成properties content string
     */
    public static String writePropertiesContent(Properties target) {
        return writePropertiesContent(target, null);
    }

    /**
     * 将properties转换成properties content string
     */
    public static String writePropertiesContent(Properties target, String comment) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            target.store(pw, comment);

            return sw.toString();
        } catch (Exception e) {
            ExceptionUtils.throwExt(e);
        }

        return "";
    }

    /**
     * 依据{@link  ConfigurationProperties}将{@link Properties}转换成{@code type}实例
     *
     * @param properties properties
     * @param type       config class
     * @param <T>        config type
     * @return config instance
     */
    public static <T> T toBean(Properties properties, Class<T> type) {
        //new
        T instance = ClassUtils.instance(type);
        //存储property key name与赋值逻辑的映射
        Map<String, Consumer<Object>> propKey2Setter = new HashMap<>(16);
        String prefix = "";
        ConfigurationProperties anno = type.getAnnotation(ConfigurationProperties.class);
        if (Objects.nonNull(anno)) {
            prefix = anno.value();
        }

        //遍历所有字段
        for (Field field : ClassUtils.getAllFields(type)) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) ||
                    Modifier.isStatic(modifiers)) {
                //过滤掉static | final
                continue;
            }

            String propKey = StringUtils.isBlank(prefix) ? "" : prefix + ".";
            anno = field.getAnnotation(ConfigurationProperties.class);
            if (Objects.nonNull(anno)) {
                propKey += anno.value();
            } else {
                propKey += field.getName();
            }

            propKey2Setter.put(propKey, o -> {
                Class<?> propValClass = o.getClass();
                Class<?> fieldType = field.getType();
                if (fieldType.isAssignableFrom(propValClass)) {
                    //类型兼容
                    ClassUtils.setFieldValue(instance, field, o);
                } else {
                    //转string
                    ClassUtils.setFieldValue(instance, field, ClassUtils.convertStr2PrimitiveObj(fieldType, o.toString()));
                }
            });
        }

        //遍历所有方法
        for (Method method : ClassUtils.getAllMethods(type)) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers) ||
                    Modifier.isAbstract(modifiers) ||
                    Modifier.isStatic(modifiers)) {
                //过滤掉非public | abstract | static
                continue;
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1) {
                //仅仅支持一个参数
                continue;
            }

            anno = method.getAnnotation(ConfigurationProperties.class);
            if (Objects.isNull(anno)) {
                continue;
            }
            String propKey = StringUtils.isBlank(prefix) ? "" : prefix + ".";
            propKey += anno.value();

            propKey2Setter.put(propKey, o -> {
                try {
                    Class<?> propValClass = o.getClass();
                    Class<?> paramType = paramTypes[0];
                    if (paramType.isAssignableFrom(propValClass)) {
                        //类型兼容
                        method.invoke(instance, o);
                    } else {
                        //转string
                        method.invoke(instance, o.toString());
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    ExceptionUtils.throwExt(e);
                }
            });
        }

        //赋值
        Enumeration<?> iterator = properties.propertyNames();
        while (iterator.hasMoreElements()) {
            Object propKey = iterator.nextElement();

            Consumer<Object> setter = propKey2Setter.get(propKey.toString());
            if (Objects.isNull(setter)) {
                continue;
            }

            //仅支持基础类型和带ConfigurationProperties的方法
            setter.accept(properties.get(propKey));
        }

        return instance;
    }
}

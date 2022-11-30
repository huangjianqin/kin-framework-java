package org.kin.framework.utils;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2019/7/6
 */
public class PropertiesUtils {
    /** properties key分隔离 */
    public static final String PROPERTIES_KEY_SEPARATOR = ".";
    /** properties key分隔离 */
    public static final String PROPERTIES_KEY_SEPARATOR_REGEX = "\\.";

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
     * 将{@link Properties}转换成{@link Map<String,Object>}
     */
    public static Map<String, Object> toStrObjMap(Properties properties) {
        Map<String, Object> map = new HashMap<>(properties.size());
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Object key = entry.getKey();
            if (!String.class.equals(key.getClass())) {
                //key不是string的过滤掉
                continue;
            }

            map.put(key.toString(), entry.getValue());
        }

        return map;
    }

    /**
     * 将{@link Properties}转换多层嵌套map
     */
    public static Map<String, Object> toMultiLvMap(Properties properties) {
        return toMultiLvMap(toStrObjMap(properties));
    }

    /**
     * 将A.B.C为key的map转换多层嵌套map
     */
    public static Map<String, Object> toMultiLvMap(Map<String, Object> properties) {
        Map<String, Object> multiLvMap = new HashMap<>(8);
        map2MultiLvMap(multiLvMap, properties);
        return multiLvMap;
    }

    /**
     * 将A.B.C为key的map转换多层嵌套map
     * 尾递归,提搞性能
     */
    @SuppressWarnings("unchecked")
    private static void map2MultiLvMap(Map<String, Object> rootLvMap, Map<String, Object> properties) {
        for (String key : properties.keySet()) {
            if (key.contains(PROPERTIES_KEY_SEPARATOR)) {
                String[] split = key.split(PROPERTIES_KEY_SEPARATOR_REGEX, 2);
                Map<String, Object> nextLv = rootLvMap.containsKey(split[0]) ?
                        (Map<String, Object>) rootLvMap.get(split[0]) : new HashMap<>(4);
                rootLvMap.put(split[0], nextLv);
                kv2MultiLvMap(nextLv, split[1], properties.get(key));
            } else {
                rootLvMap.put(key, properties.get(key));
            }
        }
    }

    /**
     * 将A.B.C形式的key和value转换成map转换多层嵌套map
     * 尾递归,提搞性能
     */
    @SuppressWarnings("unchecked")
    private static void kv2MultiLvMap(Map<String, Object> nowLvMap, String key, Object value) {
        if (key.contains(PROPERTIES_KEY_SEPARATOR)) {
            String[] split = key.split(PROPERTIES_KEY_SEPARATOR_REGEX, 2);
            Map<String, Object> nextLevel = nowLvMap.containsKey(split[0]) ?
                    (Map<String, Object>) nowLvMap.get(split[0]) : new HashMap<>(4);
            if (!nowLvMap.containsKey(split[0])) {
                nowLvMap.put(split[0], nextLevel);
            }
            kv2MultiLvMap(nextLevel, split[1], value);
        } else {
            nowLvMap.put(key, value);
        }
    }

    /**
     * 依据{@link  ConfigurationProperties}将{@link Properties}转换成{@code type}实例
     *
     * @param properties properties
     * @param type       config class
     * @param <T>        config type
     * @return config instance
     */
    public static <T> T toPropertiesBean(Properties properties, Class<T> type) {
        return toPropertiesBean(toMultiLvMap(properties), type);
    }

    /**
     * 依据{@link  ConfigurationProperties}将{@link Properties}转换成{@code type}实例
     *
     * @param lvMap properties多层嵌套map
     * @param type  config class
     * @param <T>   config type
     * @return config instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T toPropertiesBean(Map<String, Object> lvMap, Class<T> type) {
        //当前层的kvs
        //new
        T instance = ClassUtils.instance(type);

        ConfigurationProperties anno = type.getAnnotation(ConfigurationProperties.class);
        if (Objects.nonNull(anno)) {
            lvMap = getTargetLvMap(lvMap, anno.value());
        }

        //遍历所有字段
        for (Field field : ClassUtils.getAllFields(type)) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) ||
                    Modifier.isStatic(modifiers)) {
                //过滤掉static | final
                continue;
            }

            Class<?> fieldType = field.getType();
            String propKey = field.getName();

            Object value = lvMap.get(propKey);
            if (Objects.isNull(value)) {
                continue;
            }

            Class<?> vClass = value.getClass();

            if (Object.class.equals(vClass)) {
                //object, 直接赋值
                ClassUtils.setFieldValue(instance, field, value);
            } else if (ClassUtils.isPrimitiveType(fieldType)) {
                //基础类型
                if (ClassUtils.isAssignable(fieldType, vClass)) {
                    //类型兼容
                    ClassUtils.setFieldValue(instance, field, value);
                } else {
                    //string转换成对应类型
                    ClassUtils.setFieldValue(instance, field, ClassUtils.convertStr2PrimitiveObj(fieldType, value.toString()));
                }
            } else if (ClassUtils.isCollectionOrMapType(fieldType)) {
                //集合类型, list, array, set, map
                if (Collection.class.isAssignableFrom(vClass)) {
                    //list, array, set
                    ClassUtils.setFieldValue(instance, field, toPropsBeanCollection(fieldType, field.getGenericType(), value));
                } else if (Map.class.isAssignableFrom(vClass)) {
                    ClassUtils.setFieldValue(instance, field, toPropsBeanMap(field.getGenericType(), value));
                }
            } else {
                //其他类型
                if (Map.class.isAssignableFrom(vClass)) {
                    //同时, value类型是map, 认为是嵌套property bean
                    ClassUtils.setFieldValue(instance, field, toPropertiesBean((Map<String, Object>) value, fieldType));
                }
            }
        }


        return instance;
    }

    /**
     * 直接获取某一层的数据map, 比如A.B.C, A.B.C1, 此时{@code key}为A.B, 返回的是以C和C1为key的map
     *
     * @param multiLvMap properties多层嵌套map
     * @param key        某一层 property的key
     * @return 指定层的数据map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getTargetLvMap(Map<String, Object> multiLvMap, String key) {
        Map<String, Object> targetLvMap = multiLvMap;
        for (String keySplit : key.split(PROPERTIES_KEY_SEPARATOR_REGEX)) {
            if (targetLvMap.containsKey(keySplit)) {
                targetLvMap = (Map<String, Object>) targetLvMap.getOrDefault(keySplit, Collections.emptyMap());
            } else {
                return Collections.emptyMap();
            }
        }

        return targetLvMap;
    }

    /**
     * 将指定实例转换成集合或数组实例
     *
     * @param fieldType        字段类型, 即集合或数组类型
     * @param fieldGenericType 字段泛型参数
     * @param value            集合实例
     * @return 集合或数组实例
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object toPropsBeanCollection(Class<?> fieldType, Type fieldGenericType, Object value) {
        if (fieldType.isArray()) {
            //数组
            Class<?> componentType = fieldType.getComponentType();
            if (ClassUtils.isCollectionOrMapType(componentType)) {
                // TODO: 2022/11/30 看看yaml支不支持嵌套
                //集合嵌套, 不处理, 返回空数组
                return Array.newInstance(componentType, 0);
            }

            List<?> list = (List<?>) value;
            Object[] arr = (Object[]) Array.newInstance(componentType, list.size());
            //是否是object[]
            boolean isObjItem = Object.class.equals(componentType);
            if (ClassUtils.isPrimitiveType(componentType) || isObjItem) {
                //primitive, 直接赋值, 如果精度不对, 报错就好
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (isObjItem) {
                        arr[i] = item;
                    } else {
                        arr[i] = ClassUtils.convertStr2PrimitiveObj(item.getClass(), item.toString());
                    }
                }
            } else {
                //property bean
                for (int i = 0; i < list.size(); i++) {
                    //item是map<String, Object>
                    Object item = list.get(i);
                    check(Map.class, item.getClass());
                    arr[i] = toPropertiesBean((Map<String, Object>) item, componentType);
                }
            }

            return arr;
        } else {
            if (!(fieldGenericType instanceof ParameterizedType)) {
                //不带泛型参数, 直接copy返回
                if (Collection.class.isAssignableFrom(fieldType) ||
                        List.class.isAssignableFrom(fieldType)) {
                    //collection list
                    return new ArrayList<>((Collection) value);
                }

                if (Set.class.isAssignableFrom(fieldType)) {
                    //set
                    return new HashSet<>((Collection) value);
                }

                throw new IllegalStateException("encounter unexpected exception");
            }

            //带泛型参数
            ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;

            Collection collection = null;
            if (Collection.class.isAssignableFrom(fieldType) ||
                    List.class.isAssignableFrom(fieldType)) {
                //collection list
                collection = new ArrayList<>();
            }

            if (Set.class.isAssignableFrom(fieldType)) {
                //set
                collection = new HashSet<>();
            }

            if (Objects.nonNull(collection)) {
                List<?> list = (List<?>) value;
                Class<?> itemType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                if (ClassUtils.isCollectionOrMapType(itemType)) {
                    // TODO: 2022/11/30
                    //集合嵌套, 不处理
                    throw new UnsupportedOperationException();
                }

                if (Object.class.equals(itemType)) {
                    //object
                    collection.addAll(list);
                } else if (ClassUtils.isPrimitiveType(itemType)) {
                    //primitive
                    for (Object o : list) {
                        collection.add(ClassUtils.convertStr2PrimitiveObj(itemType, o.toString()));
                    }
                } else {
                    //property bean
                    for (Object item : list) {
                        //item是map<String, Object>
                        check(Map.class, item.getClass());
                        collection.add(toPropertiesBean((Map<String, Object>) item, itemType));
                    }
                }

                return collection;
            }
        }

        throw new IllegalStateException("encounter unexpected exception");
    }

    /**
     * 将指定实例转换成map
     *
     * @param fieldGenericType map字段泛型参数
     * @param value            map实例
     * @return map实例
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object toPropsBeanMap(Type fieldGenericType, Object value) {
        if (!(fieldGenericType instanceof ParameterizedType)) {
            //不带泛型参数, 直接返回
            return value;
        }

        //带泛型参数
        ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
        //key类型
        Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        if (!ClassUtils.isPrimitiveType(keyType)) {
            //key必须是基础类型
            throw new UnsupportedOperationException("properties bean map field key class must be primitive");
        }
        //value类型
        Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];

        if (ClassUtils.isCollectionOrMapType(valueType)) {
            // TODO: 2022/11/30
            //map嵌套, 不处理
            throw new UnsupportedOperationException();
        }

        Map map = new HashMap();
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();

            if (Object.class.equals(valueType)) {
                //object
                map.put(ClassUtils.convertStr2PrimitiveObj(keyType, k), v);
            } else if (ClassUtils.isPrimitiveType(valueType)) {
                //primitive
                map.put(ClassUtils.convertStr2PrimitiveObj(keyType, k), ClassUtils.convertStr2PrimitiveObj(valueType, v.toString()));
            } else {
                //property bean
                map.put(ClassUtils.convertStr2PrimitiveObj(keyType, k), toPropertiesBean((Map<String, Object>) v, valueType));
            }
        }

        return map;
    }

    /**
     * 检查{@code parent}是否可以assignable from{@code child}, 如果不能, 则抛异常
     *
     * @param parent 父类型
     * @param child  子类型
     */
    private static void check(Class<?> parent, Class<?> child) {
        if (!parent.isAssignableFrom(child)) {
            throw new IllegalStateException(String.format("expect class '%s', but actual is '%s'", parent.getName(), child.getName()));
        }
    }
}

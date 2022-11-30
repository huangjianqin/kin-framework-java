package org.kin.framework.utils;

import com.google.common.collect.ImmutableMap;
import org.ho.yaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2019/7/6
 */
public class YamlUtils {
    @SuppressWarnings("unchecked")
    public static YamlConfig loadYaml(String configPath) {
        //从classpath寻找
        URL url = Thread.currentThread().getContextClassLoader().getResource(configPath);
        if (url != null) {
            //返回多层嵌套map
            try {
                return new YamlConfig((Map<String, Object>) Yaml.load(url.openStream()));
            } catch (IOException e) {
                ExceptionUtils.throwExt(e);
            }
        } else {
            //从file path寻找
            try {
                //返回多层嵌套map
                return new YamlConfig((Map<String, Object>) Yaml.load(new File(configPath)));
            } catch (FileNotFoundException e) {
                ExceptionUtils.throwExt(e);
            }
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 加载yaml文件并转换为{@link Properties}
     *
     * @param configPath yaml文件路径
     * @return {@link Properties}实例
     */
    public static Properties loadYaml2Properties(String configPath) {
        return loadYaml(configPath).toProperties();
    }

    /**
     * 加载yaml文件并转换为{@code type}实例
     *
     * @param configPath yaml文件路径
     * @return T实例
     */
    public static <T> T loadYaml2Bean(String configPath, Class<T> type) {
        return loadYaml(configPath).toBean(type);
    }

    /**
     * 将{@link Properties}转换为yaml字符串
     *
     * @param properties properties
     * @return yaml字符串
     */
    public static String transfer2YamlStr(Properties properties) {
        return Yaml.dump(PropertiesUtils.toMultiLvMap(properties));
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 将yaml数据转换成{@link Properties}
     *
     * @param yamlData yaml数据
     * @return {@link Properties}实例
     */
    public static Properties transfer2Properties(Map<String, Object> yamlData) {
        Properties properties = new Properties();
        transfer2Properties(yamlData, properties, "");
        return properties;
    }

    /**
     * 将多层嵌套map转换成A.B.C的properties格式
     */
    private static void transfer2Properties(Map<String, Object> yaml, Properties properties, String keyHead) {
        for (String key : yaml.keySet()) {
            Object value = yaml.get(key);

            String propertiesKey = StringUtils.isBlank(keyHead) ? key : keyHead + "." + key;
            if (value instanceof Map) {
                transfer2Properties((Map<String, Object>) value, properties, propertiesKey);
            } else {
                if (Objects.isNull(value)) {
                    properties.put(propertiesKey, "");
                } else {
                    properties.put(propertiesKey, value);
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------


    /**
     * 内部类, 封装这yaml数据, 即多层嵌套Map
     */
    public static class YamlConfig {
        public static final YamlConfig EMPTY = new YamlConfig(Collections.emptyMap());

        public static YamlConfig empty() {
            return EMPTY;
        }

        /**
         * key -> string, value -> object
         */
        private Map<String, Object> yaml;

        public YamlConfig(Map<String, Object> yml) {
            this.yaml = ImmutableMap.copyOf(yml);
        }

        public YamlConfig(Properties properties) {
            this.yaml = PropertiesUtils.toMultiLvMap(properties);
        }

        /**
         * 转换成{@link Properties}实例
         *
         * @return {@link Properties}实例
         */
        public Properties toProperties() {
            return transfer2Properties(yaml);
        }

        /**
         * 转换成java bean
         *
         * @param type bean class
         * @param <T>  bean type
         * @return java bean
         */
        public <T> T toBean(Class<T> type) {
            return PropertiesUtils.toPropertiesBean(yaml, type);
        }

        /**
         * 转成yaml字符串
         *
         * @return yaml字符串
         */
        public String toYamlStr() {
            return Yaml.dump(yaml);
        }

        /**
         * 获取{@code key}对应的值, 可以是具体值或者Map, 即表示下一层
         *
         * @param key XX.YY.ZZ.....
         * @return value
         */
        public Object get(String key) {
            Map copy = new HashMap<>(yaml);
            String[] splitKeys = key.split(PropertiesUtils.PROPERTIES_KEY_SEPARATOR_REGEX);
            for (int i = 0; i < splitKeys.length; i++) {
                String splitKey = splitKeys[i];
                Object tmpValue = copy.get(splitKey);
                if (!(tmpValue instanceof Map)) {
                    if (i == splitKeys.length - 1) {
                        return tmpValue;
                    } else {
                        //异常
                        return null;
                    }
                }

                copy = new HashMap<>((Map) tmpValue);

            }

            return null;
        }
    }
}

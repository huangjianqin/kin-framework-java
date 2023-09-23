package org.kin.framework.collection;

import org.kin.framework.utils.IllegalFormatException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author huangjianqin
 * @date 2023/6/14
 */
public class MapConfigurationPropertiesTest {
    public static void main(String[] args) {
        ConfigurationProperties properties = new MapConfigurationProperties();
        properties.put("A", "A");
        properties.put("B", "A");
        properties.put("C", "A");
        System.out.println(properties);

        Map<String, String> map = new HashMap<>();
        map.put("A", "D");
        map.put("B", "F");
        map.put("C", "G");
        properties.putAll(map);

        System.out.println(properties.toMap());
        System.out.println(Objects.requireNonNull(properties.get("A")).toString());
        System.out.println(properties.get("D", "?"));

        System.out.println("-------------------------------------------------------------------------------------------");
        properties.put("number", "1");
        System.out.println(properties.getByte("number"));
        System.out.println(properties.getShort("number"));
        System.out.println(properties.getInt("number"));
        System.out.println(properties.getLong("number"));
        System.out.println(properties.getDouble("number"));

        properties.put("number1", Integer.MAX_VALUE + "");
//        System.out.println(properties.getByte("number1"));
//        System.out.println(properties.getShort("number1"));
        System.out.println(properties.getInt("number1"));
        System.out.println(properties.getLong("number1"));
        System.out.println(properties.getDouble("number1"));

        properties.put("number2", (Double.MAX_VALUE / 2) + "");
//        System.out.println(properties.getByte("number2"));
//        System.out.println(properties.getShort("number2"));
//        System.out.println(properties.getInt("number2"));
//        System.out.println(properties.getLong("number2"));
        System.out.println(properties.getDouble("number2"));

        properties.put("bool1", "1");
        System.out.println(properties.getBool("bool1"));

        properties.put("bool2", "true");
        System.out.println(properties.getBool("bool2"));

        properties.put("arr1", new String[]{"Item1", "Item2", "Item3"});
        System.out.println(Arrays.toString((Object[]) properties.get("arr1")));
        properties.put("arr2", "Item1,Item2,Item3");
        System.out.println(Arrays.toString(properties.get("arr2", (Function<Object, String[]>) o -> {
            if (o instanceof String) {
                return o.toString().split(",");
            } else if (o.getClass().isArray() && String.class.equals(o.getClass().getComponentType())) {
                return (String[]) o;
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a string array", "arr2"));
            }
        })));
    }
}

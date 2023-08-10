package org.kin.framework.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.kin.framework.collection.Tuple;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by huangjianqin on 2018/1/26.
 */
public class ClassUtils {
    public static final String CLASS_SUFFIX = ".class";
    /** 用于匹配内部类 */
    private static final Pattern INNER_PATTERN = Pattern.compile("\\$(\\d+).", Pattern.CASE_INSENSITIVE);
    /** 生成方法签名的参数命名 */
    public static final String METHOD_DECLARATION_ARG_NAME = "arg";
    /** key -> 泛型类型, value -> 第一个泛型类型参数 */
    private static final Map<Type, Class<?>> GENERIC_TYPES_CACHE = new ConcurrentHashMap<>();
    /** key -> 基础类型, value -> 包装类 */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new IdentityHashMap<>(8);
    /** key -> 包装类, value -> 基础类型 */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_TO_WRAPPER_MAP = new IdentityHashMap<>(8);
    /** {@link Object}的默认构造方法 */
    private static final Constructor<Object> OBJECT_CONSTRUCTOR;

    static {
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, char.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, double.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, float.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, long.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, short.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Void.class, void.class);

        for (Map.Entry<Class<?>, Class<?>> entry : PRIMITIVE_WRAPPER_TYPE_MAP.entrySet()) {
            PRIMITIVE_TYPE_TO_WRAPPER_MAP.put(entry.getValue(), entry.getKey());
        }

        //存在sun.reflect.ReflectionFactory, 才使用
        Constructor<Object> c = null;
        Class<?> reflectionFactoryClass = null;
        try {
            c = Object.class.getConstructor((Class[]) null);
            reflectionFactoryClass = Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass("sun.reflect.ReflectionFactory");
        } catch (Exception e) {
            // ignore
        }

        OBJECT_CONSTRUCTOR = c != null && reflectionFactoryClass != null ? c : null;
    }

    @FunctionalInterface
    private interface Matcher<T> {
        /**
         * @param c      基准
         * @param target 目标
         * @return true表示匹配
         */
        boolean match(Class<? extends T> c, Class<? extends T> target);
    }

    /**
     * classpath扫描配置
     */
    public static class ScanOptions {
        private boolean includeJar;
        private boolean includeInnerClass;

        public boolean isIncludeJar() {
            return includeJar;
        }

        public ScanOptions includeJar(boolean includeJar) {
            this.includeJar = includeJar;
            return this;
        }

        public boolean isIncludeInnerClass() {
            return includeInnerClass;
        }

        public ScanOptions includeInnerClass(boolean includeInnerClass) {
            this.includeInnerClass = includeInnerClass;
            return this;
        }
    }

    /**
     * 通过无参构造器实例化类
     */
    public static <T> T instance(Class<T> type) {
        if (type == null) {
            return null;
        }
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            ExceptionUtils.throwExt(e);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T instance(String classStr) {
        if (StringUtils.isBlank(classStr)) {
            return null;
        }
        try {
            Class<T> type = (Class<T>) Class.forName(classStr);
            return instance(type);
        } catch (ClassNotFoundException e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 寻找合适构造器并实例化
     * 如果不能根据参数类型精准匹配(等于{@code args}类型)构造方法, 则模糊匹配(重载)寻找合适的构造方法
     */
    public static <T> T instance(Class<T> type, Object... args) {
        if (type == null) {
            return null;
        }

        if (CollectionUtils.isEmpty(args)) {
            return instance(type);
        }

        try {
            Class<?>[] argClasses = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argClasses[i] = args[i].getClass();
            }
            Constructor<T> constructor = type.getDeclaredConstructor(argClasses);
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException e) {
            ExceptionUtils.throwExt(e);
        } catch (NoSuchMethodException e) {
            return slowInstance(type, args);
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 根据{@code cParamTypes}类型精准匹配构造方法并实例化
     */
    public static <T> T instance(Class<T> type, Class<?>[] cParamTypes, Object... args) {
        if (type == null) {
            return null;
        }

        if (CollectionUtils.isEmpty(cParamTypes)) {
            return instance(type);
        }

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(cParamTypes);
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException |
                NoSuchMethodException | InvocationTargetException e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 模糊匹配(重载)寻找合适的构造方法并实例化
     */
    @SuppressWarnings("unchecked")
    public static <T> T slowInstance(Class<T> type, Object... args) {
        try {
            Class<?>[] argClasses = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argClasses[i] = args[i].getClass();
            }
            Constructor<T> constructor = null;
            for (Constructor<?> candidate : type.getDeclaredConstructors()) {
                if (!Modifier.isPublic(candidate.getModifiers())) {
                    //非public
                    continue;
                }

                if (!isClassArrayMatch(candidate.getParameterTypes(), argClasses)) {
                    //参数不匹配
                    continue;
                }

                if (Objects.nonNull(constructor)) {
                    throw new IllegalArgumentException(String.format("find more than one %s constructor which parameter types is can assignable from %s",
                            type.getName(), classArrayToString(argClasses)));
                }

                constructor = (Constructor<T>) candidate;
            }
            if (Objects.isNull(constructor)) {
                throw new IllegalArgumentException(String.format("can not find any %s constructor which parameter types is can assignable from %s",
                        type.getName(), classArrayToString(argClasses)));
            }
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * {@link Class}数组toString
     * @param argTypes class array
     * @return string of class array
     */
    private static String classArrayToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * {@code s} class是否是{@code c} class的父类或接口
     * 一般用于匹配方法
     * @return  true表示{@code s} class是{@code c} class的父类或接口
     */
    public static boolean isClassArrayMatch(Class<?>[] s, Class<?>[] c) {
        if (s == null) {
            return c == null || c.length == 0;
        }

        if (c == null) {
            return s.length == 0;
        }

        if (s.length != c.length) {
            return false;
        }

        for (int i = 0; i < s.length; i++) {
            if (!s[i].isAssignableFrom(c[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据指定类名加载类
     */
    public static <T> Class<T> getClass(String className) {
        return getClass(className, true);
    }

    /**
     * 根据指定类名加载类
     *
     * @param initialize 是否对class进行初始化
     */
    public static <T> Class<T> getClass(String className, boolean initialize) {
        return getClass(className, initialize, ClassUtils.class.getClassLoader());
    }

    /**
     * 根据指定类名加载类
     *
     * @param initialize  是否对class进行初始化
     * @param classLoader 指定classloader
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String className, boolean initialize, ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(className, initialize, classLoader);
        } catch (ClassNotFoundException ex) {
            try {
                return (Class<T>) Class.forName(className, initialize, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                ExceptionUtils.throwExt(e);
            }
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 获取某个类的所有子类, 但不包括该类
     */
    public static <T> Set<Class<? extends T>> getSubClass(String packageName, Class<T> parent, boolean includeJar) {
        return getSubClass(packageName, parent, new ScanOptions().includeJar(includeJar));
    }

    /**
     * 获取某个类的所有子类, 但不包括该类
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> getSubClass(String packageName, Class<T> parent, ScanOptions options) {
        return scanClasspathAndFindMatch(packageName, parent,
                (c, target) -> !c.equals(target) && c.isAssignableFrom(target), options);
    }

    /**
     * 获取出现某注解的所有类, 包括抽象类和接口
     */
    public static <T> Set<Class<? extends T>> getAnnotationedClass(String packageName, Class<T> annotationClass, boolean includeJar) {
        return getAnnotationedClass(packageName, annotationClass, new ScanOptions().includeJar(includeJar));
    }

    /**
     * 获取出现某注解的所有类, 包括抽象类和接口
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> getAnnotationedClass(String packageName, Class<T> annotationClass, ScanOptions options) {
        if (annotationClass.isAnnotation()) {
            return scanClasspathAndFindMatch(packageName, annotationClass,
                    (c, target) -> {
                        if (target.isAnnotationPresent(c)) {
                            return true;
                        } else {
                            for (Field field : target.getDeclaredFields()) {
                                if (field.isAnnotationPresent(c)) {
                                    return true;
                                }
                            }

                            for (Method method : target.getDeclaredMethods()) {
                                if (method.isAnnotationPresent(c)) {
                                    return true;
                                }
                            }
                        }

                        return false;
                    }, options);
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("rawtypes")
    public static <T> Set<Class<? extends T>> scanClasspathAndFindMatch(String packageName, Class<T> c, Matcher matcher, ScanOptions options) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader staticClassLoader = ClassUtils.class.getClassLoader();
        ClassLoader[] classLoaders = contextClassLoader != null ?
                (staticClassLoader != null && contextClassLoader != staticClassLoader ?
                        new ClassLoader[]{contextClassLoader, staticClassLoader} : new ClassLoader[]{contextClassLoader})
                : new ClassLoader[0];

        Set<Class<? extends T>> subClasses = Sets.newLinkedHashSet();
        for (ClassLoader classLoader : classLoaders) {
            subClasses.addAll(scanClasspathAndFindMatch(classLoader, packageName, c, matcher, options));
        }

        return subClasses;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Set<Class<T>> scanClasspathAndFindMatch(ClassLoader contextClassLoader, String packageName, Class<T> c, Matcher matcher, ScanOptions options) {
        Set<Class<T>> subClasses = Sets.newLinkedHashSet();

        String packageResource = packageName.replaceAll("\\.", "/");
        try {
            Enumeration<URL> urls = contextClassLoader.getResources(packageResource);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("file".equals(url.getProtocol())) {
                    Path path = Paths.get(url.toURI());
                    Stream<Path> stream = Files.walk(path);
                    Set<Class<T>> classes = stream.filter(p -> !Files.isDirectory(p) && p.toString().endsWith(CLASS_SUFFIX))
                            .map(p -> {
                                URI uri = p.toUri();
                                String origin = uri.getPath();
                                //空包名要去掉root路径, 不然会解析出来的包名会包含部分classpath路径, 也就是无效了
                                //如果指定包名, 下面lastIndexOf(packageName)会过滤掉部分classpath路径
                                String className;
                                if (StringUtils.isBlank(packageName)) {
                                    origin = origin.substring(origin.indexOf(url.getPath()) + url.getPath().length());
                                    int endIndex = origin.lastIndexOf(CLASS_SUFFIX);
                                    className = origin.substring(0, endIndex);
                                    //把/替换成.
                                    className = className.replaceAll("/", ".");
                                } else {
                                    //把/替换成.
                                    origin = origin.replaceAll("/", ".");
                                    int startIndex = origin.lastIndexOf(packageName);
                                    int endIndex = origin.lastIndexOf(CLASS_SUFFIX);
                                    className = origin.substring(startIndex, endIndex);
                                }

                                if (StringUtils.isNotBlank(className) &&
                                        !INNER_PATTERN.matcher(className).find() &&
                                        (options.includeInnerClass || className.indexOf("$") <= 0)) {
                                    try {
                                        return (Class<T>) contextClassLoader.loadClass(className);
                                    } catch (ClassNotFoundException e) {
                                        ExceptionUtils.throwExt(e);
                                    }
                                }
                                return null;
                            })
                            .filter(ic -> !Objects.isNull(ic))
                            .filter(ic -> matcher.match(c, ic)).collect(Collectors.toSet());
                    subClasses.addAll(Sets.newHashSet(classes));
                } else if (options.includeJar && "jar".equals(url.getProtocol())) {
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = jarURLConnection.getJarFile();
                    Enumeration<JarEntry> jarEntries = jarFile.entries();
                    while (jarEntries.hasMoreElements()) {
                        JarEntry jarEntry = jarEntries.nextElement();
                        String entryName = jarEntry.getName();

                        if (jarEntry.isDirectory()) {
                            continue;
                        }

                        if (entryName.endsWith("/") || !entryName.endsWith(CLASS_SUFFIX)) {
                            continue;
                        }

                        if (!options.includeInnerClass && (INNER_PATTERN.matcher(entryName).find() || entryName.indexOf("$") > 0)) {
                            //过滤内部类
                            continue;
                        }

                        String className = entryName.replaceAll("/", ".");
                        className = className.substring(0, entryName.lastIndexOf(".class"));

                        try {
                            Class<T> type = (Class<T>) contextClassLoader.loadClass(className);
                            if (matcher.match(c, type)) {
                                subClasses.add(type);
                            }
                        } catch (ClassNotFoundException e) {
                            ExceptionUtils.throwExt(e);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            ExceptionUtils.throwExt(e);
        }

        return subClasses;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, String fieldName) {
        for (Field field : getAllFields(target.getClass())) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                try {
                    return (T) field.get(target);
                } catch (IllegalAccessException e) {
                    ExceptionUtils.throwExt(e);
                } finally {
                    field.setAccessible(false);
                }
            }
        }

        throw new IllegalStateException("encounter unknown error");
    }

    public static void setFieldValue(Object target, String fieldName, Object newValue) {
        for (Field field : getAllFields(target.getClass())) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                try {
                    field.set(target, newValue);
                } catch (IllegalAccessException e) {
                    ExceptionUtils.throwExt(e);
                } finally {
                    field.setAccessible(false);
                }
            }
        }
    }

    /**
     * 通过getter field实例设置值
     */
    public static Object getFieldValue(Object instance, Field field) {
        try {
            Method m = getterMethod(instance.getClass(), field);
            if (m != null) {
                return m.invoke(instance);
            } else {
                try {
                    field.setAccessible(true);
                    return field.get(instance);
                } finally {
                    field.setAccessible(false);
                }
            }

        } catch (Exception e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 通过setter field实例设置值
     */
    public static void setFieldValue(Object instance, Field field, Object value) {
        try {
            Method m = setterMethod(instance.getClass(), field);
            if (m != null) {
                m.invoke(instance, value);
            } else {
                try {
                    field.setAccessible(true);
                    field.set(instance, value);
                } finally {
                    field.setAccessible(false);
                }
            }
        } catch (Exception e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * 获取getter方法
     */
    public static Method getterMethod(Class<?> target, Field field) {
        try {
            String s = StringUtils.firstUpperCase(field.getName());
            if (isBoolean(field.getType())) {
                //如果是boolean, 先尝试is开头的getter方法, 找不到再尝试get开头
                return target.getMethod("is".concat(s));
            }
            return target.getMethod("get".concat(s));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * 获取getter方法
     */
    public static Method getterMethod(Field field) {
        return getterMethod(field.getDeclaringClass(), field);
    }

    /**
     * 获取setter方法
     */
    public static Method setterMethod(Class<?> target, Field field) {
        try {
            return target.getMethod("set".concat(StringUtils.firstUpperCase(field.getName())), field.getType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * 获取setter方法
     */
    public static Method setterMethod(Field field) {
        return setterMethod(field.getDeclaringClass(), field);
    }

    public static List<Field> getAllFields(Class<?> type) {
        return getFields(type, Object.class);
    }

    /**
     * 获取{@code type} -> parent的所有field
     * field顺序, 子类 -> 父类 -> 父父类
     */
    public static List<Field> getFields(Class<?> type, Class<?> parent) {
        if (type == null || parent == null) {
            return Collections.emptyList();
        }

        List<Field> fields = new ArrayList<>();
        while (!type.equals(parent)) {
            Collections.addAll(fields, type.getDeclaredFields());
            type = type.getSuperclass();
        }
        return fields;
    }

    public static List<Method> getAllMethods(Class<?> type) {
        return getMethods(type, Object.class);
    }

    /**
     * 获取{@code type} -> parent的所有method
     * method顺序, 子类 -> 父类 -> 父父类
     */
    public static List<Method> getMethods(Class<?> type, Class<?> parent) {
        if (type == null || parent == null) {
            return Collections.emptyList();
        }

        List<Method> methods = new ArrayList<>();
        while (!type.equals(parent)) {
            Collections.addAll(methods, type.getDeclaredMethods());
            type = type.getSuperclass();
        }
        return methods;
    }

    public static List<Class<?>> getAllClasses(Class<?> type) {
        return getClasses(type, Object.class);
    }

    /**
     * 获取{@code type} -> parent的所有class
     */
    public static List<Class<?>> getClasses(Class<?> type, Class<?> parent) {
        if (!parent.isAssignableFrom(type)) {
            throw new IllegalStateException(String.format("%s is not super class of %s", parent.getName(), type.getName()));
        }
        List<Class<?>> classes = new ArrayList<>();
        while (Objects.nonNull(type) && !type.equals(parent)) {
            classes.add(type);
            type = type.getSuperclass();
        }
        return classes;
    }

    /**
     * 获取默认值
     */
    public static Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (Boolean.TYPE.equals(type)) {
                return false;
            } else if (Byte.TYPE.equals(type)) {
                return 0;
            } else if (Character.TYPE.equals(type)) {
                return "";
            } else if (Short.TYPE.equals(type)) {
                return 0;
            } else if (Integer.TYPE.equals(type)) {
                return 0;
            } else if (Long.TYPE.equals(type)) {
                return 0L;
            } else if (Float.TYPE.equals(type)) {
                return 0.0F;
            } else if (Double.TYPE.equals(type)) {
                return 0.0D;
            }
        } else {
            if (String.class.equals(type)) {
                return "";
            } else if (Boolean.class.equals(type)) {
                return false;
            } else if (Byte.class.equals(type)) {
                return 0;
            } else if (Character.class.equals(type)) {
                return "";
            } else if (Short.class.equals(type)) {
                return 0;
            } else if (Integer.class.equals(type)) {
                return 0;
            } else if (Long.class.equals(type)) {
                return 0L;
            } else if (Float.class.equals(type)) {
                return 0.0F;
            } else if (Double.class.equals(type)) {
                return 0.0D;
            }
        }
        return null;
    }

    /**
     * bytes -> 基础类型
     */
    public static <T> T convertBytes2PrimitiveObj(Class<T> type, byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        String strValue = new String(bytes);
        return convertStr2PrimitiveObj(type, strValue);
    }

    /**
     * string -> 基础类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertStr2PrimitiveObj(Class<T> type, String strValue) {
        if (StringUtils.isNotBlank(strValue)) {
            strValue = strValue.trim();

            if (String.class.equals(type)) {
                return (T) strValue;
            } else if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
                return (T) Boolean.valueOf(strValue);
            } else if (Byte.class.equals(type) || Byte.TYPE.equals(type)) {
                return (T) Byte.valueOf(strValue);
            } else if (Character.class.equals(type) || Character.TYPE.equals(type)) {
                return (T) strValue;
            } else if (Short.class.equals(type) || Short.TYPE.equals(type)) {
                return (T) Short.valueOf(strValue);
            } else if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
                return (T) Integer.valueOf(strValue);
            } else if (Long.class.equals(type) || Long.TYPE.equals(type)) {
                return (T) Long.valueOf(strValue);
            } else if (Float.class.equals(type) || Float.TYPE.equals(type)) {
                return (T) Float.valueOf(strValue);
            } else if (Double.class.equals(type) || Double.TYPE.equals(type)) {
                return (T) Double.valueOf(strValue);
            }
        }

        return null;
    }

    /**
     * @return 是否基础类型, 包含包装类
     */
    public static boolean isPrimitiveType(Class<?> type) {
        return String.class.equals(type) ||
                Boolean.class.equals(type) || Boolean.TYPE.equals(type) ||
                Byte.class.equals(type) || Byte.TYPE.equals(type) ||
                Character.class.equals(type) || Character.TYPE.equals(type) ||
                Short.class.equals(type) || Short.TYPE.equals(type) ||
                Integer.class.equals(type) || Integer.TYPE.equals(type) ||
                Long.class.equals(type) || Long.TYPE.equals(type) ||
                Float.class.equals(type) || Float.TYPE.equals(type) ||
                Double.class.equals(type) || Double.TYPE.equals(type);
    }

    /**
     * @return 是否是集合类型orMap类型
     */
    public static boolean isCollectionOrMapType(Class<?> type) {
        return Collection.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type) ||
                type.isArray();
    }

    /**
     * @return 该类实现的接口是否有指定注解标识
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean isInterfaceAnnotationPresent(Object o, Class<?> annotation) {
        Class<?> type = o.getClass();
        while (type != null) {
            for (Class interfaceClass : type.getInterfaces()) {
                if (interfaceClass.isAnnotationPresent(annotation)) {
                    return true;
                }
            }
            type = type.getSuperclass();
        }

        return false;
    }

    /**
     * string -> 基础类型
     */
    public static Object string2Obj(Field field, String value) {
        Class<?> fieldType = field.getType();

        if (StringUtils.isBlank(value)) {
            return null;
        }

        return convertStr2PrimitiveObj(fieldType, value.trim());
    }

    /**
     * 继承的父类
     * 获取类泛型具体实现类型
     * 由于类型擦除, 获取不了本类的泛型类型参数具体类型, 但其保存的父类的泛型类型参数具体类型, 所以可以获取父类的泛型类型参数具体类型
     */
    public static List<Type> getSuperClassGenericActualTypes(Class<?> type) {
        Type genericSuperclass = type.getGenericSuperclass();
        return new ArrayList<>(Arrays.asList(((ParameterizedType) genericSuperclass).getActualTypeArguments()));
    }

    /**
     * 继承的父类
     * 获取类泛型raw type(如果该类也是带泛型的, 则会丢掉泛型信息)
     */
    public static List<Class<?>> getSuperClassGenericRawTypes(Class<?> type) {
        return getSuperClassGenericActualTypes(type).stream().map(t -> {
            if (t instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) t).getRawType();
            } else {
                return ((Class<?>) t);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 实现的接口
     * 获取指定接口的泛型具体实现类型
     * 由于类型擦除, 获取不了本类的泛型类型参数具体类型, 但其保存的父接口的泛型类型参数具体类型, 所以可以获取父接口的泛型类型参数具体类型
     *
     * @param type          接口实现类
     * @param interfaceClass 指定接口
     */
    public static List<Type> getSuperInterfacesGenericActualTypes(Class<?> interfaceClass, Class<?> type) {
        if (!interfaceClass.isAssignableFrom(type)) {
            //非实现类
            return Collections.emptyList();
        }

        Type target = type;
        while (!Object.class.equals(target)) {
            //缓存泛型参数名字以及类型的对应关系
            Map<String, Type> paramName2Type = new HashMap<>();
            Type[] genericInterfaces;
            if (target instanceof Class) {
                genericInterfaces = ((Class<?>) target).getGenericInterfaces();
                target = ((Class<?>) target).getGenericSuperclass();
            } else if (target instanceof ParameterizedType) {
                // 只知道直接父类的泛型参数类型数据, 其父类或者实现接口的泛型类型是不知道的
                // 所以这里需要提前获取直接父类的泛型参数名字及其类型的对应关系
                ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) target;
                genericInterfaces = parameterizedType.getRawType().getGenericInterfaces();

                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                TypeVariable<? extends Class<?>>[] typeParameters = parameterizedType.getRawType().getTypeParameters();
                for (int i = 0; i < typeParameters.length; i++) {
                    TypeVariable<? extends Class<?>> typeVariable = typeParameters[i];
                    paramName2Type.put(typeVariable.getName(), actualTypeArguments[i]);
                }
                target = parameterizedType.getRawType().getGenericSuperclass();
            } else {
                throw new IllegalStateException("unknown exception");
            }

            List<Type> result = getActualTypesFromInterfaces(interfaceClass, genericInterfaces, paramName2Type);
            if (CollectionUtils.isNonEmpty(result)) {
                return result;
            }
        }

        return Collections.emptyList();
    }

    /**
     * 从直接父接口寻找目标接口的实际类型信息
     *
     * @param interfaceClass    目标接口
     * @param genericInterfaces 实现的接口泛化信息
     * @param paramName2Type    泛型参数名字以及类型的对应关系缓存
     * @return 实际类型信息, 如果null, 则表明没有寻找到目标接口, 否则, 即找到目标接口的实际类型信息
     */
    @Nullable
    private static List<Type> getActualTypesFromInterfaces(Class<?> interfaceClass, Type[] genericInterfaces, Map<String, Type> paramName2Type) {
        for (Type genericInterface : genericInterfaces) {
            if (!(genericInterface instanceof ParameterizedType)) {
                //过滤掉非泛化类型
                continue;
            }

            ParameterizedType parameterizedInterface = (ParameterizedType) genericInterface;
            if (!parameterizedInterface.getRawType().equals(interfaceClass)) {
                //类型不同, 尝试从父类寻找
                List<Type> result = tryGetActualTypesFromSuperInterfaces(interfaceClass, (ParameterizedTypeImpl) genericInterface, paramName2Type);
                if (result != null) {
                    return result;
                } else {
                    continue;
                }
            }

            //找到对应的interfaceClass
            List<Type> result = new ArrayList<>();
            for (Type actualTypeArgument : parameterizedInterface.getActualTypeArguments()) {
                if (actualTypeArgument instanceof TypeVariableImpl) {
                    //父类中, 根据泛型参数名字获取对应的类型
                    TypeVariableImpl<?> typeVariable = (TypeVariableImpl<?>) actualTypeArgument;
                    String paramName = typeVariable.getName();
                    if (paramName2Type.containsKey(paramName)) {
                        result.add(paramName2Type.get(paramName));
                    }
                } else {
                    result.add(actualTypeArgument);
                }
            }

            return result;
        }

        return null;
    }

    /**
     * 尝试从实现接口的父接口寻找目标接口的实际类型信息
     *
     * @param interfaceClass         目标接口
     * @param parameterizedInterface 泛化接口实现类
     * @param childParamName2Type    子类的泛型参数名字以及类型的对应关系缓存
     * @return 实际类型信息, 如果null, 则表明没有寻找到目标接口, 否则, 即找到目标接口的实际类型信息
     */
    @Nullable
    private static List<Type> tryGetActualTypesFromSuperInterfaces(Class<?> interfaceClass, ParameterizedTypeImpl parameterizedInterface, Map<String, Type> childParamName2Type) {
        Class<?> rawInterface = parameterizedInterface.getRawType();
        Type[] genericInterfaces = rawInterface.getGenericInterfaces();
        if (CollectionUtils.isEmpty(genericInterfaces)) {
            return null;
        }

        //缓存泛型参数名字以及类型的对应关系
        Map<String, Type> paramName2Type = new HashMap<>();
        Type[] actualTypeArguments = parameterizedInterface.getActualTypeArguments();
        TypeVariable<? extends Class<?>>[] typeParameters = rawInterface.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<? extends Class<?>> typeVariable = typeParameters[i];

            if (actualTypeArguments[i] instanceof TypeVariableImpl) {
                //只知道直接父类的泛型参数类型数据, 其父类或者实现接口的泛型类型是不知道的
                //所以这里从子类的泛型参数名字以及类型的对应关系缓存中获取
                paramName2Type.put(typeVariable.getName(), childParamName2Type.get(((TypeVariableImpl<?>) actualTypeArguments[i]).getName()));
            } else {
                //实际类型, 则直接关联
                paramName2Type.put(typeVariable.getName(), actualTypeArguments[i]);
            }
        }

        List<Type> result = getActualTypesFromInterfaces(interfaceClass, genericInterfaces, paramName2Type);
        if (CollectionUtils.isNonEmpty(result)) {
            return result;
        } else {
            return null;
        }
    }

    /**
     * 实现的接口
     * 获取指定接口的泛型raw type(如果该类也是带泛型的, 则会丢掉泛型信息)
     *
     * @param type          接口实现类
     * @param interfaceClass 指定接口
     */
    public static List<Class<?>> getSuperInterfacesGenericRawTypes(Class<?> interfaceClass, Class<?> type) {
        return getSuperInterfacesGenericActualTypes(interfaceClass, type).stream().map(t -> {
            if (t instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) t).getRawType();
            } else {
                return ((Class<?>) t);
            }
        }).collect(Collectors.toList());
    }

    /**
     * @return 是否是boolean
     */
    public static boolean isBoolean(Class<?> target) {
        return Boolean.class.equals(target) || Boolean.TYPE.equals(target);
    }

    /**
     * @param field 数组 | 集合类 成员变量
     * @return 元素类型
     */
    public static Class<?> getItemType(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            //数组
            return fieldType.getComponentType();
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            //集合
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }

        throw new UnsupportedOperationException();
    }

    /**
     * @param field map 成员变量
     * @return key value 类型
     */
    public static Tuple<Class<?>, Class<?>> getKVType(Field field) {
        if (!Map.class.isAssignableFrom(field.getType())) {
            throw new UnsupportedOperationException();
        }
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        return new Tuple<>((Class<?>) parameterizedType.getActualTypeArguments()[0], (Class<?>) parameterizedType.getActualTypeArguments()[1]);
    }

    /**
     * 判断是否可以rhsType是可以被assigned to lhsType, 考虑基础类型
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        Preconditions.checkNotNull(lhsType, "Left-hand side type must not be null");
        Preconditions.checkNotNull(rhsType, "Right-hand side type must not be null");
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = PRIMITIVE_WRAPPER_TYPE_MAP.get(rhsType);
            return (lhsType == resolvedPrimitive);
        } else {
            Class<?> resolvedWrapper = PRIMITIVE_TYPE_TO_WRAPPER_MAP.get(rhsType);
            return (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper));
        }
    }

    /**
     * 获取指定类型{@code clazz}的构造器
     * !!!注意, 该构造器创建的对象, 不会进行初始化
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getNotLoadConstructor(Class<T> clazz) {
        return (Constructor<T>) sun.reflect.ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, OBJECT_CONSTRUCTOR);
    }

    /**
     * @param className 类名
     * @return 判断一个类是否存在
     */
    public static boolean isClassPresent(String className) {
        try {
            getClass(className);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 父类为XXX
     * 实现类为{prefixName}XXXX
     * 获取{prefixName}, 全小写
     *
     * @param targetClass 实现类
     * @param baseClass   父类
     */
    public static <T> String getPrefixName(Class<? extends T> targetClass, Class<T> baseClass) {
        String baseName = baseClass.getSimpleName();
        String targetName = targetClass.getSimpleName();

        int index = targetName.indexOf(baseName);
        if (index > 0) {
            return targetName.substring(0, index).toLowerCase();
        }
        return null;
    }

    /**
     * 根据genericType获取泛型类型, 只支持泛型类型只有一个的情况
     * get inferred class for generic type, such as Flux like, please refer http://tutorials.jenkov.com/java-reflection/generics.html
     * <p>
     * 支持缓存
     *
     * @param genericType generic type
     * @return inferred class
     */
    public static Class<?> getInferredClassForGeneric(Type genericType) {
        //performance promotion by cache
        if (!GENERIC_TYPES_CACHE.containsKey(genericType)) {
            try {
                Class<?> inferredClass = parseInferredClass(genericType);
                if (inferredClass != null) {
                    GENERIC_TYPES_CACHE.put(genericType, inferredClass);
                } else {
                    GENERIC_TYPES_CACHE.put(genericType, Object.class);
                }
            } catch (Exception e) {
                return Object.class;
            }
        }
        return GENERIC_TYPES_CACHE.get(genericType);
    }

    /**
     * get inferred class for Generic Type, please refer http://tutorials.jenkov.com/java-reflection/generics.html
     *
     * @param genericType generic type
     * @return inferred class
     */
    public static Class<?> parseInferredClass(Type genericType) {
        Class<?> inferredClass = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) genericType;
            Type[] typeArguments = type.getActualTypeArguments();
            if (typeArguments.length > 0) {
                final Type typeArgument = typeArguments[0];
                if (typeArgument instanceof ParameterizedType) {
                    inferredClass = (Class<?>) ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                } else if (typeArgument instanceof Class) {
                    inferredClass = (Class<?>) typeArgument;
                } else {
                    String typeName = typeArgument.getTypeName();
                    if (typeName.contains(" ")) {
                        typeName = typeName.substring(typeName.lastIndexOf(" ") + 1);
                    }
                    if (typeName.contains("<")) {
                        typeName = typeName.substring(0, typeName.indexOf("<"));
                    }
                    try {
                        inferredClass = Class.forName(typeName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (inferredClass == null && genericType instanceof Class) {
            inferredClass = (Class<?>) genericType;
        }
        return inferredClass;
    }

    //------------------------------------------------------------字节码相关------------------------------------------------------------

    /**
     * 生成方法
     */
    public static String generateMethodContent(Method method, String methodBody) {
        StringBuilder sb = new StringBuilder();
        sb.append(generateMethodDeclaration(method));
        sb.append("{").append(System.lineSeparator());
        sb.append(methodBody).append(System.lineSeparator());
        sb.append("}");
        return sb.toString();
    }

    /**
     * 基础类型封箱
     */
    public static String primitivePackage(Class<?> type, String code) {
        StringBuilder sb = new StringBuilder();
        // 需要手动装箱, 不然编译会报错
        if (type.isPrimitive()) {
            if (Integer.TYPE.equals(type)) {
                sb.append("Integer.valueOf(");
            } else if (Short.TYPE.equals(type)) {
                sb.append("Short.valueOf(");
            } else if (Byte.TYPE.equals(type)) {
                sb.append("Byte.valueOf(");
            } else if (Long.TYPE.equals(type)) {
                sb.append("Long.valueOf(");
            } else if (Float.TYPE.equals(type)) {
                sb.append("Float.valueOf(");
            } else if (Double.TYPE.equals(type)) {
                sb.append("Double.valueOf(");
            } else if (Character.TYPE.equals(type)) {
                sb.append("Character.valueOf(");
            }
        }
        sb.append(code);
        if (type.isPrimitive() && !Void.TYPE.equals(type)) {
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * 基础类型拆箱
     */
    public static String primitiveUnpackage(Class<?> type, String code) {
        //需要手动拆箱, 不然编译会报错
        if (Integer.TYPE.equals(type)) {
            return "((" + Integer.class.getSimpleName() + ")" + code + ").intValue()";
        } else if (Short.TYPE.equals(type)) {
            return "((" + Short.class.getSimpleName() + ")" + code + ").shortValue()";
        } else if (Byte.TYPE.equals(type)) {
            return "((" + Byte.class.getSimpleName() + ")" + code + ").byteValue()";
        } else if (Long.TYPE.equals(type)) {
            return "((" + Long.class.getSimpleName() + ")" + code + ").longValue()";
        } else if (Float.TYPE.equals(type)) {
            return "((" + Float.class.getSimpleName() + ")" + code + ").floatValue()";
        } else if (Double.TYPE.equals(type)) {
            return "((" + Double.class.getSimpleName() + ")" + code + ").doubleValue()";
        } else if (Character.TYPE.equals(type)) {
            return "((" + Character.class.getSimpleName() + ")" + code + ").charValue()";
        } else if (!Void.TYPE.equals(type)) {
            return "(" + type.getName() + ")" + code;
        }
        return code;
    }

    /**
     * 获取{@link Method}唯一标识
     */
    public static String getUniqueName(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName());

        Class<?>[] paramTypes = method.getParameterTypes();
        if (CollectionUtils.isNonEmpty(paramTypes)) {
            sb.append("$");
        }
        StringJoiner paramsJoiner = new StringJoiner("$");
        for (Class<?> paramType : paramTypes) {
            paramsJoiner.add(paramType.getTypeName());
        }
        sb.append(paramsJoiner);
        return sb.toString();
    }

    /**
     * 生成方法签名(编译后的, 也就是类型擦除, 解语法糖后), 方法参数从 ${ClassUtils.METHOD_DECLARATION_ARG_NAME}0, ${ClassUtils.METHOD_DECLARATION_ARG_NAME}1, ${ClassUtils.METHOD_DECLARATION_ARG_NAME}2....
     */
    public static String generateMethodDeclaration(Method method) {
        if (method == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Modifier.toString(method.getModifiers())).append(" ");
        sb.append(method.getReturnType().getName()).append(" ");
        sb.append(method.getName()).append("(");

        StringJoiner argSJ = new StringJoiner(", ");
        Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            String paramTypeStr = paramTypes[i].getTypeName();
//            if (method.isVarArgs() && (i == paramTypes.length - 1)) {
//                param = param.replaceFirst("\\[\\]$", "...");
//            }
            //处理范性类型擦除
            if (paramTypes[i] instanceof Class) {
                argSJ.add(paramTypeStr + " " + METHOD_DECLARATION_ARG_NAME + i);
            } else if (paramTypes[i] instanceof ParameterizedType) {
                argSJ.add(((ParameterizedType) paramTypes[i]).getRawType().getTypeName() + " " + METHOD_DECLARATION_ARG_NAME + i);
            } else {
                argSJ.add(Object.class.getName() + " " + METHOD_DECLARATION_ARG_NAME + i);
            }
        }
        sb.append(argSJ.toString());
        sb.append(")");

        StringJoiner throwsSJ = new StringJoiner(", ");
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        for (Class<?> exceptionType : exceptionTypes) {
            throwsSJ.add(exceptionType.getName());
        }
        if (throwsSJ.length() > 0) {
            sb.append(" throws ");
            sb.append(throwsSJ.toString());
        }

        String methodDeclarationStr = sb.toString();
        methodDeclarationStr = methodDeclarationStr.replace("abstract ", "");
        methodDeclarationStr = methodDeclarationStr.replace("transient ", "");
        methodDeclarationStr = methodDeclarationStr.replace("native ", "");
        return methodDeclarationStr;
    }
}

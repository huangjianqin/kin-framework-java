package org.kin.framework.utils;

import java.io.Serializable;

/**
 * @author huangjianqin
 * @date 2021/3/8
 */
public class ClassUtilsTest {
    public static void main(String[] args) {
        //根据genericType获取泛型类型
        System.out.println("1----------------");
        System.out.println(ClassUtils.getInferredClassForGeneric(Printer.class.getMethods()[2].getGenericReturnType()));

        System.out.println("2----------------");
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(FirInterface.class, ThirInterfaceImpl.class));
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(SecInterface.class, ThirInterfaceImpl.class));
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(ThirInterface.class, ThirInterfaceImpl.class));

        System.out.println("3----------------");
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(FirInterface.class, SecInterfaceImpl.class));
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(SecInterface.class, SecInterfaceImpl.class));

        System.out.println("4----------------");
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(FirInterface.class, FirInterfaceImpl.class));

        System.out.println("4----------------");
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(Cloneable.class, ThirInterfaceImpl.class));
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(Cloneable.class, ThirInterfaceImpl.class));
        System.out.println(ClassUtils.getSuperInterfacesGenericActualTypes(Cloneable.class, ThirInterfaceImpl.class));
    }

    public interface Printable<C> {
        default void println(C c) {
            System.out.println(c);
        }

        Printable<C> create();
    }

    public static class Printer implements Printable<Integer> {
        @Override
        public void println(Integer integer) {
            System.out.println("int >>> " + integer);
        }

        @Override
        public Printable<Integer> create() {
            return new Printer();
        }
    }

    public interface FirInterface<A> {
    }

    public static class FirInterfaceImpl implements FirInterface<Byte>, Serializable {
        private static final long serialVersionUID = 8174506334399066609L;
    }

    public interface SecInterface<B, B1> extends FirInterface<B1> {
    }

    public static class SecInterfaceImpl implements Serializable, SecInterface<Byte, Short> {
        private static final long serialVersionUID = -7971756560661688140L;
    }

    public interface ThirInterface<C, C1, C2> extends SecInterface<C1, C2> {
    }

    public static class ThirInterfaceImpl implements Serializable, ThirInterface<Byte, Short, Integer> {
        private static final long serialVersionUID = 4059793512514173401L;
    }
}

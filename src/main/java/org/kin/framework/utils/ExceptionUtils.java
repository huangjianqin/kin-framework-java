package org.kin.framework.utils;

import org.kin.framework.utils.reflection.FieldUpdaters;
import org.kin.framework.utils.reflection.ReferenceFieldUpdater;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author huangjianqin
 * @date 2018/1/28
 */
public class ExceptionUtils {
    private static final ReferenceFieldUpdater<Throwable, Throwable> CAUSE_UPDATER =
            FieldUpdaters.newReferenceFieldUpdater(Throwable.class, "cause");

    /**
     * 获取异常描述
     */
    public static String getExceptionDesc(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Raises an exception bypassing compiler checks for checked exceptions.
     */
    public static void throwExt(Throwable t) {
        if (UnsafeUtil.hasUnsafe()) {
            UnsafeUtil.getUnsafeAccessor().throwException(t);
        } else {
            throwExt0(t);
        }
    }

    /**
     * 直接抛异常, 不需要外层提供异常捕获
     * <p>
     * 类型转换只是骗过前端javac编译器, 泛型只是个语法糖, 在javac编译后会解除语法糖将类型擦除,
     * 也就是说并不会生成checkcast指令, 所以在运行期不会抛出ClassCastException异常
     * private static <E extends java/lang/Throwable> void throwException0(java.lang.Throwable) throws E;
     * flags: ACC_PRIVATE, ACC_STATIC
     * Code:
     * stack=1, locals=1, args_size=1
     * 0: aload_0
     * 1: athrow // 注意在athrow之前并没有checkcast指令
     * ...
     * Exceptions:
     * throws java.lang.Throwable
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwExt0(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * 截断cause, 避免客户端无法找到cause类型而无法序列化
     */
    public static <T extends Throwable> T cutCause(T cause) {
        Throwable rootCause = cause;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        if (rootCause != cause) {
            cause.setStackTrace(rootCause.getStackTrace());
            CAUSE_UPDATER.set(cause, cause);
        }
        return cause;
    }
}

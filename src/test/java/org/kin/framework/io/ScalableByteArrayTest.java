package org.kin.framework.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2021/12/16
 */
public class ScalableByteArrayTest {
    public static void main(String[] args) {
        ScalableByteArray array = new ScalableByteArray(8);
        for (int i = 0; i < 256; i++) {
            array.writeByte(i);
        }

        System.out.println("-----------------------------------------------1");
        System.out.println(array);
        System.out.println(array.writableBytes());
        System.out.println(array.readableBytes());
        System.out.println(array.readerIndex());

        List<Byte> read = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            read.add(array.readByte());
        }

        System.out.println(read);
        System.out.println(array.writableBytes());
        System.out.println(array.readableBytes());
        System.out.println(array.readerIndex());

        read.clear();
        for (int i = 0; i < 192; i++) {
            read.add(array.readByte());
        }

        System.out.println(read);
        System.out.println(array.writableBytes());
        System.out.println(array.readableBytes());
        System.out.println(array.readerIndex());

        System.out.println("-----------------------------------------------2");
        array.clear();
        System.out.println(array);

        for (int i = 0; i < 128; i++) {
            array.writeByte(i);
        }

        array.writeByte(1);
        array.writeByte(2);
        array.writeByte(3);

        System.out.println(array);
        System.out.println(Arrays.toString(array.toByteArray()));

        System.out.println("-----------------------------------------------3");
        array.clear();
        byte[] bytes = new byte[]{1, 3, 5, 7, 9};
        array.writeBytes(bytes);
        array.writeBytes(bytes, 2, 2);
        array.writeByte(-100);
        System.out.println(array);

        System.out.println("-----------------------------------------------4");
        array.flip();
        System.out.println(array);

        System.out.println("-----------------------------------------------5");
        array.ensureWritableBytes(5);
        array.writerIndex(12);
        array.writeByte(-101);

        array.ensureWritableBytes(5);
        array.writerIndex(16);
        array.writeByte(-102);
        System.out.println(array);

        System.out.println("-----------------------------------------------6");
    }
}

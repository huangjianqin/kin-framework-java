package org.kin.framework.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/6/14
 */
public class AttachmentMapTest {
    public static void main(String[] args) {
        AttachmentMap attachmentMap = new AttachmentMap();
        attachmentMap.attach("A", "A");
        attachmentMap.attach("B", "A");
        attachmentMap.attach("C", "A");
        System.out.println(attachmentMap);

        Map<String, String> map = new HashMap<>();
        map.put("A", "D");
        map.put("B", "F");
        map.put("C", "G");
        attachmentMap.attachMany(map);

        System.out.println(attachmentMap.attachments());
        System.out.println(Objects.requireNonNull(attachmentMap.attachment("A")).toString());
        System.out.println(attachmentMap.attachment("D", "?"));

        System.out.println("-------------------------------------------------------------------------------------------");
        attachmentMap.attach("number", "1");
        System.out.println(attachmentMap.byteAttachment("number"));
        System.out.println(attachmentMap.shortAttachment("number"));
        System.out.println(attachmentMap.intAttachment("number"));
        System.out.println(attachmentMap.longAttachment("number"));
        System.out.println(attachmentMap.doubleAttachment("number"));

        attachmentMap.attach("number1", Integer.MAX_VALUE + "");
//        System.out.println(attachmentMap.byteAttachment("number1"));
//        System.out.println(attachmentMap.shortAttachment("number1"));
        System.out.println(attachmentMap.intAttachment("number1"));
        System.out.println(attachmentMap.longAttachment("number1"));
        System.out.println(attachmentMap.doubleAttachment("number1"));

        attachmentMap.attach("number2", (Double.MAX_VALUE / 2) + "");
//        System.out.println(attachmentMap.byteAttachment("number2"));
//        System.out.println(attachmentMap.shortAttachment("number2"));
//        System.out.println(attachmentMap.intAttachment("number2"));
//        System.out.println(attachmentMap.longAttachment("number2"));
        System.out.println(attachmentMap.doubleAttachment("number2"));

        attachmentMap.attach("bool1", "1");
        System.out.println(attachmentMap.boolAttachment("bool1"));

        attachmentMap.attach("bool2", "true");
        System.out.println(attachmentMap.boolAttachment("bool2"));
    }
}

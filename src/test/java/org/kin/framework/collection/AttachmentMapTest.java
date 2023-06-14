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
        System.out.println(attachmentMap.attachment("D","?"));
    }
}

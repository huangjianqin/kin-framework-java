package org.kin.framework.csp;

import java.util.HashMap;
import java.util.Map;

/**
 * tps rule group配置
 * @author huangjianqin
 * @date 2022/4/16
 */
public final class TpsRuleGroupOptions {
    /** tps rule group name */
    private String group;
    /** group级别tps rule配置 */
    private TpsRuleOptions groupRule;
    /** key -> child pattern -> {@link TpsRuleOptions} */
    private Map<String, TpsRuleOptions> childRuleMap = new HashMap<>();

    private TpsRuleGroupOptions() {
    }

    //getter
    public String getGroup() {
        return group;
    }

    public TpsRuleOptions getGroupRule() {
        return groupRule;
    }

    public Map<String, TpsRuleOptions> getChildRuleMap() {
        return childRuleMap;
    }

    //------------------------------------------builder
    public static Builder builder() {
        return new Builder();
    }

    /** builder **/
    public static class Builder {
        private final TpsRuleGroupOptions tpsRuleGroupOptions = new TpsRuleGroupOptions();

        public Builder group(String group) {
            tpsRuleGroupOptions.group = group;
            return this;
        }

        public Builder groupRule(TpsRuleOptions groupRule) {
            tpsRuleGroupOptions.groupRule = groupRule;
            return this;
        }

        public Builder childRuleMap(String child, TpsRuleOptions options) {
            tpsRuleGroupOptions.childRuleMap.put(child, options);
            return this;
        }

        public Builder childRuleMap(Map<String, TpsRuleOptions> childRuleMap) {
            tpsRuleGroupOptions.childRuleMap.putAll(childRuleMap);
            return this;
        }

        public TpsRuleGroupOptions build() {
            return tpsRuleGroupOptions;
        }
    }
}

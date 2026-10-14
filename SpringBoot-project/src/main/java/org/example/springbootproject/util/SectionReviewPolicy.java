package org.example.springbootproject.util;

import org.example.springbootproject.pojo.DocumentSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 按章节类型决定评分维度和汇总权重，避免预算/进度章也被打「创新性」。
 */
public final class SectionReviewPolicy {

    public enum Kind {
        BACKGROUND("项目背景", 0.12),
        INNOVATION("研究内容/创新点", 0.25),
        FEASIBILITY("实施方案/可行性", 0.22),
        PROGRESS("进度安排", 0.10),
        BUDGET("经费预算", 0.12),
        TEAM("团队情况", 0.08),
        OUTCOME("预期成果", 0.07),
        RISK("风险分析", 0.08),
        GENERAL("其他章节", 0.08);

        private final String label;
        private final double defaultWeight;

        Kind(String label, double defaultWeight) {
            this.label = label;
            this.defaultWeight = defaultWeight;
        }

        public String getLabel() {
            return label;
        }

        public double getDefaultWeight() {
            return defaultWeight;
        }
    }

    public static final class Dimension {
        private final String name;
        private final double weight;
        private final String rubric;

        public Dimension(String name, double weight, String rubric) {
            this.name = name;
            this.weight = weight;
            this.rubric = rubric;
        }

        public String getName() {
            return name;
        }

        public double getWeight() {
            return weight;
        }

        public String getRubric() {
            return rubric;
        }
    }

    private static final String RUBRIC_COMPLETE =
            "1分：关键要素缺失过半；3分：要素基本齐全但论述较浅；5分：要素齐全且有具体数据或事实支撑";
    private static final String RUBRIC_INNOVATION =
            "1分：仅复述已有方案；3分：有改进思路但缺少对比；5分：有明确的技术或模式创新并论证可行";
    private static final String RUBRIC_FEASIBLE =
            "1分：缺乏实施条件；3分：方案基本可行但未评估风险与资源；5分：路径清晰且风险有对应措施";
    private static final String RUBRIC_EVIDENCE =
            "1分：仅有结论无论据；3分：有论据但不够充分；5分：论据充分、逻辑清楚";

    public static final List<String> EXPECTED_SECTIONS = List.of(
            "项目背景/研究意义",
            "研究内容/创新点",
            "实施方案/技术路线",
            "进度安排",
            "经费预算",
            "团队情况",
            "预期成果"
    );

    private SectionReviewPolicy() {
    }

    public static Kind classify(String title) {
        String t = title == null ? "" : title;
        if (containsAny(t, "创新", "研究内容", "技术方案", "核心技术", "特色")) {
            return Kind.INNOVATION;
        }
        if (containsAny(t, "实施", "可行性", "技术路线", "实现", "落地", "方法")) {
            return Kind.FEASIBILITY;
        }
        if (containsAny(t, "进度", "计划安排", "时间表", "里程碑", "甘特")) {
            return Kind.PROGRESS;
        }
        if (containsAny(t, "经费", "预算", "资金", "费用", "成本")) {
            return Kind.BUDGET;
        }
        if (containsAny(t, "团队", "成员", "人员", "分工")) {
            return Kind.TEAM;
        }
        if (containsAny(t, "成果", "预期", "指标", "产出")) {
            return Kind.OUTCOME;
        }
        if (containsAny(t, "风险", "问题与对策", "困难")) {
            return Kind.RISK;
        }
        if (containsAny(t, "背景", "意义", "现状", "调研", "需求分析", "引言", "绪论")) {
            return Kind.BACKGROUND;
        }
        return Kind.GENERAL;
    }

    public static List<Dimension> dimensionsFor(Kind kind) {
        return switch (kind) {
            case INNOVATION -> List.of(
                    new Dimension("完整性", 0.30, RUBRIC_COMPLETE),
                    new Dimension("创新性", 0.45, RUBRIC_INNOVATION),
                    new Dimension("论证充分性", 0.25, RUBRIC_EVIDENCE)
            );
            case FEASIBILITY, RISK -> List.of(
                    new Dimension("完整性", 0.30, RUBRIC_COMPLETE),
                    new Dimension("可行性", 0.45, RUBRIC_FEASIBLE),
                    new Dimension("论证充分性", 0.25, RUBRIC_EVIDENCE)
            );
            case PROGRESS, BUDGET, TEAM -> List.of(
                    new Dimension("完整性", 0.50, RUBRIC_COMPLETE),
                    new Dimension("可行性", 0.50, RUBRIC_FEASIBLE)
            );
            case OUTCOME, BACKGROUND, GENERAL -> List.of(
                    new Dimension("完整性", 0.50, RUBRIC_COMPLETE),
                    new Dimension("论证充分性", 0.50, RUBRIC_EVIDENCE)
            );
        };
    }

    public static void applyKindAndWeight(DocumentSection section) {
        Kind kind = classify(section.getSectionTitle());
        section.setSectionKind(kind.name());
        section.setWeight(kind.getDefaultWeight());
    }

    public static List<String> findMissingSections(List<DocumentSection> sections) {
        Map<Kind, Boolean> found = new LinkedHashMap<>();
        found.put(Kind.BACKGROUND, false);
        found.put(Kind.INNOVATION, false);
        found.put(Kind.FEASIBILITY, false);
        found.put(Kind.PROGRESS, false);
        found.put(Kind.BUDGET, false);
        found.put(Kind.TEAM, false);
        found.put(Kind.OUTCOME, false);

        if (sections != null) {
            for (DocumentSection section : sections) {
                if (section.getCharCount() != null && section.getCharCount() < 50) {
                    continue;
                }
                Kind kind = section.getSectionKind() != null
                        ? Kind.valueOf(section.getSectionKind())
                        : classify(section.getSectionTitle());
                if (found.containsKey(kind)) {
                    found.put(kind, true);
                }
            }
        }

        List<String> missing = new ArrayList<>();
        for (Map.Entry<Kind, Boolean> entry : found.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                missing.add(entry.getKey().getLabel());
            }
        }
        return missing;
    }

    private static boolean containsAny(String title, String... keywords) {
        for (String keyword : keywords) {
            if (title.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

package org.example.springbootproject.constant;

/**
 * AI 评审常量定义
 */
public class AiReviewConstants {
    
    /** 任务状态 */
    public static final String TASK_STATUS_PENDING = "PENDING";
    public static final String TASK_STATUS_PARSING = "PARSING";
    public static final String TASK_STATUS_CLEANING = "CLEANING";
    public static final String TASK_STATUS_SPLITTING = "SPLITTING";
    public static final String TASK_STATUS_REVIEWING = "REVIEWING";
    public static final String TASK_STATUS_SUMMARIZING = "SUMMARIZING";
    public static final String TASK_STATUS_DONE = "DONE";
    public static final String TASK_STATUS_FAILED = "FAILED";
    
    /** 评审模式 */
    public static final String REVIEW_MODE_MANUAL = "MANUAL";
    public static final String REVIEW_MODE_AI = "AI";
    
    /** 最小章节字符数 */
    public static final int MIN_SECTION_CHARS = 50;
    
    /** 等级阈值 */
    public static final double GRADE_A_THRESHOLD = 85.0;
    public static final double GRADE_B_THRESHOLD = 70.0;
    public static final double GRADE_C_THRESHOLD = 60.0;
    
    /** 等级 */
    public static final String GRADE_A = "A";
    public static final String GRADE_B = "B";
    public static final String GRADE_C = "C";
    public static final String GRADE_D = "D";
    
    private AiReviewConstants() {
        // 私有构造函数，防止实例化
    }
}

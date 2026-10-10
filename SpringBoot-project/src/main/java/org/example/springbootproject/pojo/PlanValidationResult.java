package org.example.springbootproject.pojo;

import lombok.Data;

/**
 * 计划书验证结果
 */
@Data
public class PlanValidationResult {
    private boolean valid;
    private String errorMessage;
    private String fileHash;
    
    public PlanValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }
    
    public PlanValidationResult(boolean valid, String errorMessage, String fileHash) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.fileHash = fileHash;
    }
}

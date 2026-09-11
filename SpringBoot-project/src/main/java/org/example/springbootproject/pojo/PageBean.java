package org.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 * 用于封装分页查询的结果，包含总记录数和当前页数据列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBean<T> {
    private Long total; // 总记录数
    private List<T> list; // 当前页数据列表

    /**
     * 创建分页结果对象
     */
    public static <T> PageBean<T> of(Long total, List<T> list) {
        return new PageBean<>(total, list);
    }
}


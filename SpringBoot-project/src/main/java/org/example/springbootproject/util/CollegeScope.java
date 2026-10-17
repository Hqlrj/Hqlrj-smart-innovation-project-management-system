package org.example.springbootproject.util;

import org.example.springbootproject.pojo.User;

/**
 * 学院数据范围：系统管理员看全部，其他角色只看本院。
 */
public final class CollegeScope {

    private CollegeScope() {
    }

    public static boolean isGlobalViewer(String role) {
        return "系统管理员".equals(role);
    }

    /**
     * @return null 表示不限制学院；空字符串表示无学院，结果应为空
     */
    public static String collegeFilter(User user, String role) {
        if (isGlobalViewer(role)) {
            return null;
        }
        if (user == null || user.getCollege() == null || user.getCollege().isBlank()) {
            return "";
        }
        return user.getCollege().trim();
    }

    public static boolean canViewCollege(String role, String viewerCollege, String targetCollege) {
        if (isGlobalViewer(role)) {
            return true;
        }
        if (viewerCollege == null || viewerCollege.isBlank() || targetCollege == null || targetCollege.isBlank()) {
            return false;
        }
        return viewerCollege.equals(targetCollege.trim());
    }

    public static boolean canViewNotice(String role, String viewerCollege, String publisherRole, String publisherCollege) {
        if (isGlobalViewer(role)) {
            return true;
        }
        if ("系统管理员".equals(publisherRole)) {
            return true;
        }
        return canViewCollege(role, viewerCollege, publisherCollege);
    }
}

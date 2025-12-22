package com.yes.kfcaigc.tool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 改动点追踪工具
 * 供AI调用，记录文案修改的改动点
 */
public class ModificationTracker {
    
    /**
     * 存储改动点的Map，key为请求ID，value为改动点列表
     */
    private static final Map<String, List<String>> modificationPoints = new ConcurrentHashMap<>();
    
    /**
     * 记录改动点
     * @param requestId 请求ID
     * @param changes 改动点字符串，JSON数组格式，例如：
     *                "[{\"origin\":\"...\",\"now\":\"...\"}, ...]"
     */
    public static void recordChanges(String requestId, String changes) {
        if (requestId == null || requestId.isEmpty()) {
            return;
        }
        
        if (changes == null || changes.isEmpty()) {
            return;
        }
        
        // 直接存储JSON字符串，不进行分割
        modificationPoints.compute(requestId, (key, existingList) -> {
            if (existingList == null) {
                List<String> newList = new ArrayList<>();
                newList.add(changes);
                return newList;
            } else {
                // 追加JSON字符串
                if (!existingList.contains(changes)) {
                    existingList.add(changes);
                }
                return existingList;
            }
        });
    }
    
    /**
     * 获取改动点列表
     * @param requestId 请求ID
     * @return 改动点列表，如果不存在则返回空列表
     */
    public static List<String> getChanges(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> list = modificationPoints.get(requestId);
        if (list == null) {
            return new ArrayList<>();
        }
        // 再次去重，保证返回结果没有重复
        return new ArrayList<>(new LinkedHashSet<>(list));
    }
    
    /**
     * 移除指定ID的改动点记录（清理内存）
     * @param requestId 请求ID
     */
    public static void removeChanges(String requestId) {
        if (requestId != null) {
            modificationPoints.remove(requestId);
        }
    }
    
    /**
     * 创建新的请求ID并初始化
     * @return 新的请求ID
     */
    public static String createNewRequestId() {
        String requestId = UUID.randomUUID().toString();
        modificationPoints.put(requestId, new ArrayList<>());
        return requestId;
    }
    
    /**
     * 清空所有改动点记录（用于测试或清理）
     */
    public static void clearAll() {
        modificationPoints.clear();
    }
}


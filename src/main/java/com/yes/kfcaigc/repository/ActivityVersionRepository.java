package com.yes.kfcaigc.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yes.kfcaigc.entity.ActivityVersion;
import com.yes.kfcaigc.mapper.ActivityVersionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 活动版本Repository
 */
@Repository
public class ActivityVersionRepository {

    private final ActivityVersionMapper activityVersionMapper;

    public ActivityVersionRepository(ActivityVersionMapper activityVersionMapper) {
        this.activityVersionMapper = activityVersionMapper;
    }

    /**
     * 获取活动当前最大版本号
     */
    public Integer getCurrentVersion(String activityCode) {
        LambdaQueryWrapper<ActivityVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVersion::getActivityCode, activityCode)
               .orderByDesc(ActivityVersion::getVersion)
               .last("LIMIT 1");
        
        ActivityVersion latest = activityVersionMapper.selectOne(wrapper);
        return latest != null ? latest.getVersion() : 0;
    }

    /**
     * 保存新版本
     */
    public void save(ActivityVersion activityVersion) {
        activityVersionMapper.insert(activityVersion);
    }
    
    /**
     * 获取活动的所有版本（按版本号降序）
     */
    public List<ActivityVersion> getAllVersions(String activityCode) {
        LambdaQueryWrapper<ActivityVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVersion::getActivityCode, activityCode)
               .orderByDesc(ActivityVersion::getVersion);
        return activityVersionMapper.selectList(wrapper);
    }
    
    /**
     * 根据活动编码和版本号查询
     */
    public ActivityVersion getByVersion(String activityCode, Integer version) {
        LambdaQueryWrapper<ActivityVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVersion::getActivityCode, activityCode)
               .eq(ActivityVersion::getVersion, version);
        return activityVersionMapper.selectOne(wrapper);
    }
    
    /**
     * 删除指定版本之后的所有版本
     */
    public void deleteAfterVersion(String activityCode, Integer version) {
        LambdaQueryWrapper<ActivityVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVersion::getActivityCode, activityCode)
               .gt(ActivityVersion::getVersion, version);
        activityVersionMapper.delete(wrapper);
    }
}

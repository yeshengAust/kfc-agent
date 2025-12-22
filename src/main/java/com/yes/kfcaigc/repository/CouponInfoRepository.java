package com.yes.kfcaigc.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yes.kfcaigc.entity.CouponInfo;
import com.yes.kfcaigc.mapper.CouponInfoMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CouponInfoRepository {

    private final CouponInfoMapper couponInfoMapper;

    public CouponInfoRepository(CouponInfoMapper couponInfoMapper) {
        this.couponInfoMapper = couponInfoMapper;
    }

    /**
     * 根据活动编码查询当前生效的券文案描述
     */
    public String findCouponDescByActivityCode(String activityCode) {
        LambdaQueryWrapper<CouponInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponInfo::getActivityCode, activityCode)
                    .eq(CouponInfo::getDeleted, 0)
                    .last("LIMIT 1");
        
        CouponInfo couponInfo = couponInfoMapper.selectOne(queryWrapper);
        return couponInfo != null ? couponInfo.getDescription() : null;
    }

    /**
     * 根据活动编码查询券信息
     */
    public CouponInfo findByActivityCode(String activityCode) {
        LambdaQueryWrapper<CouponInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponInfo::getActivityCode, activityCode)
                    .eq(CouponInfo::getDeleted, 0)
                    .last("LIMIT 1");
        
        return couponInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 保存券信息
     */
    public void save(CouponInfo couponInfo) {
        couponInfoMapper.insert(couponInfo);
    }

    /**
     * 更新券信息
     */
    public void update(CouponInfo couponInfo) {
        couponInfoMapper.updateById(couponInfo);
    }
}





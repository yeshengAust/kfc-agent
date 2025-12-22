package com.yes.kfcaigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yes.kfcaigc.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务表 Mapper 接口
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

}

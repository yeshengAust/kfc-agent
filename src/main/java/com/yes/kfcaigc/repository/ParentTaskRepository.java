package com.yes.kfcaigc.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yes.kfcaigc.entity.ParentTask;
import com.yes.kfcaigc.mapper.ParentTaskMapper;
import org.springframework.stereotype.Repository;

/**
 * 大任务表 Repository
 */
@Repository
public class ParentTaskRepository extends ServiceImpl<ParentTaskMapper, ParentTask> {

}

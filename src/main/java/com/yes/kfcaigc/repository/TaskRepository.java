package com.yes.kfcaigc.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yes.kfcaigc.entity.Task;
import com.yes.kfcaigc.mapper.TaskMapper;
import org.springframework.stereotype.Repository;

/**
 * 任务表 Repository
 */
@Repository
public class TaskRepository extends ServiceImpl<TaskMapper, Task> {

}

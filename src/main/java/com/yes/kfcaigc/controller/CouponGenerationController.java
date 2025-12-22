package com.yes.kfcaigc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes.kfcaigc.entity.ActivityVersion;
import com.yes.kfcaigc.entity.CouponInfo;
import com.yes.kfcaigc.entity.ParentTask;
import com.yes.kfcaigc.entity.Task;
import com.yes.kfcaigc.model.CouponGenerationRequest;
import com.yes.kfcaigc.model.CouponGenerationResponse;
import com.yes.kfcaigc.model.ModificationPointDTO;
import com.yes.kfcaigc.repository.ActivityVersionRepository;
import com.yes.kfcaigc.repository.CouponInfoRepository;
import com.yes.kfcaigc.repository.ParentTaskRepository;
import com.yes.kfcaigc.repository.TaskRepository;
import com.yes.kfcaigc.service.CouponModificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/coupon")
public class CouponGenerationController {

    @Autowired
    private CouponModificationService couponModificationService;

    @Autowired
    private CouponInfoRepository couponInfoRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ParentTaskRepository parentTaskRepository;
    
    @Autowired
    private ActivityVersionRepository activityVersionRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @PostMapping("/modify")
    public ResponseEntity<CouponGenerationResponse> modifyCoupon(@RequestBody CouponGenerationRequest request) {
        CouponGenerationResponse response = new CouponGenerationResponse();


        try {
            // 参数校验
            if (request.getActivityCode() == null || request.getActivityCode().isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("活动编码(activityCode)不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getOperation() == null || request.getOperation().isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("操作指令不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 根据 activityCode 从数据库查询原始券文案
            String originalText = couponInfoRepository.findCouponDescByActivityCode(request.getActivityCode());
            if (originalText == null || originalText.isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("未找到对应活动编码的券文案");
                return ResponseEntity.badRequest().body(response);
            }

            // 修改文案
            CouponModificationService.ModificationResult result = couponModificationService.modifyCouponText(
                    originalText,
                    request.getOperation()
            );

            response.setSuccess(true);
            response.setCouponText(result.getModifiedText());
            
            // 将ModificationPoint转换为ModificationPointDTO
            List<ModificationPointDTO> modificationPointDTOs = result.getModificationPoints().stream()
                    .map(point -> new ModificationPointDTO(point.getOrigin(), point.getNow()))
                    .collect(Collectors.toList());
            response.setModificationPoints(modificationPointDTOs);
            
            // 设置修改类型
            response.setModificationType(result.getModificationType());
            
            response.setScene(request.getOperation());
            response.setSubScene(null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("修改券文案失败", e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running");
    }
    
    /**
     * 根据activityCode获取原始券文案
     */
    @GetMapping("/original/{activityCode}")
    public ResponseEntity<CouponInfo> getOriginalText(@PathVariable String activityCode) {
        try {
            CouponInfo couponInfo = couponInfoRepository.findByActivityCode(activityCode);
            if (couponInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(couponInfo);
        } catch (Exception e) {
            log.error("查询原文失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据活动编码进行修改（支持多个活动编码，逗号分隔）
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateCoupon(@RequestBody CouponGenerationRequest request) {
        try {
            String activityCodesStr = request.getActivityCode();
            String operation = request.getOperation();
            String taskName = request.getTaskName();
            
            // 参数校验
            if (activityCodesStr == null || activityCodesStr.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "活动编码不能为空"));
            }

            if (operation == null || operation.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "操作指令不能为空"));
            }

            // 分割活动编码
            String[] activityCodesArray = activityCodesStr.split(",");
            List<String> activityCodes = Arrays.stream(activityCodesArray)
                    .map(String::trim)
                    .filter(code -> !code.isEmpty())
                    .collect(Collectors.toList());
            
            if (activityCodes.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "请输入有效的活动编码"));
            }

            log.info("接收到批量修改请求，活动编码数量：{}，操作：{}", activityCodes.size(), operation);

            // 创建大任务（父任务）
            ParentTask parentTask = new ParentTask();
            // 任务名称：优先使用前端传入的taskName，未填写时默认使用操作指令
            if (taskName != null && !taskName.trim().isEmpty()) {
                parentTask.setName(taskName.trim());
            } else {
                parentTask.setName(operation);
            }
            parentTask.setOperation(operation);
            parentTask.setActivityCodes(activityCodesStr);
            parentTask.setTotalCount(activityCodes.size());
            parentTask.setSuccessCount(0);
            parentTask.setFailCount(0);
            parentTask.setProcessingCount(activityCodes.size());
            parentTaskRepository.save(parentTask);
            
            Long parentTaskId = parentTask.getId();
            log.info("创建大任务，parentTaskId: {}", parentTaskId);

            // 使用CompletableFuture并行创建子任务
            List<CompletableFuture<Map<String, Object>>> futures = activityCodes.stream()
                    .map(activityCode -> CompletableFuture.supplyAsync(() -> 
                        createSingleTask(activityCode, operation, parentTaskId), taskExecutor))
                    .collect(Collectors.toList());

            // 等待所有任务创建完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            
            // 获取所有结果
            allFutures.join();
            
            List<Map<String, Object>> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            // 统计成功失败数量
            long successCount = results.stream().filter(r -> (Boolean) r.get("success")).count();
            long failCount = results.size() - successCount;
            
            // 提取成功的taskId
            List<Long> taskIds = results.stream()
                    .filter(r -> (Boolean) r.get("success"))
                    .map(r -> ((Number) r.get("taskId")).longValue())
                    .collect(Collectors.toList());

            log.info("批量任务创建完成，成功：{}，失败：{}", successCount, failCount);

            // 返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("parentTaskId", parentTaskId);
            response.put("totalCount", activityCodes.size());
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            response.put("taskIds", taskIds);
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量创建任务失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "创建任务失败：" + e.getMessage()));
        }
    }
    
    /**
     * 创建单个任务
     */
    private Map<String, Object> createSingleTask(String activityCode, String operation, Long parentTaskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始创建任务，activityCode: {}", activityCode);
            
            // 根据活动编码查询原文
            String originalText = couponInfoRepository.findCouponDescByActivityCode(activityCode);
            if (originalText == null || originalText.isEmpty()) {
                log.error("未找到对应活动编码的券文案：{}", activityCode);
                result.put("success", false);
                result.put("activityCode", activityCode);
                result.put("message", "未找到对应活动编码的券文案");
                return result;
            }

            // 创建任务记录（状态为0-处理中）
            Task task = new Task();
            task.setParentTaskId(parentTaskId);
            task.setStatus(0);
            task.setOriginalText(originalText); // 保存原始文案
            task.setCouponText("");
            task.setScene(operation);
            task.setSubScene(activityCode);
            task.setModificationPoints(null);
            taskRepository.save(task);
            
            Long taskId = task.getId();
            log.info("任务创建成功，taskId: {}, activityCode: {}, parentTaskId: {}", taskId, activityCode, parentTaskId);

            // 异步执行AI处理（使用CompletableFuture）
            CompletableFuture.runAsync(() -> 
                processTask(taskId, originalText, operation, parentTaskId), taskExecutor);

            result.put("success", true);
            result.put("taskId", taskId);
            result.put("activityCode", activityCode);
            result.put("message", "任务创建成功");
            
        } catch (Exception e) {
            log.error("创建任务失败，activityCode: {}", activityCode, e);
            result.put("success", false);
            result.put("activityCode", activityCode);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    /**
     * 异步处理任务（使用线程池）
     */
    private void processTask(Long taskId, String originalText, String operation, Long parentTaskId) {
        try {
            log.info("开始异步处理任务，taskId: {}", taskId);
            
            // 调用AI修改服务
            CouponModificationService.ModificationResult result = 
                couponModificationService.modifyCouponText(originalText, operation);

            // 将ModificationPoint转换为JSON字符串
            List<ModificationPointDTO> modificationPointDTOs = result.getModificationPoints().stream()
                    .map(point -> new ModificationPointDTO(point.getOrigin(), point.getNow()))
                    .collect(Collectors.toList());
            String modificationPointsJson = objectMapper.writeValueAsString(modificationPointDTOs);

            // 更新任务状态为1-处理完成
            Task task = taskRepository.getById(taskId);
            task.setStatus(1);
            task.setCouponText(result.getModifiedText());
            task.setModificationPoints(modificationPointsJson);
            task.setModificationType(result.getModificationType()); // 保存修改类型
            taskRepository.updateById(task);
            
            // 更新父任务统计数据
            updateParentTaskCount(parentTaskId);
            
            log.info("任务处理完成，taskId: {}", taskId);
            
        } catch (Exception e) {
            log.error("任务处理失败，taskId: {}", taskId, e);
            
            // 更新任务状态为2-处理失败
            Task task = taskRepository.getById(taskId);
            task.setStatus(2);
            task.setErrorMessage(e.getMessage());
            taskRepository.updateById(task);
            
            // 更新父任务统计数据
            updateParentTaskCount(parentTaskId);
        }
    }
    
    /**
     * 更新父任务统计数据
     */
    private void updateParentTaskCount(Long parentTaskId) {
        if (parentTaskId == null) {
            return;
        }
        
        try {
            // 查询所有子任务
            List<Task> childTasks = taskRepository.lambdaQuery()
                    .eq(Task::getParentTaskId, parentTaskId)
                    .list();
            
            // 统计数量：0-处理中, 1-已完成, 2-已更新, <0-失败
            long successCount = childTasks.stream().filter(t -> t.getStatus() == 1 || t.getStatus() == 2).count();
            long failCount = childTasks.stream().filter(t -> t.getStatus() < 0).count();
            long processingCount = childTasks.stream().filter(t -> t.getStatus() == 0).count();
            
            // 更新父任务
            ParentTask parentTask = parentTaskRepository.getById(parentTaskId);
            parentTask.setSuccessCount((int) successCount);
            parentTask.setFailCount((int) failCount);
            parentTask.setProcessingCount((int) processingCount);
            parentTaskRepository.updateById(parentTask);
            
            log.info("更新父任务统计，parentTaskId: {}, 成功: {}, 失败: {}, 处理中: {}", 
                    parentTaskId, successCount, failCount, processingCount);
            
        } catch (Exception e) {
            log.error("更新父任务统计失败，parentTaskId: {}", parentTaskId, e);
        }
    }
    
    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable Long taskId) {
        try {
            Task task = taskRepository.getById(taskId);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", task.getStatus());
            result.put("originalText", task.getOriginalText());
            result.put("couponText", task.getCouponText());
            result.put("scene", task.getScene());
            result.put("subScene", task.getSubScene());
            result.put("modificationType", task.getModificationType()); // 添加修改类型
            result.put("errorMessage", task.getErrorMessage());
            
            // 解析modificationPoints JSON字符串
            if (task.getModificationPoints() != null && !task.getModificationPoints().isEmpty()) {
                List<ModificationPointDTO> modificationPoints = 
                    objectMapper.readValue(task.getModificationPoints(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ModificationPointDTO.class));
                result.put("modificationPoints", modificationPoints);
            } else {
                result.put("modificationPoints", null);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询任务状态失败，taskId: {}", taskId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 查询所有任务列表
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getAllTasks() {
        try {
            // 查询所有大任务
            List<ParentTask> parentTasks = parentTaskRepository.list();
            
            // 按创建时间倒序排列
            parentTasks.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            
            List<Map<String, Object>> result = parentTasks.stream().map(parentTask -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", parentTask.getId());
                map.put("name", parentTask.getName());
                map.put("operation", parentTask.getOperation());
                map.put("activityCodes", parentTask.getActivityCodes());
                map.put("totalCount", parentTask.getTotalCount());
                map.put("successCount", parentTask.getSuccessCount());
                map.put("failCount", parentTask.getFailCount());
                map.put("processingCount", parentTask.getProcessingCount());
                map.put("createTime", parentTask.getCreateTime().toString());
                map.put("updateTime", parentTask.getUpdateTime().toString());
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询任务列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 查询大任务的子任务列表
     */
    @GetMapping("/parent-task/{parentTaskId}/tasks")
    public ResponseEntity<List<Map<String, Object>>> getChildTasks(@PathVariable Long parentTaskId) {
        try {
            // 查询所有子任务
            List<Task> childTasks = taskRepository.lambdaQuery()
                    .eq(Task::getParentTaskId, parentTaskId)
                    .list();
            
            // 按创建时间排序
            childTasks.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
            
            List<Map<String, Object>> result = childTasks.stream().map(task -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", task.getId());
                map.put("status", task.getStatus());
                map.put("scene", task.getScene());
                map.put("subScene", task.getSubScene());
                map.put("createTime", task.getCreateTime().toString());
                map.put("updateTime", task.getUpdateTime().toString());
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询子任务列表失败，parentTaskId: {}", parentTaskId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 确认更新券信息：将当前任务的修改结果更新到 coupon_info，并在 activity_version 中记录新版本
     */
    @PostMapping("/updateCouponInfo/{taskId}")
    public ResponseEntity<Map<String, Object>> updateCouponInfo(@PathVariable Long taskId) {
        try {
            // 查询任务
            Task task = taskRepository.getById(taskId);
            if (task == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "任务不存在"));
            }
            
            if (task.getStatus() != 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "任务未完成，无法更新券信息"));
            }
            
            // 检查任务是否已经更新过（状态=2）
            if (task.getStatus() == 2) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "该任务已经更新过券信息，不可重复更新"));
            }
            
            String activityCode = task.getSubScene(); // subScene 是 activityCode
            if (activityCode == null || activityCode.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "活动编码为空"));
            }
            
            String newDescription = task.getCouponText(); // 修改后的文案
            
            // 查询当前 coupon_info 记录
            CouponInfo couponInfo = couponInfoRepository.findByActivityCode(activityCode);
            if (couponInfo == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "券信息不存在"));
            }
            
            // 获取当前版本号，+1
            Integer currentVersion = couponInfo.getVersion();
            if (currentVersion == null) {
                currentVersion = 0;
            }
            Integer newVersion = currentVersion + 1;
            
            // 更新 coupon_info.description 和 version
            couponInfo.setDescription(newDescription);
            couponInfo.setVersion(newVersion);
            couponInfoRepository.update(couponInfo);
            
            // 在 activity_version 中创建新版本记录
            ActivityVersion activityVersion = new ActivityVersion();
            activityVersion.setActivityCode(activityCode);
            activityVersion.setVersion(newVersion);
            activityVersion.setDescription(newDescription); // 保存此版本的券文案
            activityVersionRepository.save(activityVersion);
            
            // 更新任务状态为 2（已更新）
            task.setStatus(2);
            taskRepository.updateById(task);
            
            log.info("更新券信息成功，activityCode: {}, 新版本: {}", activityCode, newVersion);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "更新成功",
                "activityCode", activityCode,
                "newVersion", newVersion
            ));
            
        } catch (Exception e) {
            log.error("更新券信息失败，taskId: {}", taskId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取活动的所有版本列表
     */
    @GetMapping("/versions/{activityCode}")
    public ResponseEntity<List<Map<String, Object>>> getVersions(@PathVariable String activityCode) {
        try {
            List<ActivityVersion> versions = activityVersionRepository.getAllVersions(activityCode);
            List<Map<String, Object>> result = versions.stream()
                .map(v -> Map.of(
                    "version", (Object) v.getVersion(),
                    "description", v.getDescription(),
                    "createTime", v.getCreateTime().toString()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取版本列表失败，activityCode: {}", activityCode, e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
    
    /**
     * 版本回退：回退到指定版本，删除该版本之后的所有版本
     */
    @PostMapping("/rollback/{activityCode}/{version}")
    public ResponseEntity<Map<String, Object>> rollbackVersion(
            @PathVariable String activityCode,
            @PathVariable Integer version) {
        try {
            // 查询指定版本是否存在
            ActivityVersion targetVersion = activityVersionRepository.getByVersion(activityCode, version);
            if (targetVersion == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "指定的版本不存在"));
            }
            
            // 删除该版本之后的所有版本
            activityVersionRepository.deleteAfterVersion(activityCode, version);
            
            // 更新 coupon_info 为目标版本的内容
            CouponInfo couponInfo = couponInfoRepository.findByActivityCode(activityCode);
            if (couponInfo != null) {
                couponInfo.setDescription(targetVersion.getDescription());
                couponInfo.setVersion(targetVersion.getVersion());
                couponInfoRepository.update(couponInfo);
            }
            
            // 回退后，将该活动编码的所有状态为2的任务改回1（允许继续修改）
            LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(Task::getSubScene, activityCode)
                      .eq(Task::getStatus, 2);
            List<Task> tasksToUpdate = taskRepository.list(taskWrapper);
            
            if (!tasksToUpdate.isEmpty()) {
                for (Task task : tasksToUpdate) {
                    task.setStatus(1);
                    taskRepository.updateById(task);
                }
                log.info("回退版本后，已将 {} 个任务状态从2改回1", tasksToUpdate.size());
            }
            
            log.info("版本回退成功，activityCode: {}, 回退到版本: {}", activityCode, version);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "回退成功",
                "version", version
            ));
            
        } catch (Exception e) {
            log.error("版本回退失败，activityCode: {}, version: {}", activityCode, version, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "回退失败: " + e.getMessage()));
        }
    }
}


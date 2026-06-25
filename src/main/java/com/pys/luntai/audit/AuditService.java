package com.pys.luntai.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pys.luntai.user.User;
import com.pys.luntai.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuditService {

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 提交审核申请（创建审批记录）
     */
    public void submitAudit(Long userId, String companyName, String companyAddress,
                           String companyPhone, String companyPhotoUrl) {
        AuditRecord record = new AuditRecord();
        record.setUserId(userId);
        record.setCompanyName(companyName);
        record.setCompanyAddress(companyAddress);
        record.setCompanyPhone(companyPhone);
        record.setCompanyPhotoUrl(companyPhotoUrl);
        record.setStatus("PENDING");
        auditRecordMapper.insert(record);
    }

    /**
     * 获取审批列表
     * @param status 审批状态筛选，null表示全部
     */
    public List<AuditRecordVO> getAuditList(String status) {
        // 1. 构建查询条件
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AuditRecord::getStatus, status);
        }
        wrapper.orderByDesc(AuditRecord::getCreateTime);

        // 2. 查询所有符合条件的记录
        List<AuditRecord> records = auditRecordMapper.selectList(wrapper);

        // 3. 收集所有涉及的用户ID
        Set<Long> userIds = records.stream()
                .map(AuditRecord::getUserId)
                .collect(Collectors.toSet());

        Set<Long> auditorIds = records.stream()
                .map(AuditRecord::getAuditorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 4. 批量查询用户信息
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, User> auditorMap = auditorIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(auditorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 5. 转换为VO并返回
        return records.stream()
                .map(record -> convertToVO(record, userMap, auditorMap))
                .collect(Collectors.toList());
    }

    /**
     * 获取待审核数量
     */
    public long getPendingCount() {
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecord::getStatus, "PENDING");
        return auditRecordMapper.selectCount(wrapper);
    }

    /**
     * 通过审核
     */
    public void approveAudit(Long auditorId, Long recordId) {
        AuditRecord record = auditRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }
        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }

        record.setStatus("APPROVED");
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        auditRecordMapper.updateById(record);

        // 同时更新用户表的审核状态
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            user.setAuditStatus("APPROVED");
            user.setCompanyName(record.getCompanyName());
            user.setCompanyAddress(record.getCompanyAddress());
            user.setCompanyPhone(record.getCompanyPhone());
            user.setCompanyPhotoUrl(record.getCompanyPhotoUrl());
            user.setAuditRemark(null);
            userMapper.updateById(user);
        }
    }

    /**
     * 驳回审核
     */
    public void rejectAudit(Long auditorId, Long recordId) {
        AuditRecord record = auditRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }
        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }

        record.setStatus("REJECTED");
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        auditRecordMapper.updateById(record);

        // 同时更新用户表的审核状态
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            user.setAuditStatus("REJECTED");
            user.setAuditRemark(null);
            userMapper.updateById(user);
        }
    }

    /**
     * 重置已通过审核的用户为未提交状态
     */
    public void resetAudit(Long adminId, Long recordId) {
        AuditRecord record = auditRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }
        if (!"APPROVED".equals(record.getStatus())) {
            throw new RuntimeException("仅可重置已通过的审核");
        }

        // 更新审批记录状态
        record.setStatus("RESET");
        record.setAuditorId(adminId);
        record.setAuditTime(LocalDateTime.now());
        auditRecordMapper.updateById(record);

        // 重置用户表审核状态和公司信息
        Long userId = record.getUserId();
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getAuditStatus, null)
                .set(User::getCompanyName, null)
                .set(User::getCompanyAddress, null)
                .set(User::getCompanyPhone, null)
                .set(User::getCompanyPhotoUrl, null)
                .set(User::getAuditRemark, null));
    }

    /**
     * 获取用户的最新审批记录
     */
    public AuditRecord getLatestByUserId(Long userId) {
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecord::getUserId, userId)
                .orderByDesc(AuditRecord::getCreateTime)
                .last("LIMIT 1");
        return auditRecordMapper.selectOne(wrapper);
    }

    private AuditRecordVO convertToVO(AuditRecord record, Map<Long, User> userMap, Map<Long, User> auditorMap) {
        User user = userMap.get(record.getUserId());
        User auditor = record.getAuditorId() != null ? auditorMap.get(record.getAuditorId()) : null;

        return AuditRecordVO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .nickname(user != null ? user.getNickname() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .companyName(record.getCompanyName())
                .companyAddress(record.getCompanyAddress())
                .companyPhone(record.getCompanyPhone())
                .companyPhotoUrl(record.getCompanyPhotoUrl())
                .status(record.getStatus())
                .auditRemark(record.getAuditRemark())
                .auditorId(record.getAuditorId())
                .auditorName(auditor != null ? auditor.getNickname() : null)
                .auditTime(record.getAuditTime())
                .createTime(record.getCreateTime())
                .build();
    }
}
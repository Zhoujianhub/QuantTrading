package com.quant.analysis.repository;

import com.quant.analysis.model.entity.AnalysisRequest;
import com.quant.analysis.model.enums.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分析请求Repository
 */
@Repository
public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {
    
    Optional<AnalysisRequest> findByRequestId(String requestId);
    
    List<AnalysisRequest> findByStockCodeOrderByCreatedAtDesc(String stockCode);
    
    List<AnalysisRequest> findByBatchId(String batchId);
    
    List<AnalysisRequest> findByStatus(AnalysisStatus status);
    
    List<AnalysisRequest> findByStatusInOrderByCreatedAtDesc(List<AnalysisStatus> statuses);
}

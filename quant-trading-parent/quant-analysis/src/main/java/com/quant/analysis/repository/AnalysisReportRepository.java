package com.quant.analysis.repository;

import com.quant.analysis.model.entity.AnalysisReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分析报告Repository
 */
@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    
    Optional<AnalysisReport> findByRequestId(String requestId);
    
    List<AnalysisReport> findByStockCodeOrderByCreatedAtDesc(String stockCode);
    
    List<AnalysisReport> findTop10ByOrderByCreatedAtDesc();
    
    Page<AnalysisReport> findByStockCodeContainingOrderByCreatedAtDesc(String stockCode, Pageable pageable);
    
    Page<AnalysisReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

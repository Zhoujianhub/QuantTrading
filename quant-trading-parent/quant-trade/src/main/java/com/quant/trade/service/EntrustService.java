package com.quant.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quant.trade.entity.Entrust;

public interface EntrustService extends IService<Entrust> {

    /**
     * 创建委托单
     * @param entrust 委托单信息
     * @return 委托单编号
     */
    String createEntrust(Entrust entrust);

    /**
     * 根据客户编号查询委托单
     * @param clientId 客户编号
     * @return 委托单列表
     */
    java.util.List<Entrust> getEntrustsByClientId(String clientId);
}
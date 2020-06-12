package com.edward.gware.manage.gware.mapper;

import com.edward.gware.manage.gware.bean.WareSku;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */

public interface WareSkuMapper extends Mapper<WareSku> {


    public Integer selectStockBySkuid(String skuid);

    public Integer incrStockLocked(WareSku wareSku);

    public Integer selectStockBySkuidForUpdate(WareSku wareSku);

    public Integer deliveryStock(WareSku wareSku);

    public List<WareSku> selectWareSkuAll();
}

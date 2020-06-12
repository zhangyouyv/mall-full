package com.edward.gmall.service;

import com.edward.gmall.bean.OmsOrder;

public interface OrderService {

    boolean checkTradeCode(String memberId, String tradeCode);

    String generateTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeCode(String outTradeCode);

    void updateOrderStatus(OmsOrder omsOrder);

}

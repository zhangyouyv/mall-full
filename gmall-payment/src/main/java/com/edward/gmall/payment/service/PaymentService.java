package com.edward.gmall.payment.service;

import com.edward.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendDelayPaymentCheckResult(String outTradeCode, int count);

    Map<String, Object> checkAlipayPayment(String out_trade_no);
}

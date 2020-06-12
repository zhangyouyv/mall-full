package com.edward.gmall.payment.mq;

import com.edward.gmall.bean.PaymentInfo;
import com.edward.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_CHECK_QUEUE")
    public void consumePaymentCheckResult(MapMessage mapMessage) throws JMSException {

        //直接获取消息 todo了解下jmstemplate
        String out_trade_no = mapMessage.getString("out_trade_no");
        Integer count = 0;
        if (mapMessage.getString("count") != null) {
            count = Integer.parseInt(mapMessage.getString("count"));
        }

        //调用paymentService的支付宝的检查接口
        Map<String, Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);
        if (resultMap == null || resultMap.isEmpty()) {
            count--;
            paymentService.sendDelayPaymentCheckResult(out_trade_no, count);
        } else {
            String tradeStatus = (String) resultMap.get("trade_status");

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                //支付成功则通知修改支付状态
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no"));
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackContent((String) resultMap.get("msg"));
                paymentInfo.setCallbackTime(new Date());
                paymentService.updatePaymentInfo(paymentInfo);
                System.out.println("已经支付成功，调用支付任务，修改支付信息和发送支付成功的队列");
            } else {
                if (count > 0) {
                    //继续发送延时任务，计算延时时间
                    System.out.println("支付未成功,剩余检查次数为" + count);
                    count--;
                    paymentService.sendDelayPaymentCheckResult(out_trade_no, count);
                } else {
                    System.out.println("检查次数用尽");
                }

            }
        }


    }
}

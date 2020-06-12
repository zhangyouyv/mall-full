package com.edward.gmall.order.mq;

import com.edward.gmall.bean.OmsOrder;
import com.edward.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderServiceMqListener {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {

        //直接获取消息 todo了解下jmstemplate
        String out_trade_no = mapMessage.getString("out_trade_no");
        //System.out.println(out_trade_no);

        //修改订单状态
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        orderService.updateOrderStatus(omsOrder);


    }
}

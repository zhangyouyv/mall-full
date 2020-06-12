package com.edward.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.edward.gmall.bean.PaymentInfo;
import com.edward.gmall.mq.ActiveMQUtil;
import com.edward.gmall.payment.mapper.PaymentInfoMapper;
import com.edward.gmall.payment.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {

        //幂等性检查
        PaymentInfo paymentInfo1 = new PaymentInfo();
        paymentInfo1.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfo2 = paymentInfoMapper.selectOne(paymentInfo1);
        if (StringUtils.isNotBlank(paymentInfo2.getPaymentStatus()) && paymentInfo2.getPaymentStatus().equals("已支付")) {
            return;
        } else {
            Example example = new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("orderSn", paymentInfo.getOrderSn());

            Connection connection = null;
            Session session = null;
            try {
                //获取消息连接
                connection = activeMQUtil.getConnectionFactory().createConnection();
                //参数表示支持事务模式
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
            } catch (JMSException e) {
                e.printStackTrace();
            }
            try {
                paymentInfoMapper.updateByExampleSelective(paymentInfo, example);

                //支付成功了之后，订单服务需要更新了，库存更新，物流更新
                Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                //创建消息生产者
                MessageProducer producer = session.createProducer(payment_success_queue);
                //声明消息类型
                ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();

                //填充消息内容
                activeMQMapMessage.setString("out_trade_no", paymentInfo.getOrderSn());
                //发送消息
                producer.send(activeMQMapMessage);

                session.commit();
            } catch (Exception e) {
                //消息回滚
                try {
                    session.rollback();
                } catch (JMSException jmsException) {
                    jmsException.printStackTrace();
                }
            } finally {
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void sendDelayPaymentCheckResult(String outTradeCode, int count) {
        Connection connection = null;
        Session session = null;
        try {
            //获取消息连接
            connection = activeMQUtil.getConnectionFactory().createConnection();
            //参数表示支持事务模式
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {

            //支付成功了之后，订单服务需要更新了，库存更新，物流更新
            Queue payment_result_check_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            //创建消息生产者
            MessageProducer producer = session.createProducer(payment_result_check_queue);
            //声明消息类型
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();

            //填充消息内容
            activeMQMapMessage.setString("out_trade_no", outTradeCode);
            activeMQMapMessage.setInt("count", count);
            //延迟队列,第一个参数计划类型，第二个延迟时间长短单位毫秒，
            // 需要在activemq的broker标签中添加支持计划标签schedulerSupport="true"（放在标签最外侧）
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 10);

            //发送消息
            producer.send(activeMQMapMessage);

            session.commit();
        } catch (Exception e) {
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException jmsException) {
                jmsException.printStackTrace();
            }
        } finally {
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {

        Map<String, Object> resultMap = new HashMap<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用支付宝支付检查接口成功");
            resultMap.put("trade_no", response.getTradeNo());
            resultMap.put("out_trade_no", response.getOutTradeNo());
            resultMap.put("trade_status", response.getTradeStatus());
        } else {
            System.out.println("交易可能未创建,调用失败");
        }
        return resultMap;
    }
}

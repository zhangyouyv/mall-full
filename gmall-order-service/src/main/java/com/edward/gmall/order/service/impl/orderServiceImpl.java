package com.edward.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.edward.gmall.bean.OmsOrder;
import com.edward.gmall.bean.OmsOrderItem;
import com.edward.gmall.mq.ActiveMQUtil;
import com.edward.gmall.order.mapper.OmsOrderItemMapper;
import com.edward.gmall.order.mapper.OmsOrderMapper;
import com.edward.gmall.service.OrderService;
import com.edward.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class orderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public boolean checkTradeCode(String memberId, String tradeCode) {

        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";

            //获取tradeCode
            String tradeCodeFrCache = jedis.get(tradeKey);

            //对比防重删令牌,lua脚本,防止并发订单攻击
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),
                    Collections.singletonList(tradeCodeFrCache));

            //验证交易码是否一致
            if (eval != null && eval != 0) {
                //交易码一致就删除掉交易码
                //jedis.del("user:" + memberId + ":tradeCode");//为了防止订单并发攻击，可以用lua脚本防止
                return true;
            } else {
                return false;
            }
        } finally {
            jedis.close();
        }
    }

    @Override
    public String generateTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();

        //随机生成交易码
        String tradeCode = UUID.randomUUID().toString();

        //存到redis中,过期时间一般是15或者30分钟
        jedis.setex("user:" + memberId + ":tradeCode", 60 * 15, tradeCode);

        jedis.close();
        return tradeCode;
    }


    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();

        //保存订单详情
        List<OmsOrderItem> omsOrderItemList = omsOrder.getOmsOrderItemList();
        for (OmsOrderItem omsOrderItem : omsOrderItemList) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeCode(String outTradeCode) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeCode);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrderStatus(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());

        omsOrder.setStatus(1);

        omsOrderMapper.updateByExampleSelective(omsOrder, example);
        //发送消息通知库存
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
            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            //创建消息生产者
            MessageProducer producer = session.createProducer(order_pay_queue);
            //声明消息类型
            TextMessage textMQMessage = new ActiveMQTextMessage();

            //获取订单信息
            OmsOrder omsOrder2 = new OmsOrder();
            omsOrder2.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResponse = omsOrderMapper.selectOne(omsOrder2);

            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrder2.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItem);

            omsOrderResponse.setOmsOrderItemList(select);


            //填充消息内容
            textMQMessage.setText(JSON.toJSONString(omsOrderResponse));
            //发送消息
            producer.send(textMQMessage);

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

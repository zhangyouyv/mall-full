package com.edward.gmall.seckill.controller;

import com.edward.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;



@Controller
public class SecKillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    /*
    * 先到先得式秒杀
    * */
    @RequestMapping("seckill")
    @ResponseBody
    public String seckill(){
        Jedis jedis = redisUtil.getJedis();
        RSemaphore semaphore = redissonClient.getSemaphore("prod");
        boolean b = semaphore.tryAcquire();
        //查看库存
        Integer stock = Integer.parseInt(jedis.get("prod"));
        if (b) {
            System.out.println("还剩商品" + stock + "件,目前有" + (1000 - stock) + "人在抢");
        } else {
            System.out.println("还剩商品" + stock + "件,某人抢购失败");
        }

        jedis.close();
        return "1";
    }

    /*
    *运气式秒杀
    * */
    @RequestMapping("kill")
    @ResponseBody
    public String kill(){

        Jedis jedis = redisUtil.getJedis();

        //开启商品的监控
        jedis.watch("prod");
        //查看库存
        Integer stock = Integer.parseInt(jedis.get("prod"));


        if(stock>0){
            //开启事务
            Transaction multi = jedis.multi();
            //抢购
            multi.incrBy("prod", -1);
            List<Object> exec = multi.exec();

            if(exec!=null && exec.size()>0){
                System.out.println("还剩商品"+stock+"件,目前有"+(100000-stock)+"人在抢");
            }else{
                System.out.println("还剩商品"+stock+"件,某人抢购失败");
            }

        }

        jedis.close();

        return "1";
    }
}

package com.edward.gmall.order.controller;

import com.edward.gmall.annotations.LoginRequired;
import com.edward.gmall.bean.OmsCartItem;
import com.edward.gmall.bean.OmsOrder;
import com.edward.gmall.bean.OmsOrderItem;
import com.edward.gmall.bean.UmsMemberReceiveAddress;
import com.edward.gmall.service.CartService;
import com.edward.gmall.service.OrderService;
import com.edward.gmall.service.SkuService;
import com.edward.gmall.service.UserService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String deliveryAddressId, String tradeCode, BigDecimal totalPrice, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickName");

        //首先验证交易码，看是否重复提交
        boolean success = orderService.checkTradeCode(memberId, tradeCode);

        if (success) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount();支付运费之后生成物流信息
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);

            String outTradeCode = "gmall";
            outTradeCode = outTradeCode + System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeCode += simpleDateFormat.format(new Date());
            omsOrder.setOrderSn(outTradeCode);//用来和其他系统交互的外部订单号
            omsOrder.setTotalAmount(totalPrice);
            omsOrder.setOrderType(1);

            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(deliveryAddressId);

            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());

            //唯一时间加减工具类
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            Date time = calendar.getTime();
            omsOrder.setReceiveTime(time);

            omsOrder.setSourceType(0);
            omsOrder.setStatus(1);

            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            //用id查出来用户需要付款的商品和总金额
            for (OmsCartItem omsCartItem : omsCartItems) {
                if ("1".equals(omsCartItem.getIsChecked())) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();

                    //验价
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(), omsCartItem.getPrice());
                    if (b = false) {
                        ModelAndView modelAndView = new ModelAndView("tradeFail");
                        return modelAndView;
                    }

                    // 验库存,远程调用httpclient.
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    String outTradeCode1 = "gmall";
                    outTradeCode1 = outTradeCode + System.currentTimeMillis();
                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("YYYYMMDDHHmmss");
                    outTradeCode1 += simpleDateFormat.format(new Date());
                    omsOrderItem.setOrderSn(outTradeCode);//用来和其他系统交互的外部订单号
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("11111111111111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");


                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItemList(omsOrderItems);


            //把数据写到数据库，同时删掉购物车对应数据

            orderService.saveOrder(omsOrder);

            //重定向到支付系统
            ModelAndView modelAndView = new ModelAndView("redirect:http://payment.gmall.com:8087/index");
            modelAndView.addObject("outTradeCode", outTradeCode);
            modelAndView.addObject("totalPrice", totalPrice);

            return modelAndView;
        } else {
            ModelAndView modelAndView = new ModelAndView("tradeFail");
            return modelAndView;
        }
    }

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        BigDecimal totalPrice = new BigDecimal(0);
        for (OmsCartItem omsCartItem : omsCartItems) {
            if ("1".equals(omsCartItem.getIsChecked())) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                totalPrice = omsCartItem.getPrice().multiply(omsCartItem.getQuantity()).add(totalPrice);
                omsOrderItems.add(omsOrderItem);
            }
        }


        modelMap.put("orderDetailList", omsOrderItems);

        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);
        modelMap.put("userAddressList", receiveAddressByMemberId);
        modelMap.put("totalPrice", totalPrice);

        //为了避免重复提交订单，需要生成交易码，避免重复提交
        String tradeCode = orderService.generateTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);
        return "trade";
    }
}

package com.edward.gmall.cart.controller;

import com.alibaba.fastjson.JSON;
import com.edward.gmall.annotations.LoginRequired;
import com.edward.gmall.bean.OmsCartItem;
import com.edward.gmall.bean.PmsSkuInfo;
import com.edward.gmall.service.CartService;
import com.edward.gmall.service.SkuService;
import com.edward.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin
public class CartController {

    @Reference//(version = "1.0.0")
    CartService cartService;
    @Reference
    SkuService skuService;

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response,ModelMap modelMap){

        return "string";
    }


        @RequestMapping("checkCart")
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response,ModelMap modelMap){

        String memberId = "1";
        //调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        cartService.checkCart(omsCartItem);

        //将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        return "cartListInner";
    }

    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, BigDecimal quantity, HttpServletRequest request, HttpServletResponse response){
        //调用商品服务查询商品信息
        PmsSkuInfo pmsSkuInfo = skuService.getItemById(skuId);
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //把商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductSkuId(pmsSkuInfo.getId());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSn("1111111122222222555");
        omsCartItem.setProductSubTitle("");
        omsCartItem.setQuantity(quantity);
        //判断用户是否登录
        String memberId = "1";//空串表示未登录反之表示登录


        if(StringUtils.isBlank(memberId)){
            //用户未登录，操作Cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){

                //Cookie不为空
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
                //当前商品在cookie中是否存在
                boolean exist= isItemInCart(omsCartItem,omsCartItems);

                //存在，更新数量
                if(exist){
                    for (OmsCartItem cartItem : omsCartItems) {
                        if(cartItem.getProductId().equals(omsCartItem.getProductId())){
                            omsCartItem.setQuantity(omsCartItem.getQuantity().add(cartItem.getQuantity()));
                        }
                    }

                }else{
                    //不存在，添加商品
                    omsCartItems.add(omsCartItem);
                }
            }else{
                //Cookie为空
                omsCartItems.add(omsCartItem);
            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems),60*60*72,true);
        }else{
            //用户已经登录，操作数据库
            //从数据库查出该用户的购物车数据
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(skuId,memberId);
            if(omsCartItemFromDb==null){
                //该用户没有添加过当前商品
                omsCartItem.setMemberId(memberId);
                cartService.addCart(omsCartItem);
            }else{
                //该用户添加过商品，更新数量
                omsCartItemFromDb.setQuantity(omsCartItem.getQuantity().add(omsCartItemFromDb.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);

            }

            //同步缓存
            cartService.flushCartCache(memberId);


        }

        //调用购物车服务

        //重定向不能直接访问templates下面的文件，因为templates中包含java代码
        //为了安全，在服务器中隐藏，所以需要把这个放到static下面
        return "redirect:/success.html";
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String getCartList(HttpServletRequest request, ModelMap modelMap){
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = "1";

        if(StringUtils.isNotBlank(memberId)){
            //已经登录查询db缓存
            omsCartItems =  cartService.cartList(memberId);

        }else{
            //没有登陆查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }

        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }

        modelMap.put("cartList",omsCartItems);
        return "cartList";
    }

    private boolean isItemInCart(OmsCartItem omsCartItem, List<OmsCartItem> omsCartItems) {
        boolean b = false;
        String productId = omsCartItem.getProductSkuId();
        for (OmsCartItem cartItem : omsCartItems) {
            if(productId.equals(cartItem.getProductSkuId())){
                b = true;
            }else{
                b = false;
            }
        }
        return b;
    }
}

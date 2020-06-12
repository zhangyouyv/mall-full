package com.edward.gmall.service;

import com.edward.gmall.bean.UmsMember;
import com.edward.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String deliveryAddressId);
}

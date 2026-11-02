package com.macro.mall.service;

import java.util.List;

/** 商品下架后的外部变更通知端口。 */
public interface PmsProductUnlistingPort {
    void notifyUnlisted(List<Long> productIds);
}

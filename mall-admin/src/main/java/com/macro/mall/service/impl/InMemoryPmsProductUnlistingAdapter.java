package com.macro.mall.service.impl;

import com.macro.mall.service.PmsProductUnlistingPort;
import org.springframework.stereotype.Service;

import java.util.List;

/** 教学用假实现；只记录最近批次；真实部署应替换为搜索同步适配器。 */
@Service
public class InMemoryPmsProductUnlistingAdapter implements PmsProductUnlistingPort {
    private List<Long> notifiedProductIds = List.of();

    @Override
    public synchronized void notifyUnlisted(List<Long> productIds) {
        notifiedProductIds = List.copyOf(productIds);
    }

    public synchronized List<Long> getNotifiedProductIds() {
        return notifiedProductIds;
    }
}

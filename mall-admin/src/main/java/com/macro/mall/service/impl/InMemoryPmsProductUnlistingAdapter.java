package com.macro.mall.service.impl;

import com.macro.mall.service.PmsProductUnlistingPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** 教学用假实现；真实部署应替换为搜索同步适配器。 */
@Service
public class InMemoryPmsProductUnlistingAdapter implements PmsProductUnlistingPort {
    private final List<Long> notifiedProductIds = new ArrayList<>();

    @Override
    public synchronized void notifyUnlisted(List<Long> productIds) {
        notifiedProductIds.addAll(productIds);
    }

    public synchronized List<Long> getNotifiedProductIds() {
        return List.copyOf(notifiedProductIds);
    }
}

package com.macro.mall.portal.service.impl;

import com.macro.mall.model.PmsProductExample;

final class ProductVisibilityCriteria {
    private ProductVisibilityCriteria() {
    }

    static PmsProductExample.Criteria apply(PmsProductExample.Criteria criteria) {
        return criteria.andDeleteStatusEqualTo(0).andPublishStatusEqualTo(1);
    }
}

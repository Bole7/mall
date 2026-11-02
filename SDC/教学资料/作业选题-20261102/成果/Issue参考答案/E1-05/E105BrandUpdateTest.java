package com.macro.mall.service.impl;

import com.macro.mall.dto.PmsBrandParam;
import com.macro.mall.mapper.PmsBrandMapper;
import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsBrand;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductExample;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class E105BrandUpdateTest {
    @Test
    void propagatesNameToProductsBeforeUpdatingBrand() {
        PmsBrandMapper brands = mock(PmsBrandMapper.class);
        PmsProductMapper products = mock(PmsProductMapper.class);
        PmsBrandServiceImpl service = new PmsBrandServiceImpl();
        ReflectionTestUtils.setField(service, "brandMapper", brands);
        ReflectionTestUtils.setField(service, "productMapper", products);
        when(brands.updateByPrimaryKeySelective(any(PmsBrand.class))).thenReturn(1);
        PmsBrandParam param = new PmsBrandParam();
        param.setName("New Brand");
        param.setFirstLetter("N");

        assertEquals(1, service.updateBrand(42L, param));

        ArgumentCaptor<PmsProduct> product = ArgumentCaptor.forClass(PmsProduct.class);
        ArgumentCaptor<PmsProductExample> condition = ArgumentCaptor.forClass(PmsProductExample.class);
        ArgumentCaptor<PmsBrand> brand = ArgumentCaptor.forClass(PmsBrand.class);
        InOrder order = inOrder(products, brands);
        order.verify(products).updateByExampleSelective(product.capture(), condition.capture());
        order.verify(brands).updateByPrimaryKeySelective(brand.capture());
        assertEquals("New Brand", product.getValue().getBrandName());
        assertEquals(1, condition.getValue().getOredCriteria().size());
        assertEquals(1, condition.getValue().getOredCriteria().get(0).getAllCriteria().size());
        assertEquals("brand_id =", condition.getValue().getOredCriteria().get(0).getAllCriteria().get(0).getCondition());
        assertEquals(42L, condition.getValue().getOredCriteria().get(0).getAllCriteria().get(0).getValue());
        assertEquals(42L, brand.getValue().getId());
        assertEquals("New Brand", brand.getValue().getName());
    }
}

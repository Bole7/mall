package com.macro.mall.portal;

import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsProductExample;
import com.macro.mall.portal.service.impl.HomeServiceImpl;
import com.macro.mall.portal.service.impl.PmsPortalBrandServiceImpl;
import com.macro.mall.portal.service.impl.PmsPortalProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Proxy;

class ProductVisibilityRulesTest {
    @Test
    void allPortalProductQueriesApplyTheSameVisibilityConditions() {
        var examples = new java.util.ArrayList<PmsProductExample>();
        PmsProductMapper mapper = (PmsProductMapper) Proxy.newProxyInstance(
                PmsProductMapper.class.getClassLoader(), new Class[]{PmsProductMapper.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("selectByExample")) {
                        examples.add((PmsProductExample) args[0]);
                        return Collections.emptyList();
                    }
                    return defaultValue(method.getReturnType());
                });

        PmsPortalProductServiceImpl search = new PmsPortalProductServiceImpl();
        ReflectionTestUtils.setField(search, "productMapper", mapper);
        search.search("", null, null, 1, 10, 1);

        PmsPortalBrandServiceImpl brand = new PmsPortalBrandServiceImpl();
        ReflectionTestUtils.setField(brand, "productMapper", mapper);
        brand.productList(7L, 1, 10);

        HomeServiceImpl home = new HomeServiceImpl();
        ReflectionTestUtils.setField(home, "productMapper", mapper);
        home.recommendProductList(10, 1);

        assertEquals(3, examples.size());
        for (PmsProductExample example : examples) {
            var conditions = example.getOredCriteria().get(0).getAllCriteria();
            assertTrue(conditions.stream().anyMatch(c -> "delete_status =".equals(c.getCondition()) && Integer.valueOf(0).equals(c.getValue())));
            assertTrue(conditions.stream().anyMatch(c -> "publish_status =".equals(c.getCondition()) && Integer.valueOf(1).equals(c.getValue())));
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return 0;
    }
}

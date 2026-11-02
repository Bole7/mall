package com.macro.mall.sdc.e4;

import com.macro.mall.portal.dao.PortalProductDao;
import com.macro.mall.portal.service.impl.OmsPromotionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;

import static org.mockito.Mockito.*;

class PromotionWiringTest {
    @Test void springInjectsTheDaoIntoTheSingleConstructor() {
        PortalProductDao dao = mock(PortalProductDao.class, withSettings().mockMaker("mock-maker-subclass"));
        when(dao.getPromotionProductList(anyList())).thenReturn(Collections.emptyList());
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(PortalProductDao.class, () -> dao);
            context.register(OmsPromotionServiceImpl.class);
            context.refresh();
            context.getBean(OmsPromotionServiceImpl.class).calcCartPromotion(Collections.emptyList());
            verify(dao).getPromotionProductList(Collections.emptyList());
        }
    }
}

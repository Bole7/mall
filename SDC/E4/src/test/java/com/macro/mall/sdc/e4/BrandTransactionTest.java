package com.macro.mall.sdc.e4;

import com.macro.mall.mapper.SmsHomeBrandMapper;
import com.macro.mall.model.SmsHomeBrand;
import com.macro.mall.service.SmsHomeBrandService;
import com.macro.mall.service.impl.SmsHomeBrandServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BrandTransactionTest {
    interface SelfCallingService extends SmsHomeBrandService {
        int callCreateInternally();
    }

    static class SelfCallingFixture extends SmsHomeBrandServiceImpl implements SelfCallingService {
        public int callCreateInternally() {
            return create(Arrays.asList(new SmsHomeBrand()));
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {}

    @Test void selfCallDoesNotGoThroughTransactionProxy() {
        SmsHomeBrandMapper mapper = mock(SmsHomeBrandMapper.class);
        PlatformTransactionManager tx = mock(PlatformTransactionManager.class);
        when(tx.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(Config.class, SelfCallingFixture.class);
            context.registerBean(SmsHomeBrandMapper.class, () -> mapper);
            context.registerBean(PlatformTransactionManager.class, () -> tx);
            context.refresh();
            SelfCallingService service = context.getBean(SelfCallingService.class);
            assertTrue(AopUtils.isAopProxy(service));
            assertEquals(1, service.callCreateInternally());
            verifyNoInteractions(tx);
            assertEquals(1, service.create(Arrays.asList(new SmsHomeBrand())));
            verify(tx).commit(any());
        }
    }

    @Test void proxyCommitsAndRollsBackButDirectInstanceBypassesAdvice() {
        SmsHomeBrandMapper mapper = mock(SmsHomeBrandMapper.class);
        PlatformTransactionManager tx = mock(PlatformTransactionManager.class);
        when(tx.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(Config.class, SmsHomeBrandServiceImpl.class);
            context.registerBean(SmsHomeBrandMapper.class, () -> mapper);
            context.registerBean(PlatformTransactionManager.class, () -> tx);
            context.refresh();
            SmsHomeBrandService service = context.getBean(SmsHomeBrandService.class);
            assertTrue(AopUtils.isAopProxy(service));
            SmsHomeBrand first = new SmsHomeBrand();
            SmsHomeBrand second = new SmsHomeBrand();
            assertEquals(2, service.create(Arrays.asList(first, second)));
            assertEquals(1, first.getRecommendStatus());
            assertEquals(0, first.getSort());
            verify(mapper, times(2)).insert(any());
            verify(tx).commit(any());
            verify(tx, never()).rollback(any());

            reset(mapper, tx);
            when(tx.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
            when(mapper.insert(any())).thenReturn(1).thenThrow(new IllegalStateException("insert failed"));
            assertThrows(IllegalStateException.class,
                    () -> service.create(Arrays.asList(new SmsHomeBrand(), new SmsHomeBrand())));
            verify(tx).rollback(any());
            verify(tx, never()).commit(any());

            reset(mapper, tx);
            SmsHomeBrandServiceImpl direct = new SmsHomeBrandServiceImpl();
            ReflectionTestUtils.setField(direct, "homeBrandMapper", mapper);
            assertEquals(1, direct.create(Arrays.asList(new SmsHomeBrand())));
            verifyNoInteractions(tx);
        }
    }
}

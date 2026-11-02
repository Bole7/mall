package com.macro.mall.portal.sdc;

import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.portal.dao.HomeDao;
import com.macro.mall.portal.domain.FlashPromotionProduct;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.domain.HomeFlashPromotion;
import com.macro.mall.portal.service.impl.HomeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SDCE106Test {
    @Test
    void contentAggregatesSixSectionsFromTheirDependencies() {
        HomeServiceImpl service = new HomeServiceImpl();
        SmsHomeAdvertiseMapper advertiseMapper = mock(SmsHomeAdvertiseMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        HomeDao homeDao = mock(HomeDao.class, withSettings().mockMaker("mock-maker-subclass"));
        SmsFlashPromotionMapper flashPromotionMapper = mock(SmsFlashPromotionMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        SmsFlashPromotionSessionMapper sessionMapper = mock(SmsFlashPromotionSessionMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        inject(service, advertiseMapper, homeDao, flashPromotionMapper, sessionMapper);

        List<SmsHomeAdvertise> adverts = List.of(new SmsHomeAdvertise());
        List<PmsBrand> brands = List.of(new PmsBrand());
        List<PmsProduct> newProducts = List.of(new PmsProduct());
        List<PmsProduct> hotProducts = List.of(new PmsProduct());
        List<CmsSubject> subjects = List.of(new CmsSubject());
        SmsFlashPromotion promotion = new SmsFlashPromotion();
        promotion.setId(51L);
        SmsFlashPromotionSession session = new SmsFlashPromotionSession();
        session.setId(52L);
        session.setStartTime(new Date(1_000L));
        session.setEndTime(new Date(2_000L));
        SmsFlashPromotionSession nextSession = new SmsFlashPromotionSession();
        nextSession.setStartTime(new Date(3_000L));
        nextSession.setEndTime(new Date(4_000L));
        List<FlashPromotionProduct> flashProducts = List.of(new FlashPromotionProduct());
        when(advertiseMapper.selectByExample(any(SmsHomeAdvertiseExample.class))).thenReturn(adverts);
        when(flashPromotionMapper.selectByExample(any(SmsFlashPromotionExample.class))).thenReturn(List.of(promotion));
        when(sessionMapper.selectByExample(any(SmsFlashPromotionSessionExample.class)))
                .thenReturn(List.of(session), List.of(nextSession));
        when(homeDao.getRecommendBrandList(0, 6)).thenReturn(brands);
        when(homeDao.getNewProductList(0, 4)).thenReturn(newProducts);
        when(homeDao.getHotProductList(0, 4)).thenReturn(hotProducts);
        when(homeDao.getRecommendSubjectList(0, 4)).thenReturn(subjects);
        when(homeDao.getFlashProductList(51L, 52L)).thenReturn(flashProducts);

        HomeContentResult result = service.content();

        assertSame(adverts, result.getAdvertiseList());
        assertSame(brands, result.getBrandList());
        assertSame(newProducts, result.getNewProductList());
        assertSame(hotProducts, result.getHotProductList());
        assertSame(subjects, result.getSubjectList());
        assertNotNull(result.getHomeFlashPromotion());
        assertSame(session.getStartTime(), result.getHomeFlashPromotion().getStartTime());
        assertSame(session.getEndTime(), result.getHomeFlashPromotion().getEndTime());
        assertSame(nextSession.getStartTime(), result.getHomeFlashPromotion().getNextStartTime());
        assertSame(nextSession.getEndTime(), result.getHomeFlashPromotion().getNextEndTime());
        assertSame(flashProducts, result.getHomeFlashPromotion().getProductList());
        verify(homeDao).getRecommendBrandList(0, 6);
        verify(homeDao).getNewProductList(0, 4);
        verify(homeDao).getHotProductList(0, 4);
        verify(homeDao).getRecommendSubjectList(0, 4);
        verify(homeDao).getFlashProductList(51L, 52L);
        verify(sessionMapper, times(2)).selectByExample(any(SmsFlashPromotionSessionExample.class));
    }

    @Test
    void missingCampaignLeavesAnEmptyFlashPromotionWrapperWithoutBreakingHomeContent() {
        HomeServiceImpl service = new HomeServiceImpl();
        SmsHomeAdvertiseMapper advertiseMapper = mock(SmsHomeAdvertiseMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        HomeDao homeDao = mock(HomeDao.class, withSettings().mockMaker("mock-maker-subclass"));
        SmsFlashPromotionMapper flashPromotionMapper = mock(SmsFlashPromotionMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        SmsFlashPromotionSessionMapper sessionMapper = mock(SmsFlashPromotionSessionMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        inject(service, advertiseMapper, homeDao, flashPromotionMapper, sessionMapper);
        when(advertiseMapper.selectByExample(any(SmsHomeAdvertiseExample.class))).thenReturn(List.of());
        when(flashPromotionMapper.selectByExample(any(SmsFlashPromotionExample.class))).thenReturn(List.of());

        HomeContentResult result = service.content();

        assertNotNull(result);
        assertNotNull(result.getHomeFlashPromotion());
        HomeFlashPromotion flash = result.getHomeFlashPromotion();
        assertNull(flash.getStartTime());
        assertNull(flash.getEndTime());
        assertNull(flash.getNextStartTime());
        assertNull(flash.getNextEndTime());
        assertNull(flash.getProductList());
        verify(flashPromotionMapper).selectByExample(any(SmsFlashPromotionExample.class));
        verify(sessionMapper, never()).selectByExample(any(SmsFlashPromotionSessionExample.class));
        verify(homeDao, never()).getFlashProductList(any(), any());
    }

    private static void inject(HomeServiceImpl service, SmsHomeAdvertiseMapper advertiseMapper,
                               HomeDao homeDao, SmsFlashPromotionMapper flashPromotionMapper,
                               SmsFlashPromotionSessionMapper sessionMapper) {
        ReflectionTestUtils.setField(service, "advertiseMapper", advertiseMapper);
        ReflectionTestUtils.setField(service, "homeDao", homeDao);
        ReflectionTestUtils.setField(service, "flashPromotionMapper", flashPromotionMapper);
        ReflectionTestUtils.setField(service, "promotionSessionMapper", sessionMapper);
    }
}

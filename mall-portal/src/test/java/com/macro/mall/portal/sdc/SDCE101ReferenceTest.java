package com.macro.mall.portal.sdc;

import com.macro.mall.mapper.SmsCouponHistoryMapper;
import com.macro.mall.mapper.SmsCouponMapper;
import com.macro.mall.model.SmsCoupon;
import com.macro.mall.model.SmsCouponHistory;
import com.macro.mall.model.SmsCouponHistoryExample;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.service.UmsMemberCouponService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.impl.UmsMemberCouponServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SDCE101ReferenceTest {
    @Test
    void claimWritesMemberHistoryThenUpdatesCouponAndHistoryQueryUsesMember() throws Exception {
        var service = new UmsMemberCouponServiceImpl();
        var settings = withSettings().mockMaker("mock-maker-subclass");
        var memberService = mock(UmsMemberService.class, settings);
        var couponMapper = mock(SmsCouponMapper.class, settings);
        var historyMapper = mock(SmsCouponHistoryMapper.class, settings);
        var member = new UmsMember();
        member.setId(101L);
        member.setNickname("member-101");
        when(memberService.getCurrentMember()).thenReturn(member);

        var coupon = new SmsCoupon();
        coupon.setId(7L);
        coupon.setCount(2);
        coupon.setPerLimit(1);
        coupon.setEnableTime(new Date(0));
        coupon.setReceiveCount(4);
        when(couponMapper.selectByPrimaryKey(7L)).thenReturn(coupon);
        when(historyMapper.countByExample(any())).thenReturn(0L);
        when(historyMapper.selectByExample(any())).thenAnswer(invocation -> {
            var example = invocation.<SmsCouponHistoryExample>getArgument(0);
            var criteria = example.getOredCriteria().get(0).getAllCriteria();
            assertTrue(criteria.stream().anyMatch(c -> c.getCondition().startsWith("member_id =") && c.getValue().equals(101L)));
            assertTrue(criteria.stream().anyMatch(c -> c.getCondition().startsWith("use_status =") && c.getValue().equals(0)));
            return List.of();
        });
        ReflectionTestUtils.setField(service, "memberService", memberService);
        ReflectionTestUtils.setField(service, "couponMapper", couponMapper);
        ReflectionTestUtils.setField(service, "couponHistoryMapper", historyMapper);

        service.add(7L);

        var order = inOrder(historyMapper, couponMapper);
        order.verify(historyMapper).insert(argThat(h -> h.getCouponId().equals(7L)
                && h.getMemberId().equals(101L) && h.getMemberNickname().equals("member-101")
                && h.getGetType() == 1 && h.getUseStatus() == 0 && h.getCreateTime() != null));
        order.verify(couponMapper).updateByPrimaryKey(coupon);
        assertEquals(1, coupon.getCount());
        assertEquals(5, coupon.getReceiveCount());

        assertTrue(UmsMemberCouponService.class.getMethod("add", Long.class)
                .isAnnotationPresent(Transactional.class));
        assertEquals(List.of(), service.listHistory(0));
        verify(historyMapper, times(1)).insert(any(SmsCouponHistory.class));
    }
}

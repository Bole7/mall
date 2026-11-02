package com.macro.mall.portal;

import com.macro.mall.mapper.UmsMemberReceiveAddressMapper;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.UmsMemberReceiveAddress;
import com.macro.mall.model.UmsMemberReceiveAddressExample;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.impl.UmsMemberReceiveAddressServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UmsMemberReceiveAddressObservationTest {
    private final UmsMemberService memberService = mock(UmsMemberService.class, withSettings().mockMaker("mock-maker-subclass"));
    private final UmsMemberReceiveAddressMapper mapper = mock(UmsMemberReceiveAddressMapper.class, withSettings().mockMaker("mock-maker-subclass"));
    private final UmsMemberReceiveAddressServiceImpl service = new UmsMemberReceiveAddressServiceImpl();

    UmsMemberReceiveAddressObservationTest() {
        ReflectionTestUtils.setField(service, "memberService", memberService);
        ReflectionTestUtils.setField(service, "addressMapper", mapper);
        UmsMember member = new UmsMember();
        member.setId(73L);
        when(memberService.getCurrentMember()).thenReturn(member);
    }

    @Test
    void readsAreScopedToCurrentMemberAndDefaultUpdateClearsThenSets() {
        service.list();
        verify(mapper).selectByExample(argThat(query -> has(query, "member_id =", 73L)));

        clearInvocations(mapper);
        UmsMemberReceiveAddress request = new UmsMemberReceiveAddress();
        request.setDefaultStatus(1);
        request.setName("Ada");
        when(mapper.updateByExampleSelective(any(), any())).thenReturn(1);

        assertEquals(1, service.update(91L, request));

        ArgumentCaptor<UmsMemberReceiveAddress> rows = ArgumentCaptor.forClass(UmsMemberReceiveAddress.class);
        ArgumentCaptor<UmsMemberReceiveAddressExample> queries = ArgumentCaptor.forClass(UmsMemberReceiveAddressExample.class);
        InOrder order = inOrder(mapper);
        order.verify(mapper, times(2)).updateByExampleSelective(rows.capture(), queries.capture());

        assertEquals(0, rows.getAllValues().get(0).getDefaultStatus());
        assertTrue(has(queries.getAllValues().get(0), "member_id =", 73L));
        assertTrue(has(queries.getAllValues().get(0), "default_status =", 1));
        assertEquals(1, rows.getAllValues().get(1).getDefaultStatus());
        assertEquals("Ada", rows.getAllValues().get(1).getName());
        assertTrue(has(queries.getAllValues().get(1), "member_id =", 73L));
        assertTrue(has(queries.getAllValues().get(1), "id =", 91L));
    }

    private static boolean has(UmsMemberReceiveAddressExample example, String condition, Object value) {
        return example.getOredCriteria().stream()
                .flatMap(criteria -> criteria.getAllCriteria().stream())
                .anyMatch(criterion -> condition.equals(criterion.getCondition()) && value.equals(criterion.getValue()));
    }
}

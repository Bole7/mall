package com.macro.mall.sdc.e1;

import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberProductCollection;
import com.macro.mall.portal.domain.MemberReadHistory;
import com.macro.mall.portal.repository.MemberProductCollectionRepository;
import com.macro.mall.portal.repository.MemberReadHistoryRepository;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.impl.MemberCollectionServiceImpl;
import com.macro.mall.portal.service.impl.MemberReadHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MemberReadHistoryBehaviorTest {
    private final PmsProductMapper products = mock(PmsProductMapper.class);
    private final MemberReadHistoryRepository histories = mock(MemberReadHistoryRepository.class);
    private final MemberProductCollectionRepository collections = mock(MemberProductCollectionRepository.class);
    private final UmsMemberService members = mock(UmsMemberService.class);
    private final MemberReadHistoryServiceImpl historyService = new MemberReadHistoryServiceImpl();
    private final MemberCollectionServiceImpl collectionService = new MemberCollectionServiceImpl();
    private final UmsMember member = new UmsMember();

    @BeforeEach void setUp() {
        member.setId(42L);
        member.setNickname("member-42");
        member.setIcon("icon.png");
        when(members.getCurrentMember()).thenReturn(member);
        ReflectionTestUtils.setField(historyService, "memberService", members);
        ReflectionTestUtils.setField(historyService, "memberReadHistoryRepository", histories);
        ReflectionTestUtils.setField(historyService, "productMapper", products);
        ReflectionTestUtils.setField(historyService, "sqlEnable", false);
        ReflectionTestUtils.setField(collectionService, "memberService", members);
        ReflectionTestUtils.setField(collectionService, "productCollectionRepository", collections);
        ReflectionTestUtils.setField(collectionService, "productMapper", products);
        ReflectionTestUtils.setField(collectionService, "sqlEnable", false);
    }

    @Test void createOverwritesIdentityAndTimeAndAllowsRepeatedEvents() {
        MemberReadHistory supplied = new MemberReadHistory();
        supplied.setId("client-id");
        supplied.setMemberId(7L);
        supplied.setProductId(9L);
        Date before = new Date();
        assertEquals(1, historyService.create(supplied));
        Date after = new Date();
        assertEquals(42L, supplied.getMemberId());
        assertEquals("member-42", supplied.getMemberNickname());
        assertEquals("icon.png", supplied.getMemberIcon());
        assertNull(supplied.getId());
        assertNotNull(supplied.getCreateTime());
        assertFalse(supplied.getCreateTime().before(before) || supplied.getCreateTime().after(after));
        assertEquals(1, historyService.create(newHistory(9L)));
        verify(histories, times(2)).save(any(MemberReadHistory.class));
        verify(products, never()).selectByPrimaryKey(anyLong());
    }

    @Test void createRejectsMissingOrUnavailableProductsWhenSqlEnrichmentIsEnabled() {
        assertEquals(0, historyService.create(new MemberReadHistory()));
        verify(members, never()).getCurrentMember();
        ReflectionTestUtils.setField(historyService, "sqlEnable", true);
        when(products.selectByPrimaryKey(9L)).thenReturn(null);
        assertEquals(0, historyService.create(newHistory(9L)));
        PmsProduct deleted = new PmsProduct();
        deleted.setDeleteStatus(1);
        when(products.selectByPrimaryKey(10L)).thenReturn(deleted);
        assertEquals(0, historyService.create(newHistory(10L)));
        verify(histories, never()).save(any(MemberReadHistory.class));
    }

    @Test void listUsesCurrentMemberZeroBasedPageAndDescendingTimeRepositoryMethod() {
        Pageable expected = PageRequest.of(2, 7);
        historyService.list(3, 7);
        verify(histories).findByMemberIdOrderByCreateTimeDesc(42L, expected);
    }

    @Test void clearDeletesOnlyCurrentMembersHistory() {
        historyService.clear();
        verify(histories).deleteAllByMemberId(42L);
    }

    @Test void collectionDeduplicatesWhereHistoryRecordsEachVisit() {
        MemberProductCollection collection = new MemberProductCollection();
        collection.setProductId(9L);
        when(collections.findByMemberIdAndProductId(42L, 9L)).thenReturn(new MemberProductCollection());
        assertEquals(0, collectionService.add(collection));
        verify(collections, never()).save(any(MemberProductCollection.class));
        assertEquals(1, historyService.create(newHistory(9L)));
        verify(histories).save(any(MemberReadHistory.class));
        verify(collections).findByMemberIdAndProductId(42L, 9L);
    }

    private static MemberReadHistory newHistory(Long productId) {
        MemberReadHistory history = new MemberReadHistory();
        history.setProductId(productId);
        return history;
    }
}

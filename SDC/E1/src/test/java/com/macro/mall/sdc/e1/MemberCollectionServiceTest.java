package com.macro.mall.sdc.e1;

import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberProductCollection;
import com.macro.mall.portal.repository.MemberProductCollectionRepository;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.impl.MemberCollectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberCollectionServiceTest {
    private final PmsProductMapper productMapper = mock(PmsProductMapper.class);
    private final MemberProductCollectionRepository repository = mock(MemberProductCollectionRepository.class);
    private final UmsMemberService memberService = mock(UmsMemberService.class);
    private final MemberCollectionServiceImpl service = new MemberCollectionServiceImpl();
    private final UmsMember member = new UmsMember();

    @BeforeEach
    void setUp() {
        member.setId(101L);
        member.setNickname("member-101");
        member.setIcon("member.png");
        when(memberService.getCurrentMember()).thenReturn(member);
        ReflectionTestUtils.setField(service, "productMapper", productMapper);
        ReflectionTestUtils.setField(service, "productCollectionRepository", repository);
        ReflectionTestUtils.setField(service, "memberService", memberService);
    }

    @Test
    void sqlEnabledCopiesMySqlProductSnapshotAndUsesCurrentMemberForDeduplication() {
        ReflectionTestUtils.setField(service, "sqlEnable", true);
        when(repository.findByMemberIdAndProductId(101L, 9L)).thenReturn(null);
        PmsProduct product = new PmsProduct();
        product.setDeleteStatus(0);
        product.setName("Product");
        product.setSubTitle("Subtitle");
        product.setPrice(new BigDecimal("12.50"));
        product.setPic("product.png");
        when(productMapper.selectByPrimaryKey(9L)).thenReturn(product);
        MemberProductCollection request = new MemberProductCollection();
        request.setMemberId(999L);
        request.setProductId(9L);
        request.setProductName("untrusted request name");

        assertEquals(1, service.add(request));

        assertEquals(101L, request.getMemberId());
        assertEquals("member-101", request.getMemberNickname());
        assertEquals("member.png", request.getMemberIcon());
        assertEquals("Product", request.getProductName());
        assertEquals("Subtitle", request.getProductSubTitle());
        assertEquals("12.50", request.getProductPrice());
        assertEquals("product.png", request.getProductPic());
        verify(repository).findByMemberIdAndProductId(101L, 9L);
        verify(productMapper).selectByPrimaryKey(9L);
        verify(repository).save(request);
    }

    @Test
    void sqlDisabledPreservesRequestProductSnapshotAndSkipsMySql() {
        ReflectionTestUtils.setField(service, "sqlEnable", false);
        when(repository.findByMemberIdAndProductId(101L, 9L)).thenReturn(null);
        MemberProductCollection request = new MemberProductCollection();
        request.setProductId(9L);
        request.setProductName("request name");
        request.setProductSubTitle("request subtitle");
        request.setProductPrice("8.00");
        request.setProductPic("request.png");

        assertEquals(1, service.add(request));

        assertEquals("request name", request.getProductName());
        assertEquals("request subtitle", request.getProductSubTitle());
        assertEquals("8.00", request.getProductPrice());
        assertEquals("request.png", request.getProductPic());
        verifyNoInteractions(productMapper);
        verify(repository).save(request);
    }

    @Test
    void duplicateSkipsProductLookupAndSave() {
        ReflectionTestUtils.setField(service, "sqlEnable", true);
        when(repository.findByMemberIdAndProductId(101L, 9L)).thenReturn(new MemberProductCollection());
        MemberProductCollection request = new MemberProductCollection();
        request.setProductId(9L);

        assertEquals(0, service.add(request));

        verifyNoInteractions(productMapper);
        verify(repository, never()).save(any());
    }

    @Test
    void sqlEnabledRejectsMissingOrDeletedMySqlProduct() {
        ReflectionTestUtils.setField(service, "sqlEnable", true);
        when(repository.findByMemberIdAndProductId(101L, 9L)).thenReturn(null);
        when(productMapper.selectByPrimaryKey(9L)).thenReturn(null);
        MemberProductCollection request = new MemberProductCollection();
        request.setProductId(9L);
        assertEquals(0, service.add(request));
        verify(repository, never()).save(any());

        PmsProduct deleted = new PmsProduct();
        deleted.setDeleteStatus(1);
        when(productMapper.selectByPrimaryKey(10L)).thenReturn(deleted);
        request.setProductId(10L);
        assertEquals(0, service.add(request));
        verify(repository, never()).save(any());
    }

    @Test
    void listAndDetailAreScopedToCurrentMember() {
        PageRequest pageable = PageRequest.of(1, 5);
        Page<MemberProductCollection> expected = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findByMemberId(101L, pageable)).thenReturn(expected);
        MemberProductCollection detail = new MemberProductCollection();
        when(repository.findByMemberIdAndProductId(101L, 9L)).thenReturn(detail);

        assertSame(expected, service.list(2, 5));
        assertSame(detail, service.detail(9L));

        verify(repository).findByMemberId(101L, pageable);
        verify(repository).findByMemberIdAndProductId(101L, 9L);
    }

    @Test
    void addWithoutProductIdReturnsBeforeReadingCurrentMember() {
        assertEquals(0, service.add(new MemberProductCollection()));
        verifyNoInteractions(memberService, repository, productMapper);
    }
}

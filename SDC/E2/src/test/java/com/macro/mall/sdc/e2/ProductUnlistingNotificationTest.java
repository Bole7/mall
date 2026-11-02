package com.macro.mall.sdc.e2;

import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.service.PmsProductUnlistingPort;
import com.macro.mall.service.impl.InMemoryPmsProductUnlistingAdapter;
import com.macro.mall.service.impl.PmsProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductUnlistingNotificationTest {
    @Test
    void inMemoryAdapterKeepsAnInspectableSnapshot() {
        InMemoryPmsProductUnlistingAdapter adapter = new InMemoryPmsProductUnlistingAdapter();
        adapter.notifyUnlisted(List.of(7L, 8L));
        assertEquals(List.of(7L, 8L), adapter.getNotifiedProductIds());
    }

    @Test
    void unlistingAfterSuccessfulWriteNotifiesOnce() {
        Fixture f = fixture(2);
        assertEquals(2, f.service.updatePublishStatus(List.of(7L, 8L), 0));
        verify(f.port).notifyUnlisted(List.of(7L, 8L));
    }

    @Test
    void failedOrEmptyWriteDoesNotNotify() {
        Fixture failed = fixtureWithMapperFailure();
        assertThrows(RuntimeException.class, () -> failed.service.updatePublishStatus(List.of(7L), 0));
        verifyNoInteractions(failed.port);

        Fixture empty = fixture(0);
        assertEquals(0, empty.service.updatePublishStatus(List.of(7L), 0));
        verifyNoInteractions(empty.port);
    }

    @Test
    void nonUnlistingStatusDoesNotNotify() {
        Fixture f = fixture(1);
        assertEquals(1, f.service.updatePublishStatus(List.of(7L), 1));
        verifyNoInteractions(f.port);
    }

    @Test
    void notificationFailureDoesNotChangeDatabaseWriteResult() {
        Fixture f = fixture(1);
        doThrow(new IllegalStateException("fake notification failure")).when(f.port).notifyUnlisted(List.of(7L));
        assertEquals(1, f.service.updatePublishStatus(List.of(7L), 0));
        verify(f.mapper).updateByExampleSelective(any(), any());
    }

    private Fixture fixture(int count) {
        PmsProductMapper mapper = mock(PmsProductMapper.class);
        when(mapper.updateByExampleSelective(any(), any())).thenReturn(count);
        return fixture(mapper);
    }

    private Fixture fixtureWithMapperFailure() {
        PmsProductMapper mapper = mock(PmsProductMapper.class);
        when(mapper.updateByExampleSelective(any(), any())).thenThrow(new RuntimeException("database failure"));
        return fixture(mapper);
    }

    private Fixture fixture(PmsProductMapper mapper) {
        PmsProductServiceImpl service = new PmsProductServiceImpl();
        PmsProductUnlistingPort port = mock(PmsProductUnlistingPort.class);
        ReflectionTestUtils.setField(service, "productMapper", mapper);
        ReflectionTestUtils.setField(service, "productUnlistingPort", port);
        return new Fixture(service, mapper, port);
    }

    private record Fixture(PmsProductServiceImpl service, PmsProductMapper mapper, PmsProductUnlistingPort port) {}
}

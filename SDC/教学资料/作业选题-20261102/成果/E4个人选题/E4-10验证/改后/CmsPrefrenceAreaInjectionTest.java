package com.macro.mall.sdc.e4;

import com.macro.mall.mapper.CmsPrefrenceAreaMapper;
import com.macro.mall.model.CmsPrefrenceArea;
import com.macro.mall.model.CmsPrefrenceAreaExample;
import com.macro.mall.service.impl.CmsPrefrenceAreaServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CmsPrefrenceAreaInjectionTest {
    @Test void listAndSpringWiring() {
        CmsPrefrenceAreaMapper mapper = mock(CmsPrefrenceAreaMapper.class);
        List<CmsPrefrenceArea> empty = Collections.emptyList();
        CmsPrefrenceArea first = new CmsPrefrenceArea();
        CmsPrefrenceArea second = new CmsPrefrenceArea();
        List<CmsPrefrenceArea> expected = Arrays.asList(first, second);
        when(mapper.selectByExample(any())).thenReturn(empty, expected, expected);
        CmsPrefrenceAreaServiceImpl service = new CmsPrefrenceAreaServiceImpl(mapper);
        assertTrue(service.listAll().isEmpty());
        List<CmsPrefrenceArea> actual = service.listAll();
        assertEquals(2, actual.size());
        assertSame(first, actual.get(0));
        assertSame(second, actual.get(1));
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(CmsPrefrenceAreaMapper.class, () -> mapper);
            context.register(CmsPrefrenceAreaServiceImpl.class);
            context.refresh();
            assertSame(expected, context.getBean(CmsPrefrenceAreaServiceImpl.class).listAll());
        }
        ArgumentCaptor<CmsPrefrenceAreaExample> examples = ArgumentCaptor.forClass(CmsPrefrenceAreaExample.class);
        verify(mapper, times(3)).selectByExample(examples.capture());
        for (CmsPrefrenceAreaExample example : examples.getAllValues()) {
            assertTrue(example.getOredCriteria().isEmpty());
            assertNull(example.getOrderByClause());
            assertFalse(example.isDistinct());
        }
    }
}

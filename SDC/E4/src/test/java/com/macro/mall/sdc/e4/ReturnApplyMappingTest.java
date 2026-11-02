package com.macro.mall.sdc.e4;

import com.macro.mall.dao.OmsOrderReturnApplyDao;
import com.macro.mall.dto.OmsOrderReturnApplyResult;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class ReturnApplyMappingTest {
    @Test void detailMapsAssociatedAddressAndBindsId() throws Exception {
        DataSource ds = new UnpooledDataSource("org.h2.Driver", "jdbc:h2:mem:return_apply;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE oms_order_return_apply (id BIGINT PRIMARY KEY, company_address_id BIGINT, return_name VARCHAR(80))");
            s.execute("CREATE TABLE oms_company_address (id BIGINT PRIMARY KEY, address_name VARCHAR(80), name VARCHAR(80), phone VARCHAR(80), province VARCHAR(80), city VARCHAR(80), region VARCHAR(80), detail_address VARCHAR(80))");
            s.execute("INSERT INTO oms_company_address (id,address_name,name,city) VALUES (10,'Warehouse','Alice','Suzhou'), (20,'Depot','Bob','Hangzhou')");
            s.execute("INSERT INTO oms_order_return_apply VALUES (1,10,'Customer'), (2,NULL,'No address'), (3,20,'Other customer')");
        }
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration(new Environment("test", new JdbcTransactionFactory(), ds));
        for (String resource : new String[]{"com/macro/mall/mapper/OmsOrderReturnApplyMapper.xml", "com/macro/mall/mapper/OmsCompanyAddressMapper.xml", "dao/OmsOrderReturnApplyDao.xml"}) {
            try (InputStream input = org.apache.ibatis.io.Resources.getResourceAsStream(resource)) {
                new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
            }
        }
        try (SqlSession session = new SqlSessionFactoryBuilder().build(config).openSession()) {
            OmsOrderReturnApplyDao dao = session.getMapper(OmsOrderReturnApplyDao.class);
            OmsOrderReturnApplyResult result = dao.getDetail(1L);
            assertEquals(1L, result.getId());
            assertEquals("Customer", result.getReturnName());
            assertEquals(10L, result.getCompanyAddress().getId());
            assertEquals("Warehouse", result.getCompanyAddress().getAddressName());
            assertEquals("Alice", result.getCompanyAddress().getName());
            assertEquals("Suzhou", result.getCompanyAddress().getCity());
            OmsOrderReturnApplyResult other = dao.getDetail(3L);
            assertEquals(3L, other.getId());
            assertEquals("Other customer", other.getReturnName());
            assertEquals(20L, other.getCompanyAddress().getId());
            assertEquals("Depot", other.getCompanyAddress().getAddressName());
            assertEquals("Bob", other.getCompanyAddress().getName());
            assertEquals("Hangzhou", other.getCompanyAddress().getCity());
            assertNull(dao.getDetail(2L).getCompanyAddress());
            assertNull(dao.getDetail(999L));
        }
    }
}

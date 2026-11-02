package com.macro.mall.portal;

import com.macro.mall.portal.dao.HomeDao;
import com.macro.mall.model.PmsProduct;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeProductVisibilitySqlTest {
    @Test
    void homeQueriesExcludeDeletedPublishedProducts() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:home-products;MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE pms_product (id BIGINT PRIMARY KEY, name VARCHAR(100), publish_status INT, delete_status INT)");
            statement.execute("CREATE TABLE sms_home_new_product (product_id BIGINT, recommend_status INT, sort INT)");
            statement.execute("CREATE TABLE sms_home_recommend_product (product_id BIGINT, recommend_status INT, sort INT)");
            statement.execute("INSERT INTO pms_product VALUES (1, 'visible', 1, 0), (2, 'deleted', 1, 1), (3, 'unpublished', 0, 0)");
            statement.execute("INSERT INTO sms_home_new_product VALUES (1,1,3), (2,1,2), (3,1,1)");
            statement.execute("INSERT INTO sms_home_recommend_product VALUES (1,1,3), (2,1,2), (3,1,1)");
        }

        SqlSessionFactory factory = sqlSessionFactory(dataSource);
        try (SqlSession session = factory.openSession()) {
            HomeDao homeDao = session.getMapper(HomeDao.class);
            for (List<PmsProduct> products : List.of(homeDao.getNewProductList(0, 10), homeDao.getHotProductList(0, 10))) {
                assertEquals(1, products.size());
                assertEquals(1L, products.get(0).getId());
                assertTrue(products.stream().noneMatch(product -> product.getId() == 2L));
            }
        }
    }

    private SqlSessionFactory sqlSessionFactory(JdbcDataSource dataSource) throws Exception {
        Configuration configuration = new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
        for (String path : List.of("/com/macro/mall/mapper/PmsProductMapper.xml",
                "/com/macro/mall/mapper/PmsBrandMapper.xml", "/com/macro/mall/mapper/CmsSubjectMapper.xml",
                "/dao/HomeDao.xml")) {
            try (InputStream xml = getClass().getResourceAsStream(path)) {
                new XMLMapperBuilder(xml, configuration, path, configuration.getSqlFragments()).parse();
            }
        }
        return new SqlSessionFactoryBuilder().build(configuration);
    }
}

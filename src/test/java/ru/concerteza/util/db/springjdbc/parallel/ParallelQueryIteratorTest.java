package ru.concerteza.util.db.springjdbc.parallel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.UnhandledException;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.concerteza.util.collection.CtzCollectionUtils;
import ru.concerteza.util.collection.accessor.Accessor;
import ru.concerteza.util.collection.accessor.RoundRobinAccessor;
import ru.concerteza.util.concurrency.SameThreadExecutor;
import ru.concerteza.util.db.springjdbc.ResultSetIterator;
import ru.concerteza.util.db.springjdbc.RowMapperFunction;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static ru.concerteza.util.collection.CtzCollectionUtils.fireTransform;

/**
 * User: alexey
 * Date: 6/12/12
 */
public class ParallelQueryIteratorTest {

    @Test
    public void test() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:foo");
        JdbcTemplate jt = new JdbcTemplate(ds);
        jt.execute("create table foo(bar varchar(42))");
        jt.update("insert into foo(bar) values('41')");
        jt.update("insert into foo(bar) values('42')");
        jt.update("insert into foo(bar) values('43')");
        Accessor<DataSource> robin = RoundRobinAccessor.of(ImmutableList.<DataSource>of(ds));
        // single thread used, buffer must me bigger than data
        ParallelQueryIterator<String> iter = new ParallelQueryIterator<String>(robin, "select bar from foo",
                new SimpleMapper(), new Forker(1), new SameThreadExecutor(), 10);
        iter.start(ImmutableMap.<String, Object>of());
        assertEquals("41", iter.next());
        assertEquals("42", iter.next());
        assertEquals("43", iter.next());
        // check restart
        iter.start(ImmutableMap.<String, Object>of());
        assertEquals("41", iter.next());
        assertEquals("42", iter.next());
        assertEquals("43", iter.next());
    }

    /**
     * Multithreaded stress test
     */
//    @Test
    public void testStress() {
        { // single thread
            DataSource ds = createDS();
            JdbcTemplate jt = new JdbcTemplate(ds);
            long start = System.currentTimeMillis();
            long res = jt.query("select bar from foo", new Extractor());
//            1300
            System.out.println("10000 records from one thread: " + (System.currentTimeMillis() - start));
            assertEquals(res, 10000);
        }
        { // 20 threads
            int count = 20;
            ImmutableList.Builder<DataSource> builder = ImmutableList.builder();
            for(int i = 0; i < count; i++) builder.add(createDS());
            Accessor<DataSource> robin = RoundRobinAccessor.of(builder.build());
            long start = System.currentTimeMillis();
            ParallelQueryIterator<String> iter = new ParallelQueryIterator<String>(robin, "select bar from foo",
                    new SlowpokeMapper(), new Forker(count), Executors.newCachedThreadPool(), 100)
                    .start(ImmutableMap.<String, Object>of());
            long res = CtzCollectionUtils.fireTransform(iter);
//          2100
            System.out.println("200000 records from 20 threads: " + (System.currentTimeMillis() - start));
            assertEquals(res, 10000 * count);
        }
    }

    private DataSource createDS() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:" + RandomStringUtils.randomAlphanumeric(10));
        NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(ds);
        jt.getJdbcOperations().execute("create table foo(bar varchar(42))");
        for(int i=0; i< 10000; i++) {
            jt.update("insert into foo(bar) values(:str)", ImmutableMap.of("str", RandomStringUtils.randomAscii(42)));
        }
        return ds;
    }

    private class Extractor implements ResultSetExtractor<Long> {
        @Override
        public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
            Iterator<ResultSet> rsIter = ResultSetIterator.identity(rs);
            Iterator<String> strIter = Iterators.transform(rsIter, RowMapperFunction.of(new SlowpokeMapper()));
            return fireTransform(strIter);
        }
    }

    private class SimpleMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("bar");
        }
    }

    private class SlowpokeMapper implements RowMapper<String> {
        AtomicInteger count = new AtomicInteger(0);

        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                int co = count.incrementAndGet();
                if(0 == co % 10)Thread.sleep(1);
                return rs.getString("bar");
            } catch(InterruptedException e) {
                throw new UnhandledException(e);
            }
        }
    }

    private class Forker implements ParamsForker {
        private final int forksCount;

        private Forker(int forksCount) {
            this.forksCount = forksCount;
        }

        @Override
        public List<Map<String, ?>> fork(Map<String, ?> params) {
            ImmutableList.Builder<Map<String, ?>> builder = ImmutableList.builder();
            for(int i = 0; i < forksCount; i++) {
                builder.add(ImmutableMap.<String, Object>of());
            }
            return builder.build();
        }
    }
}

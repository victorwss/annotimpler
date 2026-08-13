package ninja.javahacker.test.annotimpler.jdbc;

import ninja.javahacker.test.ForTests;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.jdbc;
import module org.junit.jupiter.api;

public class JdbcApiTest {

    public interface EmptyDao {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ImplementedBy(RefusesPropertiesImplementation.class)
    public @interface RefusesProperties {
    }

    public static final class RefusesPropertiesImplementation implements Implementation {
        @Override
        public <E> CallContext<E> prepare(Class<E> k, Method m, PropertyBag props)
                throws BadImplementationException, PropertyBag.PropertyNotFoundException {
            throw new PropertyBag.PropertyNotFoundException(ConnectionFactoryKeyProperty.INSTANCE);
        }
    }

    private static Connection connection(List<String> calls) {
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                JdbcApiTest.class.getClassLoader(),
                new Class<?>[] {Connection.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "commit", "rollback", "close" -> {
                            calls.add(method.getName());
                            return null;
                        }
                        case "toString" -> {
                            return "connection";
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        default -> throw new AssertionError(method);
                    }
                }
        );
    }

    @Test
    public void testConnectionFactoryBeginAndJdbcTransaction() throws Exception {
        var calls = new ArrayList<String>();
        var con = connection(calls);
        var opened = new AtomicInteger();
        ConnectionFactory factory = () -> {
            opened.incrementAndGet();
            return con;
        };

        var transaction = factory.begin("tx-1");

        Assertions.assertSame(con, transaction.unwrap());
        Assertions.assertEquals("tx-1", transaction.id());
        Assertions.assertEquals(1, opened.get());

        transaction.commit();
        transaction.rollback();
        transaction.close();
        Assertions.assertEquals(List.of("commit", "rollback", "close"), calls);
    }

    @Test
    @SuppressWarnings("null")
    public void testConnectionFactoryAndTransactionNulls() {
        var calls = new ArrayList<String>();
        var con = connection(calls);
        ConnectionFactory factory = () -> con;

        ForTests.testNull("id", () -> factory.begin(null));
    }

    @Test
    public void testJdbcAnnotimplerCreatesProxy() throws Exception {
        ConnectionFactory factory = () -> {
            throw new AssertionError("Connection must not be opened while creating the proxy.");
        };

        var dao = JdbcAnnotimpler.create(factory, EmptyDao.class);

        Assertions.assertNotNull(dao);
        Assertions.assertTrue(dao instanceof EmptyDao);
        Assertions.assertEquals(dao, dao);
        Assertions.assertTrue(dao.toString().contains(EmptyDao.class.getName()));
    }

    @Test
    @SuppressWarnings("null")
    public void testJdbcAnnotimplerNullsAndInvalidType() throws Exception {
        ConnectionFactory factory = () -> {
            throw new AssertionError();
        };

        ForTests.testNull("factory", () -> JdbcAnnotimpler.create(null, EmptyDao.class));
        ForTests.testNull("iface", () -> JdbcAnnotimpler.create(factory, null));
        var ctor = JdbcAnnotimpler.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        var invocation = Assertions.assertThrows(
                InvocationTargetException.class,
                ctor::newInstance
        );
        Assertions.assertInstanceOf(UnsupportedOperationException.class, invocation.getCause());
        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> JdbcAnnotimpler.create(factory, String.class)
        );
    }

    @Test
    public void testJdbcAnnotimplerRejectsMalformedDao() {
        interface BadDao {
            @QuerySql
            String value();
        }

        var ex = Assertions.assertThrows(
                BadImplementationException.class,
                () -> JdbcAnnotimpler.create(() -> {
                    throw new AssertionError();
                }, BadDao.class)
        );
        Assertions.assertNotNull(ex);
    }

    @Test
    public void testJdbcAnnotimplerWrapsPropertyFailure() {
        interface RefusingDao {
            @RefusesProperties
            String value();
        }

        var ex = Assertions.assertThrows(
                BadImplementationException.class,
                () -> JdbcAnnotimpler.create(() -> {
                    throw new AssertionError();
                }, RefusingDao.class)
        );
        Assertions.assertEquals("The implementation refused the default properties.", ex.getMessage());
        Assertions.assertInstanceOf(PropertyBag.PropertyNotFoundException.class, ex.getCause());
    }
}

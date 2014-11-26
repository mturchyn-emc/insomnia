package mturchyn.blackwater.file.keyvalue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class KeyValueRepositoryTest {

    public static final String DIR = "c:\\Users\\Maksim\\monica\\data";
    public static final String INDEX_EXT = "bwi";
    public static final String VALUES_EXT = "bwv";
    public static final String REPO_NAME = "test";
    public static final int THREAD_POOL_SIZE = 70;
    private KeyValueRepository<String, Car> repository;
    private ExecutorService threadPool;

    @Before
    public void setUp() throws Exception {
        repository = new FileKeyValueRepository<>(DIR, REPO_NAME); // todo
        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Test
    public void testBasicOperationsForOneObject() throws Exception {
        Assert.assertFalse(repository.contains("1"));
        Assert.assertNull(repository.read("1"));

        final Car savedCar1 = Car.of("brand1", "model1");
        Car putCar1 = repository.put("1", savedCar1);
        Assert.assertEquals(savedCar1, putCar1);

        Assert.assertTrue(repository.contains("1"));

        Car readCar1 = repository.read("1");
        Assert.assertEquals(savedCar1, readCar1);

        Car removedCar1 = repository.remove("1");
        Assert.assertEquals(savedCar1, removedCar1);
        Assert.assertFalse(repository.contains("1"));
        Assert.assertNull(repository.read("1"));
    }

    @Test
    public void testPutAndReadAndContainsOperations() throws Exception {
        Car savedCar1 = Car.of("brand1", "model1");
        Car savedCar2 = Car.of("brand2", "model2");
        Car savedCar3 = Car.of("brand3", "model3");
        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);

        Assert.assertTrue(repository.contains("1"));
        Assert.assertTrue(repository.contains("2"));
        Assert.assertTrue(repository.contains("3"));

        Assert.assertEquals(savedCar1, repository.read("1"));
        Assert.assertEquals(savedCar2, repository.read("2"));
        Assert.assertEquals(savedCar3, repository.read("3"));
    }

    @Test
    public void testPutAndDeleteOperations() throws Exception {
        Car savedCar1 = Car.of("brand1", "model1");
        Car savedCar2 = Car.of("brand2", "model2");
        Car savedCar3 = Car.of("brand3", "model3");
        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);

        repository.remove("1");
        repository.remove("2");
        repository.remove("3");

        Assert.assertFalse(repository.contains("1"));
        Assert.assertFalse(repository.contains("2"));
        Assert.assertFalse(repository.contains("3"));

        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);

        Assert.assertTrue(repository.contains("1"));
        Assert.assertTrue(repository.contains("2"));
        Assert.assertTrue(repository.contains("3"));

        repository.remove("1");
        repository.remove("2");
        repository.remove("3");

        Assert.assertFalse(repository.contains("1"));
        Assert.assertFalse(repository.contains("2"));
        Assert.assertFalse(repository.contains("3"));
    }

    @Test
    public void testPartitionDeleteOperations() throws Exception {
        Car savedCar1 = Car.of("brand1", "model1");
        Car savedCar2 = Car.of("brand2", "model2");
        Car savedCar3 = Car.of("brand3", "model3");
        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);

        repository.remove("1");
        Assert.assertFalse(repository.contains("1"));
        Assert.assertTrue(repository.contains("2"));
        Assert.assertTrue(repository.contains("3"));
        repository.put("1", savedCar1);
        Assert.assertTrue(repository.contains("1"));
        Assert.assertEquals(savedCar1, repository.read("1"));
        repository.remove("2");
        repository.remove("3");
        Assert.assertTrue(repository.contains("1"));
        Assert.assertFalse(repository.contains("2"));
        Assert.assertFalse(repository.contains("3"));

    }

    @Test
    public void testPutAndClearOperations() throws Exception {
        Car savedCar1 = Car.of("brand1", "model1");
        Car savedCar2 = Car.of("brand2", "model2");
        Car savedCar3 = Car.of("brand3", "model3");
        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);

        repository.clear();
        Assert.assertFalse(repository.contains("1"));
        Assert.assertFalse(repository.contains("2"));
        Assert.assertFalse(repository.contains("3"));

        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);

        Assert.assertTrue(repository.contains("1"));
        Assert.assertTrue(repository.contains("2"));
        Assert.assertTrue(repository.contains("3"));

        repository.clear();
        Assert.assertFalse(repository.contains("1"));
        Assert.assertFalse(repository.contains("2"));
        Assert.assertFalse(repository.contains("3"));
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        Car savedCar1 = Car.of("brand1", "model1");
        Car savedCar2 = Car.of("brand2", "model2");
        Car savedCar3 = Car.of("brand3", "model3");
        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);
        repository.save();
        repository.close();

        repository = new FileKeyValueRepository<>(DIR, REPO_NAME); // todo

        Assert.assertTrue(repository.contains("1"));
        Assert.assertTrue(repository.contains("2"));
        Assert.assertTrue(repository.contains("3"));

        Assert.assertEquals(savedCar1, repository.read("1"));
        Assert.assertEquals(savedCar2, repository.read("2"));
        Assert.assertEquals(savedCar3, repository.read("3"));

        repository.clear();
        repository.save();
        repository.close();

        repository = new FileKeyValueRepository<>(DIR, REPO_NAME); // todo

        Assert.assertFalse(repository.contains("1"));
        Assert.assertFalse(repository.contains("2"));
        Assert.assertFalse(repository.contains("3"));
    }

    @Test
    public void testConcurrentRead() throws Exception {
        int n = 100;
        for (long i = 1; i <= n; i++) {
            String id = String.valueOf(i);
            repository.put(id, Car.of("brand" + id, "model" + id));
        }
        List<Future<Pair<Long, Car>>> futures = new ArrayList<>(n);
        for (long i = 1; i < THREAD_POOL_SIZE * 10; i++) {
            final long id = i % n + 1;
            Future<Pair<Long, Car>> future = threadPool.submit(() -> {
                Pair<Long, Car> pair = new Pair<>(id, repository.read(String.valueOf(id)));
                return pair;
            });
            futures.add(future);
        }

        for (Future<Pair<Long, Car>> future : futures) {
            Pair<Long, Car> longCarPair = future.get();
            Assert.assertEquals("brand" + longCarPair.getT(), longCarPair.getV().brand);
            Assert.assertEquals("model" + longCarPair.getT(), longCarPair.getV().model);
        }
    }

    @Test
    public void testConcurrentWrite() throws Exception {
        int n = 100;

        List<Future<Object>> futures = new ArrayList<>(n);
        for (long i = 1; i <= n; i++) {
            final long id = i;
            Future<Object> future = threadPool.submit(() -> {
                repository.put(String.valueOf(id), Car.of("brand" + id, "model" + id));
                return new Object();
            });
            futures.add(future);
        }

        for (Future<Object> future : futures) {
            future.get();
        }
        for (long i = 1; i <= n; i++) {
            Car car = repository.read(String.valueOf(i));
            Assert.assertEquals("brand" + i, car.brand);
            Assert.assertEquals("model" + i, car.model);
        }
    }

    @Test
    public void testConcurrentReadAndWrite() throws Exception {
        Car savedCar1 = Car.of("brand1", "model1");
        Car savedCar2 = Car.of("brand2", "model2");
        Car savedCar3 = Car.of("brand3", "model3");
        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);

        int n = 100;

        List<Future<Object>> futures = new ArrayList<>(n);
        for (long i = 4; i <= n; i++) {
            final long id = i;
            Future<Object> future = threadPool.submit(() -> {
                repository.put(String.valueOf(id), Car.of("brand" + id, "model" + id));
                return new Object();
            });
            futures.add(future);
            long id2 = i % 3 + 1;
            Car car = repository.read(String.valueOf(id2));
            Assert.assertEquals("brand" + id2, car.brand);
            Assert.assertEquals("model" + id2, car.model);
        }

        for (Future<Object> future : futures) {
            future.get();
        }

    }

    @Test
    public void testSaveAndLoadForBigN() throws Exception {
        Car savedCar1 = Car.of("brand1", "model1");
        Car savedCar2 = Car.of("brand2", "model2");
        Car savedCar3 = Car.of("brand3", "model3");
        repository.put("1", savedCar1);
        repository.put("2", savedCar2);
        repository.put("3", savedCar3);
        repository.save();
        repository.close();

        for (int i = 4; i < 100; i++) {
            repository = new FileKeyValueRepository<>(DIR, REPO_NAME); // todo
            String id = String.valueOf(i);
            String previousId = String.valueOf(i - 1);
            Assert.assertTrue(repository.contains(previousId));
            Assert.assertEquals(Car.of("brand" + previousId, "model" + previousId), repository.read(previousId));
            repository.put(id, Car.of("brand" + id, "model" + id));
            repository.save();
            repository.close();
        }
        repository = new FileKeyValueRepository<>(DIR, REPO_NAME);
    }

    // Tear down methods ////////////////////////////////////////////////////////////////////////////////////

    @After
    public void tearDown() throws Exception {
        threadPool.shutdown();
        repository.clear();
        repository.close();
        File indexFile = new File(DIR, REPO_NAME + "." + INDEX_EXT);
        deleteFile(indexFile);
        File valuesFile = new File(DIR, REPO_NAME + "." + VALUES_EXT);
        deleteFile(valuesFile);
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            if (!file.delete()) {
                System.out.println("Cannot delete file " + file.getAbsolutePath());

            }
        }
    }

    private static class Car implements Serializable {
        String brand;
        String model;

        public static Car of(String brand, String model) {
            Car car = new Car();
            car.brand = brand;
            car.model = model;
            return car;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Car car = (Car) o;

            if (!brand.equals(car.brand)) return false;
            if (!model.equals(car.model)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = brand.hashCode();
            result = 31 * result + model.hashCode();
            return result;
        }
    }

    private static class Pair<T, V> {
        private final T t;
        private final V v;

        private Pair(T t, V v) {
            this.t = t;
            this.v = v;
        }

        public T getT() {
            return t;
        }

        public V getV() {
            return v;
        }
    }




}

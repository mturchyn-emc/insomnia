package mturchyn.blackwater.integration;

import mturchyn.blackwater.core.document.Document;
import mturchyn.blackwater.core.document.Field;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class IntegrationTestSuite {

    private ObjectMapper jsonMapper = new ObjectMapper();
    private NoteDao noteDao;
    private EnglishWords englishWords = EnglishWords.getInstance();

    private ExecutorService threadPool;
    private int threadPoolSize;
    private int recordsToPut;
    private int requestsToRead;

    private CloseableHttpClient httpClient;


    public IntegrationTestSuite(NoteDao noteDao, int threadPoolSize, int recordsToPut, int requestsToRead) {
        this.noteDao = noteDao;
        this.threadPoolSize = threadPoolSize;
        this.recordsToPut = recordsToPut;
        this.requestsToRead = requestsToRead;
        System.out.println(String.format("Pool size: %d, records to put: %d, requests to read: %d",
          threadPoolSize, recordsToPut, requestsToRead));
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        httpClient = HttpClients.createDefault();
    }


    public void resetIndex() {
        try {
            HttpDelete resetIndexRequest = new HttpDelete(Constants.APP_BASE_URL + "/api/documents/note");

            try (CloseableHttpResponse response = httpClient.execute(resetIndexRequest)) {
                handleResponse(response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putNotes() throws IOException {
        List<Note> notes = noteDao.readAllNotes();
        if (recordsToPut >= 0 && recordsToPut < notes.size()) {
            notes = notes.subList(0, recordsToPut);
        }

        for (int i = 0; i < notes.size(); i += 1000) {
            int rightBound;
            if (i + 1000 < notes.size()) {
                rightBound = i + 1000;
            } else {
                rightBound = notes.size() - 1;
            }

            System.out.println(String.format("Putting next portion: [{%d}, {%d}]", i, rightBound));
            sendNotes(notes.subList(i, rightBound));
        }

    }

    private void sendNotes(List<Note> notes) throws IOException {
        List<Document> notesAsDocument = convertNotesToNoteDocuments(notes);

        String notesAsJson = jsonMapper.writeValueAsString(notesAsDocument);
        try {
            HttpPut postRequest = new HttpPut(Constants.APP_BASE_URL + "/api/documents/note");

            StringEntity input = new StringEntity(notesAsJson);
            input.setContentType("application/json");
            postRequest.setEntity(input);

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                handleResponse(response);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Document> convertNotesToNoteDocuments(List<Note> notes) {
        return notes.stream().map((Note note) -> {
                Document document = new Document();
                document.setId(note.getId());
                document.setFields(Arrays.asList(
                  Field.from("ID", note.getId()),
                  Field.from("title", note.getTitle()),
                  Field.from("content", note.getContent())
                ));
                return document;

            }).collect(Collectors.toList());
    }

    private void handleResponse(HttpResponse response) throws IOException {
        if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
              + response.getStatusLine().getStatusCode());
        }

        try (InputStream inputStream = response.getEntity().getContent()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
        }

    }

    public void readNotes() throws ExecutionException, InterruptedException {

        List<Future<ReadRequestDescription>> futures = new ArrayList<>(requestsToRead);
        for (int i = 0; i < requestsToRead; i++) {
            System.out.println("Left " + (requestsToRead - i));
            Future<ReadRequestDescription> submit = threadPool.submit(new ReadTask());
            futures.add(submit);
        }
        int successes = 0;
        int failures = 0;
        double averageTime = -1;
        long minTime = 10000;
        long maxTime = -1;


        for (Future<ReadRequestDescription> result : futures) {
            ReadRequestDescription readResult = result.get();
            if (readResult.success) {
                successes++;
                averageTime += readResult.msInResponse;
                if (minTime > readResult.msInResponse) {
                    minTime = readResult.msInResponse;
                }
                if (maxTime < readResult.msInResponse) {
                    maxTime = readResult.msInResponse;
                }
            } else {
                failures++;
            }
        }
        if (successes > 0) {
            averageTime = averageTime / successes;
        }
        System.out.println(String.format(
          "Initial parameters:%n" +
            "Thread pool size: %d%n" +
            "Written documents: %d%n" +
            "Number of search requests: %d%n" +
            "Result: %n" +
            "successes: %d, %n" +
            "failures: %d %n" +
            "averageTime: %.3f ms%n," +
            "min time: %d ms%n" +
            "max time: %d ms",
          threadPoolSize, recordsToPut, requestsToRead,
          successes, failures, averageTime, minTime, maxTime));
        Assert.assertEquals(0, failures);
        Assert.assertTrue(averageTime < 10);

    }


    private class ReadTask implements Callable<ReadRequestDescription> {

        @Override
        public ReadRequestDescription call() throws Exception {
            ReadRequestDescription readResult = new ReadRequestDescription();
            try {

                String englishWork = englishWords.nextRandomWord();
                String uri = "/api/documents/note?query=" + englishWork;
                HttpGet getRequest = new HttpGet(Constants.APP_BASE_URL + uri);
                readResult.requestUri = uri;

                long start = System.currentTimeMillis();
                try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                    readResult.ms = System.currentTimeMillis() - start;


                    if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {
                        readResult.success = false;
                    } else {
                        readResult.success = true;
                        try (InputStream inputStream = response.getEntity().getContent()) {
                            JsonNode jsonNode = jsonMapper.readValue(inputStream, JsonNode.class);
                            readResult.msInResponse = jsonNode.get("takenTimeMs").asLong();
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
                readResult.success = false;
            }
            return readResult;
        }
    }

    public void close() {
        try {
            ConcurrentUtils.shutDownExecutorService(threadPool, 10);
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

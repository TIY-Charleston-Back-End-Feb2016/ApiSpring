package com.theironyard;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by zach on 3/31/16.
 */
@RestController
public class Controller {
    static final String SAMPLE_URL = "http://gturnquist-quoters.cfapps.io/api/random";

    @PostConstruct
    public void init() throws InterruptedException {
        // run a continuous background task
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("Hello!");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    @Async
    public Future<HashMap> getQuoteAsync() throws InterruptedException {
        RestTemplate query = new RestTemplate();
        HashMap result = query.getForObject(SAMPLE_URL, HashMap.class);
        String type = (String) result.get("type");
        if (type.equals("success")) {
            HashMap value = (HashMap) result.get("value");
            return new AsyncResult<HashMap>(value);
        }
        return new AsyncResult<HashMap>(null);
    }

    @RequestMapping(path = "/quote", method = RequestMethod.GET)
    public ArrayList getQuote() throws ExecutionException, InterruptedException {
        Future<HashMap> m1 = getQuoteAsync();
        Future<HashMap> m2 = getQuoteAsync();
        Future<HashMap> m3 = getQuoteAsync();

        while (!m1.isDone() || !m2.isDone() || !m3.isDone()) {
            Thread.sleep(10);
        }

        ArrayList arr = new ArrayList();
        arr.add(m1.get());
        arr.add(m2.get());
        arr.add(m3.get());
        return arr;
    }
}

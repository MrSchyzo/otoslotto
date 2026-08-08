package com.mrschyzo.hungarian.interfaces;

import com.mrschyzo.hungarian.application.service.ApplicationException;
import com.mrschyzo.hungarian.application.service.LotteryAppService;

import java.io.*;

public class IOLotteryController implements AutoCloseable {
    private BufferedReader in;
    private PrintWriter out;
    private LotteryAppService service;

    public IOLotteryController(InputStream in, OutputStream out, LotteryAppService service) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = new PrintWriter(out, true);
        this.service = service;
    }

    public void beginInteraction() {
        long start = System.nanoTime();
        boolean keepRunning = true;
        try {
            service.load();
        } catch (ApplicationException e) {
            out.println(String.format("Failed to load: %s", e));
            return;
        }
        out.println(String.format("READY (%d ns)", System.nanoTime() - start));
        while (keepRunning) {
            out.print("Name a winning draw (format \"n1 n2 n3 n4 n5\"): ");
            out.flush();
            try {
                var line = in.readLine().toLowerCase();
                if (line.contains("exit")) {
                    keepRunning = false;
                    continue;
                }
                var startQuery = System.nanoTime();
                var result = service.query(line);
                out.println(
                        String.format(
                                "[%f ms] 2-matches: %d; 3-matches: %d, 4-matches: %d, 5-matches: %d",
                                Double.valueOf(System.nanoTime() - startQuery) / 1_000_000.0,
                                result.get(2),
                                result.get(3),
                                result.get(4),
                                result.get(5)
                        )
                );
            } catch (Exception e) {
                out.println(e);
            }
        }
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (Exception _) {
        }
        try {
            out.close();
        } catch (Exception _) {
        }
    }
}

package com.etema.ragnarmmo.common.api.jobs;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class RagnarJobsAPI {
    private static final Consumer<JobType> DEFAULT_CHANGE_REQUEST = job -> {
    };
    private static final AtomicReference<Consumer<JobType>> CHANGE_REQUEST =
            new AtomicReference<>(DEFAULT_CHANGE_REQUEST);

    private RagnarJobsAPI() {
    }

    public static void registerChangeJobRequest(Consumer<JobType> request) {
        CHANGE_REQUEST.set(request != null ? request : DEFAULT_CHANGE_REQUEST);
    }

    public static void requestChangeJob(JobType job) {
        if (job != null) {
            CHANGE_REQUEST.get().accept(job);
        }
    }

    public static boolean hasChangeJobRequest() {
        return CHANGE_REQUEST.get() != DEFAULT_CHANGE_REQUEST;
    }
}

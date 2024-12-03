package org.bih.aft.service.beam;

import java.util.List;
import java.util.Map;

record Result(
        String from,
        List<String> to,
        String task,
        Status status,
        String body,
        Map metadata
) {
    static Result fromTask(Task task, String from, Status status, String body) {
        return new Result(from, List.of(task.from()), task.id(), status, body, Map.of());
    }
}

enum Status {
    claimed,
    tempfailed,
    permfailed,
    succeeded,
}

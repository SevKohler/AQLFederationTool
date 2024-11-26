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
) { }

enum Status {
    claimed,
    tempfailed,
    permfailed,
    succeeded,
}

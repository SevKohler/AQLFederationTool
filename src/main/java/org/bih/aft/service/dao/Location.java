package org.bih.aft.service.dao;

public record Location(String name, String url) {
    public String localQueryEndpoint() {
        return url + "/query/local";
    }
}

package org.obscura.backend.service;

import org.obscura.backend.exception.GameNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class StreamScoutService {

    private final RestClient SSRestClient;

    public StreamScoutService(RestClient SSRestClient) {
        this.SSRestClient = SSRestClient;
    }
}
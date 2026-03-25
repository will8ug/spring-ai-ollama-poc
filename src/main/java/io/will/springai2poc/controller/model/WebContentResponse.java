package io.will.springai2poc.controller.model;

import java.util.List;

public record WebContentResponse(String url, List<String> contents) {
}

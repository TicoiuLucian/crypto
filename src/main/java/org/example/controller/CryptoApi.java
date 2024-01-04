package org.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.tuple.Pair;
import org.example.exception.ErrorMessage;
import org.example.exception.InvalidDateException;
import org.example.model.Crypto;
import org.example.model.Symbol;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CryptoApi {


    @Operation(summary = "Get a descending sorted list of all the cryptos,comparing the normalized range (i.e. (max-min)/min)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject("[{ \"timestamp\": \"2022-01-01T10:00:00\",\"symbol\":\"BTC\",\"price\":2615.75}," +
                                    "{ \"timestamp\": \"2023-08-02T10:00:00\",\"symbol\":\"ETH\",\"price\":4522.26}]"))}),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class), examples = @ExampleObject("message: Too many requests/Only 1 request per 20 seconds allowed")))})
    @GetMapping("/sorted-cryptos/normalized-range")
    ResponseEntity<List<Crypto>> getSortedCryptosByNormalizedRange();

    @Operation(summary = "Get a list of statistics(oldest/newest/min/max) for a specific crypto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject("[{ \"timestamp\": \"2022-01-01T10:00:00\",\"symbol\":\"BTC\",\"price\":2615.75}," +
                                    "{ \"timestamp\": \"2023-08-02T10:00:00\",\"symbol\":\"ETH\",\"price\":4522.26}]"))}),
            @ApiResponse(responseCode = "400", description = "Invalid month supplied", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class), examples = @ExampleObject("message: Too many requests/Only 1 request per 20 seconds allowed")))})
    @GetMapping("/statistics/{crypto}")
    ResponseEntity<List<Pair<String, Crypto>>> getStatisticsForCrypto(@Parameter(description = "Crypto to filter by") @PathVariable Symbol crypto);

    @Operation(summary = "Get a list of statistics(oldest/newest/min/max) for each crypto for the a specific month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject("[{ \"timestamp\": \"2022-01-01T10:00:00\",\"symbol\":\"BTC\",\"price\":2615.75}," +
                                    "{ \"timestamp\": \"2023-08-02T10:00:00\",\"symbol\":\"ETH\",\"price\":4522.26}]"))}),
            @ApiResponse(responseCode = "400", description = "Invalid year/month supplied", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class), examples = @ExampleObject("message: Too many requests/Only 1 request per 20 seconds allowed")))})
    @GetMapping("/statistics/year/{year}/month/{month}")
    ResponseEntity<Map<String, List<Crypto>>> getStatisticsForMonthAndYear(@Parameter(description = "Year") @PathVariable int year, @Parameter(description = "Month") @PathVariable int month) throws InvalidDateException;

    @Operation(summary = "Get highest normalized range for a specific day")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "highest normalized crypto returned",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Crypto.class),
                            examples = @ExampleObject("{ \"timestamp\": \"2022-01-01T10:00:00\",\"symbol\":\"BTC\",\"price\":2615.75}"))}),
            @ApiResponse(responseCode = "400", description = "Invalid date supplied", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class), examples = @ExampleObject("message: Too many requests/Only 1 request per 20 seconds allowed")))})
    @PostMapping("/highest-normalized-range/")
    ResponseEntity<Crypto> getHighestNormalizedRange(@Parameter(description = "LocalDate used to filter") @RequestBody LocalDate date);

}
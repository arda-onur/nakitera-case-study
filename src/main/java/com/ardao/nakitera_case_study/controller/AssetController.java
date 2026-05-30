package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.response.AssetListResponse;
import com.ardao.nakitera_case_study.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset")
@Tag(name = "Asset", description = "Asset listing endpoints")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "List assets", description = "Returns assets held by a customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assets returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @GetMapping("/list")
    public ResponseEntity<Page<AssetListResponse>> getCustomerList(@Parameter(description = "Page number", example = "0")
                                                                       @RequestParam(defaultValue = "0")
                                                                       @PositiveOrZero int page,
                                                                   @Parameter(description = "Page size", example = "10")
                                                                       @RequestParam(defaultValue = "10")
                                                                       @Positive int size,
                                                                   @Parameter(description = "Customer id", example = "1")
                                                                       @RequestParam(required = false)
                                                                       Long customerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.assetService.getCustomerAssetList(page,size,customerId));
    }
}

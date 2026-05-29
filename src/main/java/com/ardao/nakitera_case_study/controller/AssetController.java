package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.response.AssetListResponse;
import com.ardao.nakitera_case_study.service.AssetService;
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
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }


    @GetMapping("/list")
    public ResponseEntity<Page<AssetListResponse>> getCustomerList(@RequestParam(defaultValue = "0")
                                                                   @PositiveOrZero
                                                                   int page,
                                                                   @RequestParam(defaultValue = "10")
                                                                   @Positive
                                                                   int size,
                                                                   @RequestParam(required = false)
                                                                   Long customerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.assetService.getCustomerAssetList(page,size,customerId));
    }
}

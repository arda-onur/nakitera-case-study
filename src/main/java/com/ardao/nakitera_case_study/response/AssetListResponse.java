package com.ardao.nakitera_case_study.response;

public record AssetListResponse(long id,
                                 String assetName,
                                 int size,
                                 int usableSize) {
}

package com.ardao.nakitera_case_study.util.mapper.asset;

import com.ardao.nakitera_case_study.entity.Asset;
import com.ardao.nakitera_case_study.response.AssetListResponse;

public final class AssetMapper {

    public static AssetListResponse toAssetListResponse(Asset asset) {
    return new AssetListResponse(
            asset.getId(),
            asset.getAssetName(),
            asset.getSize(),
            asset.getUsableSize()
    );
}
    private AssetMapper() {

    }
}

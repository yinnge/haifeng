package com.haifeng.app.service.favorite;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.favorite.FavoriteAddDTO;
import com.haifeng.app.dto.favorite.FavoriteQueryDTO;
import com.haifeng.app.dto.favorite.FavoriteStatusQueryDTO;
import com.haifeng.app.vo.favorite.FavoriteCountsVO;
import com.haifeng.app.vo.favorite.FavoriteItemVO;

import java.util.List;

public interface FavoriteService {

    void add(FavoriteAddDTO dto);

    void remove(String targetType, Long targetId);

    IPage<FavoriteItemVO> page(FavoriteQueryDTO dto);

    FavoriteCountsVO counts();

    List<Long> favoritedIds(FavoriteStatusQueryDTO dto);
}

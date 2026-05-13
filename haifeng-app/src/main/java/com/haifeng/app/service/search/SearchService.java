package com.haifeng.app.service.search;

import com.haifeng.app.vo.search.SearchResultVO;

import java.util.List;

public interface SearchService {

    List<SearchResultVO> searchUniversity(String keyword, Integer limit);

    List<SearchResultVO> searchCity(String keyword, Integer limit);

    List<SearchResultVO> searchMajor(String keyword, Integer limit);
}

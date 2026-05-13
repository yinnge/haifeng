package com.haifeng.app.controller.search;

import com.haifeng.app.service.search.SearchService;
import com.haifeng.app.vo.search.SearchResultVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/app/search")
@RequiredArgsConstructor
@RequireLogin
public class SearchController {

    private final SearchService searchService;

    /**
     * 搜索大学（用于个人资料填写）
     */
    @GetMapping("/university")
    public R<List<SearchResultVO>> searchUniversity(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        return R.ok(searchService.searchUniversity(keyword, limit));
    }

    /**
     * 搜索城市（用于个人资料填写）
     */
    @GetMapping("/city")
    public R<List<SearchResultVO>> searchCity(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        return R.ok(searchService.searchCity(keyword, limit));
    }

    /**
     * 搜索专业（用于个人资料填写）
     */
    @GetMapping("/major")
    public R<List<SearchResultVO>> searchMajor(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        return R.ok(searchService.searchMajor(keyword, limit));
    }
}

package com.mall.search.service;

import com.mall.search.vo.SearchParamVO;
import com.mall.search.vo.SearchResult;

public interface MallSearchService {

    SearchResult searchByParam(SearchParamVO searchParam);
}

package com.mall.search.index;

import com.mall.search.service.MallSearchService;
import com.mall.search.vo.SearchParamVO;
import com.mall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class IndexController {

    @Resource
    private MallSearchService mallSearchService;

    /**
     * 首页静态资源
     */
    @GetMapping({"/", "/list.html"})
    public String index(SearchParamVO searchParam, Model model, HttpSession session) {
        SearchResult searchRes = mallSearchService.searchByParam(searchParam);
        model.addAttribute("result", searchRes);
        return "list";
    }
}

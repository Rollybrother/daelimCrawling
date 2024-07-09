package com.daelim.crawling.mainProgram.crawlingHistory;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import com.daelim.crawling.Daelim.CrawlingHistoryVO;

@Controller
@RequestMapping("/ajax")
public class CrawlingHistoryController {

    @Autowired
    private CrawlingHistoryService crawlingHistoryService;

    @GetMapping("/searchByDate")
    @ResponseBody
    public ArrayList<CrawlingHistoryVO> searchByDate(@RequestParam(name = "date") String date) {
        return crawlingHistoryService.searchByDate(date);
    }
    
}

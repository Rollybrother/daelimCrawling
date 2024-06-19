package com.daelim.crawling.mainProgram;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.daelim.crawling.Daelim.DaelimRepository;
import com.daelim.crawling.Daelim.DaelimVO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/ajax")
@RequiredArgsConstructor
public class AjaxController {
	private final DaelimRepository daelimRepository;
    @PostMapping("/addProduct")
    public DaelimVO addProduct(@RequestBody DaelimVO product) {
        // 이 자리에 insert 함수를 만들고 싶어
    	try {
    		daelimRepository.insertForDbCrawling(product.getName(), product.getPrice(), product.getSearchLimit(),
    				product.getCompetitor1Product(),product.getCompetitor1Name(),product.getCompetitor2Product(),product.getCompetitor2Name());
		} catch (Exception e) {
			System.out.println(e);
		}
    	
    	DaelimVO result 
    	= new DaelimVO(daelimRepository.findMaxIndex(),product.getName(), product.getPrice(), product.getSearchLimit(),
    			product.getCompetitor1Product(),product.getCompetitor1Name(),product.getCompetitor2Product(),product.getCompetitor2Name());
        return result;
    }
    
    @DeleteMapping("/deleteProducts")
    public void deleteProducts(@RequestBody List<Integer> indices) {
        try {
            daelimRepository.deleteByIndices(indices);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    @PostMapping("/updateAutoSearchStatus")
    public String updateAutoSearchStatus(@RequestParam("isEnabled") boolean isEnabled, @RequestParam("intervalDays") int intervalDays) {
        // 여기서 자동감시 상태를 저장하거나 필요에 따라 처리합니다.
        // 단순히 상태를 반환하도록 구현합니다.
        return isEnabled ? "자동감시 중 : " + intervalDays + "일주기" : "";
    }
}

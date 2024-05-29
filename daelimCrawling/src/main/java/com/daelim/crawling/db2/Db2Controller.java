package com.daelim.crawling.db2;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.daelim.crawling.Daelim.DaelimVO;

import lombok.RequiredArgsConstructor;

@ComponentScan
@RequestMapping("/db2")
@RequiredArgsConstructor
@Controller
public class Db2Controller {
	static Logger logger;
	
	private final Db2Repository db2Repository;
	
	@GetMapping("/list")
	public String list(Model model) {
    	List<Db2VO> list = this.db2Repository.findAllFromDbTest();
        model.addAttribute("list", list);	
        return "db2";
    }
}

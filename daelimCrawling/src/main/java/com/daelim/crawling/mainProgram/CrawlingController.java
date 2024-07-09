package com.daelim.crawling.mainProgram;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.daelim.crawling.Daelim.DaelimRepository;
import com.daelim.crawling.Daelim.DaelimVO;
import com.daelim.crawling.mainProgram.competitor.CompetitorDto;
import com.daelim.crawling.mainProgram.competitor.CompetitorService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@ComponentScan
@RequestMapping("/crawling")
@RequiredArgsConstructor
@Controller
public class CrawlingController {
	private final CrawlingService crawlingService;
    private final DaelimRepository daelimRepository;
	private CompetitorService competitorService;

    @GetMapping("/beforeSearch")
    public String beforeList(Model model) {
        ArrayList<DaelimVO> list = this.daelimRepository.findAllFromDbCrawling();
        model.addAttribute("list", list);
        
        ArrayList<CrawlingDto> result = new ArrayList<>();  // 기본적으로 빈 리스트를 사용하거나 필요 시 기본 데이터를 추가
        model.addAttribute("listAfter", result); 
        return "splitView"; 
    }
    
    @PostMapping("/searchChecked")
    public String searchChecked(@RequestParam("list.index[]") ArrayList<Integer> indices,
                                @RequestParam("list.name[]") ArrayList<String> names,
                                @RequestParam("list.price[]") ArrayList<Integer> prices,
                                @RequestParam("list.searchLimit[]") ArrayList<Integer> searchLimits,
                                @RequestParam("list.competitor1Product[]") ArrayList<String> competitor1Products,
                                @RequestParam("list.competitor1Name[]") ArrayList<String> competitor1Names,
                                @RequestParam("list.competitor2Product[]") ArrayList<String> competitor2Products,
                                @RequestParam("list.competitor2Name[]") ArrayList<String> competitor2Names,
                                @RequestParam("searchType") String searchType,
                                @RequestParam("autoSearchEnabled") boolean autoSearchEnabled,
                                @RequestParam("autoSearchInterval") int autoSearchInterval,
                                Model model) throws MessagingException, IOException {
    							
        ArrayList<DaelimVO> list = new ArrayList<>();
        
        for (int i = 0; i < indices.size(); i++) {
            DaelimVO item = new DaelimVO(indices.get(i), names.get(i), prices.get(i), searchLimits.get(i));
            if(i<competitor1Products.size()) {
            	item.setCompetitor1Product(competitor1Products.get(i));
            	item.setCompetitor1Name(competitor1Names.get(i));
            }
            if(i<competitor2Products.size()) {
            	item.setCompetitor2Product(competitor2Products.get(i));
            	item.setCompetitor2Name(competitor2Names.get(i));
            }
            list.add(item);
        }
        ArrayList<CrawlingDto> searchBeforeResult = crawlingService.searchMany(list, searchType);
        // 경쟁자 데이터 계산
        competitorService = new CompetitorService(list, searchType);
        ArrayList<CompetitorDto> competitorResult = competitorService.finalComputation();
        	
        ArrayList<CrawlingDto> searchFinalResult = new ArrayList<>();
        
        for (DaelimVO e : list) {
            int limitPrice = e.getPrice();
            for (CrawlingDto e2 : searchBeforeResult) {
                if (e2.getName().contains(e.getName()) && e2.getPrice() < e.getPrice()) {
                    e2.setOK(false);
                    searchFinalResult.add(e2);
                } else {
                    searchFinalResult.add(e2);
                }
            }
        }
        ArrayList<DaelimVO> originalList = this.daelimRepository.findAllFromDbCrawling();
        ArrayList<percentDto> percentArray = analizePercent(searchFinalResult);
        // searchFinalResult를 price 기준으로 오름차순 정렬
        searchFinalResult.sort(Comparator.comparingInt(CrawlingDto::getPrice));
        
        // 이메일 발송
        sendEmail(searchFinalResult, percentArray,competitorResult);
        
        model.addAttribute("list", originalList);
        model.addAttribute("listAfter", searchFinalResult);
        model.addAttribute("percentArray", percentArray);
        model.addAttribute("autoSearchEnabled", autoSearchEnabled);
        model.addAttribute("autoSearchInterval", autoSearchInterval);
        model.addAttribute("indices", indices);
        model.addAttribute("searchType", searchType); 
        model.addAttribute("competitorResult", competitorResult); 
        return "splitView";
    }
    
    
    @GetMapping("/afterSearch")
    public String afterList(@RequestParam("productCode") String productCode, 
                            @RequestParam("searchType") String searchType, Model model) {
        ArrayList<CrawlingDto> result = crawlingService.search(productCode, searchType);
        ArrayList<DaelimVO> originalList = this.daelimRepository.findAllFromDbCrawling();
        model.addAttribute("list", originalList); 
        model.addAttribute("listAfter", result); 
        return "splitView"; 
    }

    private void sendEmail(ArrayList<CrawlingDto> target, ArrayList<percentDto> percentArray, 
            ArrayList<CompetitorDto> competitorArray) throws MessagingException, IOException {

        ArrayList<CrawlingDto> sendMail = new ArrayList<>();
        for (CrawlingDto e : target) {
            if (!e.isOK()) {
                sendMail.add(e);
            }
        }
        
        if (!sendMail.isEmpty()) {
            StringBuilder emailBody = new StringBuilder("최저가 한도 초과 제품 발생:<br><br>");
            int count = 1;
            for (CrawlingDto dto : sendMail) {
                 emailBody.append(count).append(". <a href=\"")
                         .append(dto.getLink())
                         .append("\">")
                         .append(dto.getName())
                         .append("</a> : ")
                         .append(dto.getPrice())
                         .append(" 원<br>");
                 count++;
            }

            // percentArray 테이블 추가
            emailBody.append("<br><br><table border='1'><tr><th>쇼핑몰 이름</th><th>위반 비율</th></tr>");
            for (percentDto percent : percentArray) {
                emailBody.append("<tr>")
                         .append("<td>").append(percent.getShoppingMall()).append("</td>")
                         .append("<td>").append(percent.getPercent()).append("% (")
                         .append(percent.getDenominator()).append("건 중 ")
                         .append(percent.getNumerator()).append("건)").append("</td>")
                         .append("</tr>");
            }
            emailBody.append("</table>");

            // competitorArray 테이블 추가
            emailBody.append("<br><br><table border='1'><tr><th>(경쟁제품, 쇼핑몰)</th><th>평균가격</th></tr>");
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
            for (CompetitorDto competitor : competitorArray) {
                String formattedPrice = numberFormat.format(competitor.getAveragePrice()) + "원";
                emailBody.append("<tr>")
                         .append("<td>(").append(competitor.getProductName()).append(", ")
                         .append(competitor.getShoppingMall()).append(")</td>")
                         .append("<td>").append(formattedPrice).append("</td>")
                         .append("</tr>");
            }
            emailBody.append("</table>");

            // JavaMailSender 설정
            JavaMailSender mailSender = createJavaMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("jinsoo4735@naver.com"); // 네이버 smtp의 경우 보내는 사람의 이메일을 적어주어야 함
            
            // 이메일 설정
            helper.setTo("jsmoon@dltc.co.kr");
            helper.setSubject("최저가 한도 초과 제품 발생");
            
            // 이메일 본문 설정
            helper.setText(emailBody.toString(), true);

            // 이메일 전송
            mailSender.send(message);
        }
    }

    
    private ArrayList<percentDto> analizePercent(ArrayList<CrawlingDto> target){
		HashSet<String> set = new HashSet<>();
		for(CrawlingDto e : target) {
			set.add(e.getSearchFrom());
		}
		ArrayList<percentDto> result = new ArrayList<>();
		int cases = set.size();
		for(String e : set) {
			float falseCase = 0;
			float totalSize = 0;
			for(CrawlingDto e2 : target) {
				if(e2.getSearchFrom().equals(e)) {
					totalSize++;
					if(!e2.isOK()) {
						falseCase++;
					}
				}
			}
			int ratio = (int)((falseCase/totalSize)*100);
			result.add(new percentDto(e, ratio, falseCase, totalSize));
		}
       return result; 
    }
    
    
    private JavaMailSender createJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.naver.com");
        mailSender.setPort(465);

        mailSender.setUsername("jinsoo4735@naver.com"); // 네이버 이메일 사용자명
        mailSender.setPassword("rjsrnreogkrry"); // 네이버 이메일 비밀번호

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.enable", "true");
        return mailSender;
    }
    
}

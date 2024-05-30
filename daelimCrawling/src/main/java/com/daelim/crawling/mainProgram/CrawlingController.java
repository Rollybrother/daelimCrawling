package com.daelim.crawling.mainProgram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@ComponentScan
@RequestMapping("/crawling")
@RequiredArgsConstructor
@Controller
public class CrawlingController {
    CrawlingService crawlingService = new CrawlingService();
    private final DaelimRepository daelimRepository;

    @GetMapping("/beforeSearch")
    public String beforeList(Model model) {
        ArrayList<DaelimVO> list = this.daelimRepository.findAllFromDbCrawling();
        model.addAttribute("list", list);
        
        // 추가: `afterSearch`의 기본 데이터를 추가하여 모델에 추가
        ArrayList<CrawlingDto> result = new ArrayList<>();  // 기본적으로 빈 리스트를 사용하거나 필요 시 기본 데이터를 추가
        model.addAttribute("listAfter", result); 
        
        return "splitView"; 
    }
    
    @PostMapping("/searchChecked")
    public String searchChecked(@RequestParam("list.index[]") ArrayList<Integer> indices,
                                @RequestParam("list.name[]") ArrayList<String> names,
                                @RequestParam("list.price[]") ArrayList<Integer> prices,
                                @RequestParam("searchType") String searchType,
                                @RequestParam("autoSearchEnabled") boolean autoSearchEnabled,
                                @RequestParam("autoSearchInterval") int autoSearchInterval,
                                Model model) throws MessagingException, IOException {

        ArrayList<DaelimVO> list = new ArrayList<>();
        for (int i = 0; i < indices.size(); i++) {
            DaelimVO item = new DaelimVO(indices.get(i), names.get(i), prices.get(i));
            list.add(item);
        }
        ArrayList<CrawlingDto> searchBeforeResult = crawlingService.searchMany(list, searchType);
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

        // 이메일 발송
        sendEmail(searchFinalResult, percentArray);

        model.addAttribute("list", originalList);
        model.addAttribute("listAfter", searchFinalResult);
        model.addAttribute("percentArray", percentArray);
        model.addAttribute("autoSearchEnabled", autoSearchEnabled);
        model.addAttribute("autoSearchInterval", autoSearchInterval);
        model.addAttribute("indices", indices); 
        model.addAttribute("searchType", searchType); 
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

    private void sendEmail(ArrayList<CrawlingDto> target, ArrayList<percentDto> percentArray) throws MessagingException, IOException {
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

            // 차트 이미지 생성
            byte[] chartImage = ChartUtil.createChartImage(percentArray);

            // JavaMailSender 설정
            JavaMailSender mailSender = createJavaMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("jinsoo4735@naver.com"); // 네이버 smtp의 경우 보내는 사람의 이메일을 적어주어야 함
            
            // 이메일 설정
            helper.setTo("jsmoon@dltc.co.kr");
            helper.setSubject("최저가 한도 초과 제품 발생");
            
            // 이메일 본문에 이미지 포함
            String cid = ContentIdGenerator.getContentId();
            helper.setText(emailBody.toString() + "<br><img src='cid:" + cid + "'><br>", true);
            InputStreamSource imageSource = new ByteArrayResource(chartImage);
            helper.addInline(cid, imageSource, "image/png");

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
			result.add(new percentDto(e, ratio));
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

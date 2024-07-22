package com.daelim.crawling.mainProgram;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.context.annotation.ComponentScan;
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
import com.daelim.crawling.db2.BidetRepository;
import com.daelim.crawling.db2.BidetVO;
import com.daelim.crawling.db2.FaucetRepository;
import com.daelim.crawling.db2.FaucetVO;
import com.daelim.crawling.mainProgram.competitor.CompetitorDto;
import com.daelim.crawling.mainProgram.competitor.CompetitorService;
import com.daelim.crawling.mainProgram.sellerDetail.SellerDetailDto;
import com.daelim.crawling.mainProgram.sellerDetail.SellerDetailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

@ComponentScan
@RequestMapping("/crawling")
@RequiredArgsConstructor
@Controller
public class CrawlingController {
	
	private final CrawlingService crawlingService;
    private final DaelimRepository daelimRepository;
	private CompetitorService competitorService;
	private final SellerDetailService sellerDetailService;
	private final BidetRepository bidetRepository;
	private final FaucetRepository faucetRepository;
	
    @GetMapping("/beforeSearch")
    public String beforeList(Model model) {
        ArrayList<DaelimVO> list = this.daelimRepository.findAllFromDbCrawling();
        model.addAttribute("list", list);
        
    	// 비데 리스트와 수전 리스트를 전달
        ArrayList<BidetVO> bidetList = this.bidetRepository.findNameFromDbBidet();
        for(BidetVO e : bidetList) {
        	String name = e.getName().trim();
        	e.setName(name);
        }
        ArrayList<FaucetVO> faucetList = this.faucetRepository.findNameFromDbFaucet();
        for(FaucetVO e : faucetList) {
        	String name = e.getName().trim();
        	e.setName(name);
        }
        model.addAttribute("bidetList",bidetList);
        model.addAttribute("faucetList",faucetList);
        
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
    	
    	// 비데 리스트와 수전 리스트를 전달
        ArrayList<BidetVO> bidetList = this.bidetRepository.findNameFromDbBidet();
        for(BidetVO e : bidetList) {
        	String name = e.getName().trim();
        	e.setName(name);
        }
        ArrayList<FaucetVO> faucetList = this.faucetRepository.findNameFromDbFaucet();
        for(FaucetVO e : faucetList) {
        	String name = e.getName().trim();
        	e.setName(name);
        }
        model.addAttribute("bidetList",bidetList);
        model.addAttribute("faucetList",faucetList);
    	
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
        HashSet<CrawlingDto> tempSet = new HashSet();
        
        for (DaelimVO e : list) {
            int limitPrice = e.getPrice();
            for (CrawlingDto e2 : searchBeforeResult) {
                if (e2.getName().contains(e.getName()) && e2.getPrice() < e.getPrice()) {
                    e2.setOK(false);
                    // 이 지점에 판매자 정보를 넣어야 함
                    if(e2.getSearchFrom().equals("naver")) {
                    	SellerDetailDto temp = sellerDetailService.searchSellerNaver(e2.getLink());
                    	if(e2.getName().equals("도비도스 대림방수비데 IPX-5 DLB-310 24년형 신제품 노즐자동세척(설치비 별도)")) {
                    		System.out.println();
                    	}
                    	if(temp!=null) {
                    		if(temp.getSellerAddress()=="" &&
                    			temp.getSellerName()=="" &&
                    			temp.getSellerPhoneNumber()=="") {
                    			temp.setSellerName("일회성 팝업(오늘하루 보지않기)등으로");
                    			temp.setSellerAddress("정보 추출이 실패했습니다.");
                    			temp.setSellerPhoneNumber("직접 상품 링크를 클릭해주세요 죄송합니다");
                    		}
                    	}
                    	e2.setSellerDetailDto(temp);
                    }else if(e2.getSearchFrom().equals("coupang")) {
                    	SellerDetailDto temp = sellerDetailService.searchSellerCoupang(e2.getLink());
                    	e2.setSellerDetailDto(temp);
                    }
                    tempSet.add(e2);
                } else {
                	tempSet.add(e2);
                }
            }
        }
        for(CrawlingDto e : tempSet) {
        	searchFinalResult.add(e);
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
        
        // 비데 리스트와 수전 리스트를 전달
        ArrayList<BidetVO> bidetList = this.bidetRepository.findNameFromDbBidet();
        for(BidetVO e : bidetList) {
        	String name = e.getName().trim();
        	e.setName(name);
        }
        ArrayList<FaucetVO> faucetList = this.faucetRepository.findNameFromDbFaucet();
        for(FaucetVO e : faucetList) {
        	String name = e.getName().trim();
        	e.setName(name);
        }
        model.addAttribute("bidetList",bidetList);
        model.addAttribute("faucetList",faucetList);
        
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
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        for (CrawlingDto dto : sendMail) {
            String formattedPrice = numberFormat.format(dto.getPrice()) + " 원";
            emailBody.append(count).append(". <a href=\"")
                    .append(dto.getLink())
                    .append("\">")
                    .append(dto.getName())
                    .append("</a> : ")
                    .append(formattedPrice)
                    .append("<br>");
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
        for (CompetitorDto competitor : competitorArray) {
            String formattedPrice = numberFormat.format(competitor.getAveragePrice()) + "원";
            emailBody.append("<tr>")
                    .append("<td>(").append(competitor.getProductName()).append(", ")
                    .append(competitor.getShoppingMall()).append(")</td>")
                    .append("<td>").append(formattedPrice).append("</td>")
                    .append("</tr>");
        }
        emailBody.append("</table>");

        // 엑셀 파일 생성
        ByteArrayOutputStream baos = createExcelFile(sendMail, percentArray);

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

        // 엑셀 파일 첨부
        InputStreamSource attachment = new ByteArrayResource(baos.toByteArray());
        helper.addAttachment("크롤링 결과.xlsx", attachment);

        // 이메일 전송
        mailSender.send(message);
	    }
	}

private ByteArrayOutputStream createExcelFile(ArrayList<CrawlingDto> target, ArrayList<percentDto> percentArray) throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("위반제품 목록");
    int rowNum = 0;

    // Header row for CrawlingDto
    Row headerRow = sheet.createRow(rowNum++);
    headerRow.createCell(0).setCellValue("제품명");
    headerRow.createCell(1).setCellValue("가격");
    headerRow.createCell(2).setCellValue("링크");
    headerRow.createCell(3).setCellValue("쇼핑몰");
    headerRow.createCell(4).setCellValue("판매자 이름");
    headerRow.createCell(5).setCellValue("판매자 주소");
    headerRow.createCell(6).setCellValue("판매자 전화번호");

    // Data rows for CrawlingDto
    NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
    for (CrawlingDto dto : target) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(dto.getName());
        row.createCell(1).setCellValue(numberFormat.format(dto.getPrice()));
        row.createCell(2).setCellValue(dto.getLink());
        row.createCell(3).setCellValue(dto.getSearchFrom());
        if(dto.sellerDetailDto!=null) {
        	row.createCell(4).setCellValue(dto.getSellerDetailDto().getSellerName());
            row.createCell(5).setCellValue(dto.getSellerDetailDto().getSellerAddress());
            row.createCell(6).setCellValue(dto.getSellerDetailDto().getSellerPhoneNumber());
        }
        
    }

    // Create a new sheet for percentArray
    Sheet percentSheet = workbook.createSheet("검색결과 분석");
    rowNum = 0;

    // Header row for percentDto
    Row percentHeaderRow = percentSheet.createRow(rowNum++);
    percentHeaderRow.createCell(0).setCellValue("쇼핑몰");
    percentHeaderRow.createCell(1).setCellValue("위반비율");
    percentHeaderRow.createCell(2).setCellValue("전체 제품수");
    percentHeaderRow.createCell(3).setCellValue("위반 제품수");

    // Data rows for percentDto
    for (percentDto percent : percentArray) {
        Row row = percentSheet.createRow(rowNum++);
        row.createCell(0).setCellValue(percent.getShoppingMall());
        row.createCell(1).setCellValue(percent.getPercent());
        row.createCell(2).setCellValue(percent.getDenominator());
        row.createCell(3).setCellValue(percent.getNumerator());
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    workbook.write(baos);
    workbook.close();

    return baos;
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

package com.daelim.crawling.mainProgram;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import com.daelim.crawling.Daelim.DaelimVO;
import com.daelim.crawling.mainProgram.competitor.CompetitorService;
import com.daelim.crawling.mainProgram.crawlingHistory.CrawlingHistoryService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CrawlingService {
	
	private final CrawlingHistoryService crawlingHistoryService;
	public ArrayList<CrawlingDto> searchMany(ArrayList<DaelimVO> list, String searchType) {
        ArrayList<CrawlingDto> temp = new ArrayList<>();
        for (DaelimVO e : list) {
            String productCode = e.getName();
            if (searchType.equals("all")) {
                temp.addAll(searchNaver(productCode));
                temp.addAll(searchCoupang(productCode));
                // 여기 쇼핑몰 목록 추가
            } else if (searchType.equals("naver")) {
                temp.addAll(searchNaver(productCode));
            } else if (searchType.equals("coupang")) {
                temp.addAll(searchCoupang(productCode));
            }
        }
        
        ArrayList<CrawlingDto> result = new ArrayList<>();
        for (CrawlingDto e : temp) {
            for (DaelimVO e2 : list) {
                if (isValid(e, e2.getName())) {
                    if (e.getPrice() < e2.getPrice()) {
                        e.setOK(false);
                    }
                    int lowerBound = e2.getPrice()-e2.getSearchLimit();
                    int upperBound = e2.getPrice()+e2.getSearchLimit();
                    if(e.getPrice()<lowerBound || e.getPrice()>upperBound) {
                    	break;
                    }
                    LocalDate today = LocalDate.now();
    		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    		        String formattedDate = today.format(formatter);
    				e.setDate(formattedDate);
                    result.add(e);
                }
            }
        }
        crawlingHistoryService.insert(result);
        return result;
    }
	
	
	
	public ArrayList<CrawlingDto> search(String productCode,String searchType) {
		ArrayList<CrawlingDto> temp = new ArrayList<>();
		if(searchType.equals("all")) {
			temp.addAll(searchNaver(productCode));
			temp.addAll(searchCoupang(productCode));
			//여기 쇼핑몰 목록 추가
		}else if(searchType.equals("naver")) {
			temp.addAll(searchNaver(productCode));
		}else if(searchType.equals("coupang")) {
			temp.addAll(searchCoupang(productCode));
		}
//		ArrayList<CrawlingDto> result = new ArrayList<>();
//		for(CrawlingDto e : temp) {
//			if(isValid(e,productCode)) {
//				LocalDate today = LocalDate.now();
//		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//		        String formattedDate = today.format(formatter);
//				e.setDate(formattedDate);
//				result.add(e);
//			}
//		} 
//		crawlingHistoryService.insert(result);
		// 자유검색을 위해 유효성 함수 해제
		return temp;
	}
	
	public boolean isValid(CrawlingDto e,String searchTarget) {
	    if (e == null) {
	        return false;
	    }
	    if(!e.getName().contains(searchTarget)) {
	    	return false;
	    }
	    String name = e.getName();
	    if (name.contains("대림") || name.contains("도비도스") || name.contains("대림통상")) {
	        if (name.contains("대림바스")) {
	            return false;
	        }
	        return true;
	    }
	    return false;
	}

	
	public ArrayList<CrawlingDto> searchCoupang(String target) {
	    // 1. 웹 드라이버 설정
	    System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
	    ChromeOptions options = new ChromeOptions();
	    ArrayList<CrawlingDto> result = new ArrayList<>();

	    for (int i = 1; i <= 5; i++) {
	    	int count = 0;
	        WebDriver driver = null;
	        try {
	            driver = new ChromeDriver(options);
	            // 2. 웹 페이지 접속
	            String baseUrl = "https://www.coupang.com/np/search?q="
	                    + target + "&channel=user&component=&eventCategory=SRP&trcid=&traid=&sorter=scoreDesc&minPrice=&maxPrice=&priceRange=&filterType=&listSize="
	                    + "72&filter=&isPriceRange=false&brand=&offerCondition=&rating=0&page="
	                    + i;
	            driver.get(baseUrl);

	            // 암시적 대기 (최대 10초 대기)
	            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

	            // 3. 무한 스크롤을 통해 페이지 아래로 스크롤하며 데이터 추출
	            JavascriptExecutor js = (JavascriptExecutor) driver;
	            List<WebElement> productElements = new ArrayList<>();
	            int lastHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();
	            while (true) {
	                // 스크롤을 페이지 가장 아래로 내린다
	                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	                try {
	                    // 데이터 로드 대기
	                    Thread.sleep(2000);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }

	                // 새로 로드된 데이터 찾기
	                List<WebElement> newProductElements = driver.findElements(By.cssSelector("li.search-product"));
	                System.out.println("추출 끝났음");
	                if (newProductElements.size() > productElements.size()) {
	                    productElements = newProductElements;
	                } else {
	                    // 더 이상 새로운 데이터가 없으면 스크롤 중지
	                    break;
	                }

	                // 현재 높이와 이전 높이가 같으면 무한 스크롤 종료
	                int newHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();
	                if (newHeight == lastHeight) {
	                    break;
	                }
	                lastHeight = newHeight;
	            }

	            // 제품 이름과 가격 매칭하여 추출
	            ArrayList<CrawlingDto> productDatas = new ArrayList<>();
	            for (WebElement productElement : productElements) {
	                try {
	                    // 제품 이름 추출
	                    WebElement titleElement = productElement.findElement(By.cssSelector("div.name"));
	                    String productName = titleElement.getText();

	                    // 가격 추출 (클래스 이름이 없는 em 태그)
	                    WebElement priceElement = productElement.findElement(By.cssSelector("strong.price-value"));
	                    String productPrice = priceElement.getText();
	                    int intPrice = StoI(productPrice);

	                 // 링크 Url 추출
	                    WebElement linkElement = productElement.findElement(By.cssSelector("a"));
	                    String productUrl = linkElement.getAttribute("href");

	                    // CrawlingDto 객체에 저장 (CrawlingDto 클래스는 미리 정의되어 있어야 함)
	                    CrawlingDto productData = new CrawlingDto(productName, intPrice, productUrl,"coupang");
	                    if(isValid(productData, target)) {
	                    	productDatas.add(productData);
	                    	count++;
	                    }
	                    
	                } catch (Exception e) {
	                    System.out.println(e);
	                    // 데이터 추출 실패 시 예외 처리 (continue로 다음 제품으로 넘어감)
	                    continue;
	                }
	            }

	            for (CrawlingDto e : productDatas) {
	                result.add(e);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (driver != null) {
	                driver.quit();
	            }
	        }
	        if(count==0) {
	        	break;
	        }
	    }
	    
	    // 결과 반환
	    return result;
	}

	
	public ArrayList<CrawlingDto> searchNaver(String target) {
        // 1. 웹 드라이버와 크롬 드라이버 설정
        System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");  // 창을 열지 않음
        WebDriver driver = new ChromeDriver(options);
        ArrayList<CrawlingDto> result = new ArrayList<>();
        for(int i=1;i<=10;i++) {
        	int count = 0;
        	// 2. 웹 페이지 접속
            String baseUrl = "https://search.shopping.naver.com/search/all?adQuery="
                            + target + "&origQuery="
                            + target + "&pagingIndex="
                            + i + "&pagingSize=80&productSet=total&query="
                            + target + "&sort=rel&timestamp=&viewType=list";
            driver.get(baseUrl);
            
            // 암시적 대기 (최대 10초 대기)
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            // 3. 무한 스크롤을 통해 페이지 아래로 스크롤하며 데이터 추출
            JavascriptExecutor js = (JavascriptExecutor) driver;
            List<WebElement> productElements = new ArrayList<>();
            int lastHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();
            while (true) {
                // 스크롤을 페이지 가장 아래로 내린다
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                try {
                    // 데이터 로드 대기
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // 새로 로드된 데이터 찾기
                List<WebElement> newProductElements = driver.findElements(By.cssSelector("div.product_info_area__xxCTi"));
                if (newProductElements.size() > productElements.size()) {
                    productElements = newProductElements;
                } else {
                    // 더 이상 새로운 데이터가 없으면 스크롤 중지
                    break;
                }
                
                // 현재 높이와 이전 높이가 같으면 무한 스크롤 종료
                int newHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();
                if (newHeight == lastHeight) {
                    break;
                }
                lastHeight = newHeight;
            }
            
            // 제품 이름과 가격 매칭하여 추출
            ArrayList<CrawlingDto> productDatas = new ArrayList<>();
            for (WebElement productElement : productElements) {
                try {
                    // 제품 이름 추출
                    WebElement titleElement = productElement.findElement(By.cssSelector("div.product_title__Mmw2K a"));
                    String productName = titleElement.getText();
                    
                    // 가격 추출 (클래스 이름이 없는 em 태그)
                    WebElement priceElement = productElement.findElement(By.xpath(".//em[not(@class)]"));
                    String productPrice = priceElement.getText();
                    int intPrice = StoI(productPrice);
                    
                    //링크 Url 추출                    
                    WebElement linkElement = productElement.findElement(By.cssSelector("a.product_link__TrAac.linkAnchor"));
                    String productUrl = linkElement.getAttribute("href");
                    
                    // CrawlingDto 객체에 저장 (CrawlingDto 클래스는 미리 정의되어 있어야 함)
                    CrawlingDto productData = new CrawlingDto(productName, intPrice,productUrl,"naver");
                    if(isValid(productData, target)) {
                    	count++;
                    	productDatas.add(productData);
                    }
                } catch (Exception e) {
                    // 데이터 추출 실패 시 예외 처리 (continue로 다음 제품으로 넘어감)
                    continue;
                }
            }
            
            for(CrawlingDto e : productDatas) {
            	result.add(e);
            }
            if(count==0) {
            	break;
            }
        }
        driver.quit();
        // 결과 반환
        return result;
    }
	
	private int StoI(String input) {
        String cleanedInput = input.replace(",", "");
        return Integer.parseInt(cleanedInput);
    }
}

package com.daelim.crawling.mainProgram;


import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.daelim.crawling.Daelim.DaelimVO;
import com.daelim.crawling.mainProgram.competitor.CompetitorService;
import com.daelim.crawling.mainProgram.crawlingHistory.CrawlingHistoryService;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;

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
            } else if (searchType.equals("daum")) {
                temp.addAll(searchDaum(productCode));
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
			temp.addAll(searchDaum(productCode));
			//여기 쇼핑몰 목록 추가
		}else if(searchType.equals("naver")) {
			temp.addAll(searchNaver(productCode));
		}else if(searchType.equals("coupang")) {
			temp.addAll(searchCoupang(productCode));
		}else if (searchType.equals("daum")) {
            temp.addAll(searchDaum(productCode));
        }

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

	public ArrayList<CrawlingDto> searchDaum(String target) {
		
	    System.setProperty("webdriver.chrome.driver", "C:\\CrawlingProject\\driver\\chromedriver.exe");
	    ChromeOptions options = new ChromeOptions();
	    WebDriver driver = new ChromeDriver(options);
	    ArrayList<CrawlingDto> result = new ArrayList<>();

	    for (int i = 1; i <= 5; i++) {
	        int count = 0;
	        System.out.println("📄 [페이지 " + i + "] 접속 시도");

	        String baseUrl = "https://shoppinghow.kakao.com/search/" + target + "/page:" + i + "&view_type:list&image_filter_cnt:46";
	        driver.get(baseUrl);
	        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        List<WebElement> productElements = new ArrayList<>();
	        int lastHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();

	        while (true) {
	            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	            try {
	                Thread.sleep(2000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }

	            List<WebElement> newProductElements = driver.findElements(By.cssSelector("div.wrap_prod_top"));
	            System.out.println("📦 현재 상품 수집 수: " + newProductElements.size());

	            if (newProductElements.size() > productElements.size()) {
	                productElements = newProductElements;
	            } else {
	                System.out.println("🔚 더 이상 새로운 상품이 없습니다.");
	                break;
	            }

	            int newHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();
	            if (newHeight == lastHeight) {
	                System.out.println("📉 스크롤 더 이상 변화 없음, 종료");
	                break;
	            }
	            lastHeight = newHeight;
	        }

	        System.out.println("🔍 수집된 상품 갯수: " + productElements.size());

	        ArrayList<CrawlingDto> productDatas = new ArrayList<>();
	        for (WebElement productElement : productElements) {
	            try {
	                WebElement titleElement = productElement.findElement(By.cssSelector("div.wrap_cont > strong > a"));
	                String productName = titleElement.getAttribute("data-tiara-name");
	                String productUrl = titleElement.getAttribute("href");

	                WebElement priceElement = productElement.findElement(By.cssSelector("div.wrap_compare > div.wrap_price > span.num_price.lowest"));
	                String productPrice = priceElement.getText();
	                int intPrice = StoI(productPrice);

	                CrawlingDto productData = new CrawlingDto(productName, intPrice, productUrl, "daum");
	                if (isValid(productData, target)) {
	                    productDatas.add(productData);
	                    count++;
	                    System.out.println("✅ 상품 추가: " + productName + " | " + productPrice + " | " + productUrl);
	                } else {
	                    System.out.println("⛔ 필터링으로 제외됨: " + productName);
	                }
	            } catch (Exception e) {
	                System.out.println("⚠️ 상품 처리 중 예외 발생: " + e.getMessage());
	                continue;
	            }
	        }

	        result.addAll(productDatas);
	        System.out.println("🧾 [페이지 " + i + "] 유효 상품 수: " + count);

	        if (count == 0) {
	            System.out.println("📭 유효한 상품이 없어 다음 페이지 탐색 중단");
	            break;
	        }
	    }
	    
	    driver.quit();
	    System.out.println("✅ 전체 수집 완료: 총 " + result.size() + "개");
	    return result;
	}


	public ArrayList<CrawlingDto> searchCoupang(String target) {

	    System.setProperty("webdriver.chrome.driver", "C:\\CrawlingProject\\driver\\chromedriver.exe");
	    
	    ChromeOptions options = new ChromeOptions();
	    options.addArguments("--disable-blink-features=AutomationControlled");
	    options.addArguments("start-maximized");
	    options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
	    options.setExperimentalOption("useAutomationExtension", false);
    	
	    // ✅ User-Agent와 Accept-Language를 ChromeOptions에 직접 추가
	    options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
	    options.addArguments("accept-language=ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
	    
	    options.setPageLoadStrategy(PageLoadStrategy.EAGER);
	    
	    ArrayList<CrawlingDto> result = new ArrayList<>();
	    WebDriver driver = null;
	    
	    try {
	        driver = new ChromeDriver(options);

	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        Actions actions = new Actions(driver);

	        // 1. 쿠팡 접속
	        driver.get("https://www.coupang.com/");
	        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#headerSearchKeyword")));

	        // 2. 검색어 입력
	        searchInput.click();
	        actions.sendKeys(searchInput, target).perform();
	        Thread.sleep(1000);

	        // 3. 검색 버튼 클릭
	        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#headerSearchBtn")));
	        js.executeScript("arguments[0].click();", searchBtn);
	        Thread.sleep(3000);

	        // 4. 검색 결과 대기
	        try {
	            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.search-product-list")));
	        } catch (TimeoutException e) {
	            System.out.println("※ 검색 결과 없음 또는 로딩 실패: " + target);
	            return result;
	        }
	        
	        // 5. 최대 5페이지 반복
	        for (int page = 1; page <= 5; page++) {
	            System.out.println("▶ 페이지 " + page + " 처리 중...");

	            List<WebElement> productElements = new ArrayList<>();
	            int lastHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();

	            while (true) {
	                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	                Thread.sleep(2000);

	                List<WebElement> newProductElements = driver.findElements(By.cssSelector("li.search-product"));
	                if (newProductElements.size() > productElements.size()) {
	                    productElements = newProductElements;
	                } else {
	                    break;
	                }

	                int newHeight = ((Number) js.executeScript("return document.body.scrollHeight")).intValue();
	                if (newHeight == lastHeight) break;
	                lastHeight = newHeight;
	            }

	            for (WebElement productElement : productElements) {
	                try {
	                    String productName = productElement.findElement(By.cssSelector("div.name")).getText();
	                    String productPrice = productElement.findElement(By.cssSelector("strong.price-value")).getText();
	                    int intPrice = StoI(productPrice);
	                    String productUrl = productElement.findElement(By.cssSelector("a")).getAttribute("href");

	                    CrawlingDto dto = new CrawlingDto(productName, intPrice, productUrl, "coupang");
	                    if (isValid(dto, target)) result.add(dto);
	                } catch (Exception e) {
	                    continue;
	                }
	            }
	            
	            // 다음 페이지 이동
	            try {
	                String nextBtnCss = "#searchOptionForm > div.search-wrapper > div.search-content.search-content-with-feedback > div.search-pagination > span.btn-page > a:nth-child(" + (page + 1) + ")";
	                WebElement nextPageBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(nextBtnCss)));
	                js.executeScript("arguments[0].scrollIntoView(true);", nextPageBtn);
	                Thread.sleep(500);
	                js.executeScript("arguments[0].click();", nextPageBtn);
	                Thread.sleep(3000);
	                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.search-product-list")));
	            } catch (Exception e) {
	                System.out.println("▶ 더 이상 다음 페이지 없음");
	                break;
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (driver != null) driver.quit();
	    }

	    return result;
	}
	
	
	public ArrayList<CrawlingDto> searchNaver(String target) {
        // 1. 웹 드라이버와 크롬 드라이버 설정
	    System.setProperty("webdriver.chrome.driver", "C:\\CrawlingProject\\driver\\chromedriver.exe");
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

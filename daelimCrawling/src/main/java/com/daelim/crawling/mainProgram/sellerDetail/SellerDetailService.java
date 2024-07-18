package com.daelim.crawling.mainProgram.sellerDetail;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
public class SellerDetailService {
	
	
	public SellerDetailDto searchSellerCoupang(String url) {
        // 1. 웹 드라이버 설정
        System.setProperty("webdriver.chrome.driver", "C:\\CrawlingProject\\driver\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        SellerDetailDto result = new SellerDetailDto();

        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            // 2. 웹 페이지 접속
            driver.get(url);

            // 암시적 대기 (최대 10초 대기)
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            // 3. 무한 스크롤을 통해 페이지 아래로 스크롤
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);  // 데이터 로드 대기

            // 4. 판매자 정보 추출
            try {
                WebElement sellerNameElement = driver.findElement(By.cssSelector("#btfTab > ul.tab-contents > li.product-etc.tab-contents__content.etc-new-style > div > table > tbody > tr:nth-child(1) > td:nth-child(2)"));
                WebElement sellerAddressElement = driver.findElement(By.cssSelector("#btfTab > ul.tab-contents > li.product-etc.tab-contents__content.etc-new-style > div > table > tbody > tr:nth-child(1) > td:nth-child(4)"));
                WebElement sellerPhoneNumberElement = driver.findElement(By.cssSelector("#btfTab > ul.tab-contents > li.product-etc.tab-contents__content.etc-new-style > div > table > tbody > tr:nth-child(2) > td:nth-child(4)"));

                // 추출한 정보를 SellerDetailDto 객체에 저장
                result.setSellerName(sellerNameElement.getText());
                result.setSellerAddress(sellerAddressElement.getText());
                result.setSellerPhoneNumber(sellerPhoneNumberElement.getText());
            } catch (NoSuchElementException e) {
                result.setSellerName("쿠팡 로켓배송 직매입 상품");
                result.setSellerPhoneNumber("1577-7011");
                result.setSellerAddress("물류센터 상품이라 확인불가");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        // 결과 반환
        return result;
    }
	
	
	public SellerDetailDto searchSellerNaver(String url) {
	    // 1. 웹 드라이버와 크롬 드라이버 설정
	    System.setProperty("webdriver.chrome.driver", "C:\\CrawlingProject\\driver\\chromedriver.exe");
	    ChromeOptions options = new ChromeOptions();
	    WebDriver driver = new ChromeDriver(options);
	    SellerDetailDto result = new SellerDetailDto();
	    
	    try {
	        // 2. 웹 페이지 접속
	        driver.get(url);
	        
	        // 3.7초 대기
	        Thread.sleep(4000);
	        
	        // 경고창이 열렸을 경우 처리
	        try {
	            Alert alert = driver.switchTo().alert();
	            alert.accept(); // 경고창을 수락 (또는 alert.dismiss()로 무시)
	        } catch (NoAlertPresentException e) {
	            // 경고창이 없는 경우
	        }
	        
	        // 무한 스크롤을 통해 페이지 아래로 스크롤
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);  // 데이터 로드 대기
	        // 현재 URL 가져오기 (리디렉션 후 URL)
	        String currentUrl = driver.getCurrentUrl();
	        
	        // 3. 현재 URL에 "smartstore"가 포함되어 있는지 확인
	        if (currentUrl.contains("smartstore") || currentUrl.contains("coupang")
	           || currentUrl.contains("himart") || currentUrl.contains("ssg")
	           || currentUrl.contains("lotteon") || currentUrl.contains("11st")
	           || currentUrl.contains("auction")) {
	            // 그냥 계속 진행하면 됨
	        } else {
	            return null;
	        }

	        // 4. 판매자 정보 추출
	        try {
	            if(currentUrl.contains("smartstore")){
	                WebElement sellerNameElement = driver.findElement(By.cssSelector("#content > div > div.z7cS6-TO7X > div.etc > div._1aH4b0l27f > div._211Avwm-sU > table > tbody > tr > td:nth-child(2)"));
	                WebElement sellerAddressElement = driver.findElement(By.cssSelector("#RETURNPOLICY > div > table > tbody > tr:nth-child(3) > td"));
	                result.setSellerPhoneNumber("보안정책으로 인해 확인불가");
	                result.setSellerName(sellerNameElement.getText());
	                result.setSellerAddress(sellerAddressElement.getText());
	            } else if(currentUrl.contains("coupang")) {
	                WebElement sellerNameElement = driver.findElement(By.cssSelector("#btfTab > ul.tab-contents > li.product-etc.tab-contents__content.etc-new-style > div > table > tbody > tr:nth-child(1) > td:nth-child(2)"));
	                WebElement sellerAddressElement = driver.findElement(By.cssSelector("#btfTab > ul.tab-contents > li.product-etc.tab-contents__content.etc-new-style > div > table > tbody > tr:nth-child(1) > td:nth-child(4)"));
	                WebElement sellerPhoneNumberElement = driver.findElement(By.cssSelector("#btfTab > ul.tab-contents > li.product-etc.tab-contents__content.etc-new-style > div > table > tbody > tr:nth-child(2) > td:nth-child(4)"));
	                result.setSellerName(sellerNameElement.getText());
	                result.setSellerAddress(sellerAddressElement.getText());
	                result.setSellerPhoneNumber(sellerPhoneNumberElement.getText());
	            } else if(currentUrl.contains("himart")) {
	                WebElement sellerNameElement = driver.findElement(By.cssSelector("#prdAddInfo > div > div.prdInfoWrap.articleAll.article_0016979637 > table > tbody > tr:nth-child(10) > td"));
	                result.setSellerName(sellerNameElement.getText());
	                result.setSellerAddress("확인되지 않음");
	                result.setSellerPhoneNumber("판매자 이름에 표기");
	            } else if(currentUrl.contains("ssg")) {
	                WebElement sellerNameElement = driver.findElement(By.cssSelector("#_cdtl_dtlcont_wrap > div.cdtl_dtlcont_lft > div > div.cdtl_tabcont_wrap > div.cdtl_sec.cdtl_consignment > div.cdtl_cont_info > div > table > tbody > tr:nth-child(1) > td > div"));
	                WebElement sellerAddressElement = driver.findElement(By.cssSelector("#_cdtl_dtlcont_wrap > div.cdtl_dtlcont_lft > div > div.cdtl_tabcont_wrap > div.cdtl_sec.cdtl_consignment > div.cdtl_cont_info > div > table > tbody > tr:nth-child(2) > td > div"));
	                result.setSellerName(sellerNameElement.getText());
	                result.setSellerAddress(sellerAddressElement.getText());
	                result.setSellerPhoneNumber("확인되지 않음");
	            } else if(currentUrl.contains("lotteon")) {
	                WebElement sellerNameElement = driver.findElement(By.cssSelector("#stickyTabParent > div.tabArea > div:nth-child(4) > div > div:nth-child(1) > div:nth-child(2) > div > div:nth-child(1) > div > table > tr:nth-child(3) > td > p"));
	                WebElement sellerAddressElement = driver.findElement(By.cssSelector("#stickyTabParent > div.tabArea > div:nth-child(4) > div > div:nth-child(1) > div:nth-child(2) > div > div:nth-child(1) > div > table > tr:nth-child(10) > td > p"));
	                WebElement sellerPhoneNumberElement = driver.findElement(By.cssSelector("#stickyTabParent > div.tabArea > div:nth-child(4) > div > div:nth-child(1) > div:nth-child(2) > div > div:nth-child(1) > div > table > tr:nth-child(8) > td > p"));
	                result.setSellerName(sellerNameElement.getText());
	                result.setSellerAddress(sellerAddressElement.getText());
	                result.setSellerPhoneNumber(sellerPhoneNumberElement.getText());
	            } else if(currentUrl.contains("11st")) {
	                WebElement sellerNameElement = driver.findElement(By.cssSelector("#tabpanelDetail4 > div > table:nth-child(9) > tbody > tr:nth-child(1) > td:nth-child(4)"));
	                WebElement sellerAddressElement = driver.findElement(By.cssSelector("#tabpanelDetail4 > div > table:nth-child(9) > tbody > tr:nth-child(5) > td"));
	                WebElement sellerPhoneNumberElement = driver.findElement(By.cssSelector("#tabpanelDetail4 > div > table:nth-child(9) > tbody > tr:nth-child(2) > td:nth-child(4)"));
	                result.setSellerName(sellerNameElement.getText());
	                result.setSellerAddress(sellerAddressElement.getText());
	                result.setSellerPhoneNumber(sellerPhoneNumberElement.getText());
	            } else if(currentUrl.contains("auction")) {
	                WebElement sellerNameElement = driver.findElement(By.cssSelector("#vip_tab_exchange > div.box__exchange-guide > div:nth-child(6) > ul > li:nth-child(1) > span"));
	                WebElement sellerAddressElement = driver.findElement(By.cssSelector("#vip_tab_exchange > div.box__exchange-guide > div:nth-child(6) > ul > li:nth-child(6) > span"));
	                WebElement sellerPhoneNumberElement = driver.findElement(By.cssSelector("#vip_tab_exchange > div.box__exchange-guide > div:nth-child(6) > ul > li:nth-child(3) > span"));
	                result.setSellerName(sellerNameElement.getText());
	                result.setSellerAddress(sellerAddressElement.getText());
	                result.setSellerPhoneNumber(sellerPhoneNumberElement.getText());
	            }
	        } catch (NoSuchElementException e) {
	            System.out.println("판매자 정보를 찾을 수 없습니다: " + e.getMessage());
	            if (currentUrl.contains("coupang")) {
	                result.setSellerName("쿠팡 로켓배송 직매입 상품");
	                result.setSellerPhoneNumber("1577-7011");
	                result.setSellerAddress("물류센터 상품이라 확인불가");
	            }else {
	            	result.setSellerName("일회성 광고(오늘 하루 보지않기) 등으로 인해");
	                result.setSellerPhoneNumber("정보 추출이 실패했을 확률이 높습니다");
	                result.setSellerAddress("상품 링크를 직접 클릭해서 정보를 찾아주세요");
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        // 브라우저 닫기
	        if (driver != null) {
	            driver.quit();
	        }
	    }
    	
	    // 결과 반환
	    return result;
	}
	
}

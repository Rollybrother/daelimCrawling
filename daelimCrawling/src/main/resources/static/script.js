// 전역변수 선언
let isAutoSearchEnabled = false;
let intervalDays = 1;
// 자동 검색 기능 설정
let autoSearchTimeoutId;
//여기서부터 등록된 제품에 대한 검색기능
let allRows = [];

//여기는 전체보기 기능의 체크상태를 표시하기 위한 변수
var originalRows;
// 검색 기능
function filterByName() {
    let keyword = $('#searchKeyword').val().toLowerCase();
    $('#dataTable tbody tr').each(function() {
        $(this).toggle($(this).find('.product-name').text().toLowerCase().indexOf(keyword) > -1);
    });
}

// 전체보기 기능
function showAll() {
    // 현재 체크된 상태를 저장
    var checkedStates = {};
    $('#dataTable tbody tr').each(function(index, row) {
        var checkbox = $(row).find('.rowCheckbox');
        if (checkbox.prop('checked')) {
            checkedStates[index] = true;
        }
    });

    $('#searchKeyword').val('');
    $('#dataTable tbody').empty().append(originalRows.clone());

    // 저장된 체크 상태를 다시 적용
    $('#dataTable tbody tr').each(function(index, row) {
        if (checkedStates[index]) {
            $(row).find('.rowCheckbox').prop('checked', true);
        }
    });

    // 행 수 체크하여 스크롤 추가
    checkTableRows();
}

// 테이블 행 수 체크하여 스크롤 추가하는 함수
function checkTableRows() {
    const rowCount = $('#dataTable tbody tr').length;
    const maxRows = 4;
    const wrapper = $('#dataTableWrapper');

    if (rowCount > maxRows) {
        wrapper.css('max-height', '200px');
        wrapper.css('overflow-y', 'auto');
    } else {
        wrapper.css('max-height', '');
        wrapper.css('overflow-y', '');
    }
}

// 등록제품에 대한 검색과 전체보기 기능 종료
// 여기서부터 페이징 구현 시작
let currentPage = 1;
const rowsPerPage = 10;
let totalRows = document.querySelectorAll('#searchedDataTable tbody tr').length;
let totalPages = Math.ceil(totalRows / rowsPerPage);

function displayRows() {
    let rows = document.querySelectorAll('#searchedDataTable tbody tr');
    rows.forEach((row, index) => {
        row.style.display = (index >= (currentPage - 1) * rowsPerPage && index < currentPage * rowsPerPage) ? '' : 'none';
    });
    updatePagination();
}

function goToPage(page) {
    if (page === 'first') {
        currentPage = 1;
    } else if (page === 'last') {
        currentPage = totalPages;
    } else {
        currentPage = page;
    }
    displayRows();
}

function previousPage() {
    if (currentPage > 1) {
        currentPage--;
        displayRows();
    }
}

function nextPage() {
    if (currentPage < totalPages) {
        currentPage++;
        displayRows();
    }
}

function updatePagination() {
    const pagination = document.querySelector('.pagination');
    pagination.innerHTML = `
        <li class="page-item">
            <a class="page-link" href="#" aria-label="Previous" onclick="goToPage('first')">
                <span aria-hidden="true"><<</span>
            </a>
        </li>
        <li class="page-item">
            <a class="page-link" href="#" aria-label="Previous" onclick="previousPage()">
                <span aria-hidden="true">&laquo;</span>
            </a>
        </li>
    `;

    for (let i = 1; i <= totalPages; i++) {
        pagination.innerHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="goToPage(${i})">${i}</a>
            </li>
        `;
    }

    pagination.innerHTML += `
        <li class="page-item">
            <a class="page-link" href="#" aria-label="Next" onclick="nextPage()">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
        <li class="page-item">
            <a class="page-link" href="#" aria-label="Next" onclick="goToPage('last')">
                <span aria-hidden="true">>></span>
            </a>
        </li>
    `;
}

document.addEventListener('DOMContentLoaded', function() {
    displayRows();
    // 판매자 정보 관련 자바스크립트
    var priceElements = document.querySelectorAll(".price");
    priceElements.forEach(function(element) {
        var price = parseInt(element.getAttribute("data-price"));
        element.textContent = formatNumberWithComma(price);
    });

    // 판매자 정보 버튼 클릭 이벤트 리스너 추가
    var sellerInfoButtons = document.querySelectorAll(".seller-info");
    sellerInfoButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            showSellerDetail(button);
        });
    });
});

function formatNumberWithComma(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") + "원";
}

function showSellerDetail(button) {
	
    var sellerName = button.getAttribute("data-sellername");
    var sellerAddress = button.getAttribute("data-selleraddress");
    var sellerPhoneNumber = button.getAttribute("data-sellerphone");

    // HTML 요소가 존재하는지 확인
    var sellerNameElement = document.getElementById("sellerName");
    var sellerAddressElement = document.getElementById("sellerAddress");
    var sellerPhoneNumberElement = document.getElementById("sellerPhoneNumber");

    if (sellerNameElement && sellerAddressElement && sellerPhoneNumberElement) {
        sellerNameElement.textContent = "판매자 이름: " + sellerName;
        sellerAddressElement.textContent = "판매자 주소: " + sellerAddress;
        sellerPhoneNumberElement.textContent = "판매자 연락처: " + sellerPhoneNumber;

        var sellerDetailModal = new bootstrap.Modal(document.getElementById("sellerDetailModal"));
        sellerDetailModal.show();
    } else {
        console.error("판매자 정보 요소를 찾을 수 없습니다.");
    }
}


// 페이징 구현종료

// 입력값을 지우는 함수
function clearInput() {
    $("#productCode").val("");
}

// 전체 선택 체크박스를 클릭했을 때의 동작
function selectAllCheckboxes(source) {
    $('.rowCheckbox').prop('checked', source.checked);
}

//데이터 추가 함수
function addRow() {
    console.log("addRow called");
    const modal = new bootstrap.Modal(document.getElementById('productModal'), {
        backdrop: 'static',
        keyboard: false
    });
    modal.show();

    const confirmAdd = document.getElementById("confirmAdd");

    confirmAdd.onclick = function() {
        const productName = document.getElementById("productName").value.trim();
        let productPrice = document.getElementById("productPrice").value.replace(/,/g, '');
        let searchLimit = document.getElementById("searchLimit").value.replace(/,/g, '');
        const competitor1Product = document.getElementById("competitor1Product").value;
        const competitor1Name = document.getElementById("competitor1Name").value;
        const competitor2Product = document.getElementById("competitor2Product").value;
        const competitor2Name = document.getElementById("competitor2Name").value;

        if (isNaN(productPrice) || isNaN(searchLimit)) {
            alert("가격이나 검색 범위는 숫자를 입력해야 합니다.");
            return;
        }

        if (productName && productPrice) {
            $.ajax({
                type: "POST",
                url: "/ajax/addProduct",
                contentType: "application/json",
                data: JSON.stringify({ 
                    name: productName, 
                    price: productPrice, 
                    searchLimit: searchLimit,
                    competitor1Product: competitor1Product, 
                    competitor2Product: competitor2Product, 
                    competitor1Name: competitor1Name,
                    competitor2Name: competitor2Name 
                }),
                success: function(response) {
                    const formattedPrice = response.price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
                    const formattedSearchLimit = response.searchLimit.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");

                    const newRow = `
                        <tr>
                            <td><input type="checkbox" class="rowCheckbox"></td>
                            <td class="clickable">${response.index}</td>
                            <td class="clickable product-name">${response.name}</td>
                            <td class="clickable price-column"><span class="price" data-price="${response.price}">${formattedPrice}</span> 원</td>
                            <td class="clickable search-limit-column"><span class="searchLimit" data-limit="${response.searchLimit}">${formattedSearchLimit}</span> 원</td>
                            <td class="clickable competitor-column">${response.competitor1Product}</td>
                            <td class="clickable competitor-column">${response.competitor1Name}</td>
                            <td class="clickable competitor-column">${response.competitor2Product}</td>
                            <td class="clickable competitor-column">${response.competitor2Name}</td>
                        </tr>
                    `;
                    $('#dataTable tbody').append(newRow);

                    allRows = $('#dataTable tbody tr').clone();

                    modal.hide();
                    checkTableRows();
                },
                error: function(error) {
                    alert("데이터 추가 중 오류가 발생했습니다.");
                }
            });
        } else {
            alert("제품 이름과 가격을 모두 입력해야 합니다.");
        }
    }
}



// 테이블 행 수 체크하여 스크롤 추가하는 함수
function checkTableRows() {
    const rowCount = $('#dataTable tbody tr').length;
    const maxRows = 3;
    const wrapper = $('#dataTableWrapper');

    if (rowCount > maxRows) {
        wrapper.css('max-height', '200px');
        wrapper.css('overflow-y', 'auto');
    } else {
        wrapper.css('max-height', '');
        wrapper.css('overflow-y', '');
    }
}




// 데이터 삭제 함수 (AJAX 사용)
function deleteRow() {
    console.log("deleteRow called");
    const checkedRows = $('input.rowCheckbox:checked');
    const indices = Array.from(checkedRows).map(row => 
        parseInt($(row).closest('tr').find('td:nth-child(2)').text())
    );

    if (indices.length > 0) {
        $.ajax({
            type: "DELETE",
            url: "/ajax/deleteProducts",
            contentType: "application/json",
            data: JSON.stringify(indices),
            success: function() {
                checkedRows.closest('tr').remove();
            },
            error: function(error) {
                alert("데이터 삭제 중 오류가 발생했습니다.");
            }
        });
    } else {
        alert("삭제할 항목을 선택하세요.");
    }
}

function searchChecked() {
    console.log("searchChecked called");
    
    const checkedRows = $('input.rowCheckbox:checked');
    const selectedItems = Array.from(checkedRows).map(row => {
        const cells = $(row).closest('tr').find('td');
        return {
            index: cells.eq(1).text(),
            name: cells.eq(2).text(),
            price: parseInt(cells.eq(3).find('.price').text().replace(/,/g, '').replace(/원/g, '').trim()),
            searchLimit: parseInt(cells.eq(4).find('.searchLimit').text().replace(/,/g, '').replace(/원/g, '').trim()),
            competitor1Product: cells.eq(5).text(),
            competitor1Name: cells.eq(6).text(),
            competitor2Product: cells.eq(7).text(),
            competitor2Name: cells.eq(8).text()
        };
    });

    // 선택된 항목이 없을 경우 알림 표시
    if (selectedItems.length === 0) {
        alert("선택된 항목이 없습니다.");
        return;
    }

    // sellerDetailModal 모달 창 숨기기
    $('#sellerDetailModal').modal('hide');
    // loadingPopup 모달 창 표시
    $('#loadingPopup').modal('show');

    const searchType = $('input[name="searchType"]:checked').val();
    const autoSearchEnabled = typeof isAutoSearchEnabled !== 'undefined' ? isAutoSearchEnabled : false;
    let autoSearchInterval = typeof intervalDays !== 'undefined' ? intervalDays : 1;

    if (isNaN(autoSearchInterval)) {
        autoSearchInterval = 1;
    }

    const form = $('<form>', { method: 'POST', action: '/crawling/searchChecked' });

    selectedItems.forEach(item => {
        form.append($('<input>', { type: 'hidden', name: 'list.index[]', value: item.index }));
        form.append($('<input>', { type: 'hidden', name: 'list.name[]', value: item.name }));
        form.append($('<input>', { type: 'hidden', name: 'list.price[]', value: item.price }));
        form.append($('<input>', { type: 'hidden', name: 'list.searchLimit[]', value: item.searchLimit }));
        form.append($('<input>', { type: 'hidden', name: 'list.competitor1Product[]', value: item.competitor1Product }));
        form.append($('<input>', { type: 'hidden', name: 'list.competitor1Name[]', value: item.competitor1Name }));
        form.append($('<input>', { type: 'hidden', name: 'list.competitor2Product[]', value: item.competitor2Product }));
        form.append($('<input>', { type: 'hidden', name: 'list.competitor2Name[]', value: item.competitor2Name }));
    });

    form.append($('<input>', { type: 'hidden', name: 'searchType', value: searchType }));
    form.append($('<input>', { type: 'hidden', name: 'autoSearchEnabled', value: autoSearchEnabled }));
    form.append($('<input>', { type: 'hidden', name: 'autoSearchInterval', value: autoSearchInterval }));

    $('body').append(form);
    form.submit();
}


// 점이 증가하는 애니메이션
let loadingInterval;
$(document).on('show.bs.modal', '#loadingPopup', function () {
    let dots = 0;
    loadingInterval = setInterval(function () {
        dots = (dots + 1) % 4;
        $('#loadingDots').text('.'.repeat(dots));
    }, 500);
});

$(document).on('hide.bs.modal', '#loadingPopup', function () {
    clearInterval(loadingInterval);
});



function calculateNextTimeout() {
    const now = new Date();
    const nextRun = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 10, 0, 0, 0); // 다음 13:08

    if (now > nextRun) {
      nextRun.setDate(nextRun.getDate() + intervalDays); // 오늘 13:08이 이미 지났다면 다음 주기로 지정
    }
    return nextRun - now; 
}


function setAutoSearch() {
    console.log("자동검색 설정");
    isAutoSearchEnabled = $('#autoSearchToggle').is(':checked');
	intervalDays = parseInt($('#autoSearchInterval').val(), 10);

	
    // intervalDays가 NaN인지 확인하고 기본값 설정
    if (isNaN(intervalDays) || intervalDays <= 0) {
        intervalDays = 1;
    }

    if (isAutoSearchEnabled) {
        const timeout = calculateNextTimeout();
		   	if(timeout>0){
				autoSearchTimeoutId = setTimeout(() => {
		        searchChecked();
		        setRecurringSearch(intervalDays);
		        }, timeout);
		        console.log(`자동 검색이 ${timeout / 1000}초 후에 실행됩니다.`);
			}  
	    } else if (autoSearchTimeoutId) {
	        clearTimeout(autoSearchTimeoutId);
	        autoSearchTimeoutId = null; 
	    }

    // AJAX 요청 보내기
    $.ajax({
        url: '/ajax/updateAutoSearchStatus',
        type: 'POST',
        data: { isEnabled: isAutoSearchEnabled, intervalDays: intervalDays },
        success: function(response) {
            $('#autoSearchStatus').text(response);
        },
        error: function(error) {
            console.log("Error:", error);
        }
    });
}

function setRecurringSearch(days) {
    if (days > 0) {
       const intervalMilliseconds = days * 24 * 60 * 60 * 1000; // 일 단위 간격을 밀리초로 변환
        autoSearchTimeoutId = setTimeout(() => {
            searchChecked();
            setRecurringSearch(days);
        }, intervalMilliseconds);
    }
}

function disableAutoSearch() {
    if (autoSearchTimeoutId) {
        clearTimeout(autoSearchTimeoutId);
        autoSearchTimeoutId = null;
    }

    // AJAX 요청 보내기
    $.ajax({
        url: '/ajax/updateAutoSearchStatus',
        type: 'POST',
        data: { isEnabled: false, intervalDays: 0 },
        success: function(response) {
            $('#autoSearchStatus').text(response);
        },
        error: function(error) {
            console.log("Error:", error);
        }
    });
}

// 자동 검색 토글 이벤트 핸들러 추가
$('#autoSearchToggle').change(function() {
    if ($(this).is(':checked')) {
        setAutoSearch();
    } else if (autoSearchTimeoutId) {
        clearTimeout(autoSearchTimeoutId);
        autoSearchTimeoutId = null; 
        disableAutoSearch();
    }
});

// 조회 결과 페이지에서 사용할 함수들

// 재검색 함수
function retrySearch() {
    console.log("Retry search called");
    window.location.href = "/crawling/beforeSearch";
}

// 오름차순 정렬 함수
function sortAsc() {
    const rows = $('#searchedDataTable tbody tr').get();
    rows.sort(function(a, b) {
        const A = $(a).children('td').eq(1).text().toUpperCase();
        const B = $(b).children('td').eq(1).text().toUpperCase();

        if (A < B) {
            return -1;
        }
        if (A > B) {
            return 1;
        }
        return 0;
    });

    $.each(rows, function(index, row) {
        $('#searchedDataTable').children('tbody').append(row);
    });
}

// 내림차순 정렬 함수
function sortDesc() {
    const rows = $('#searchedDataTable tbody tr').get();
    rows.sort(function(a, b) {
        const A = $(a).children('td').eq(1).text().toUpperCase();
        const B = $(b).children('td').eq(1).text().toUpperCase();

        if (A < B) {
            return 1;
        }
        if (A > B) {
            return -1;
        }
        return 0;
    });

    $.each(rows, function(index, row) {
        $('#searchedDataTable').children('tbody').append(row);
    });
}

// 검색 필터 적용 함수
function applySearchFilter() {
    const searchValue = $('#searchBox').val().toLowerCase();
    $('#searchedDataTable tbody tr').filter(function() {
        $(this).toggle($(this).text().toLowerCase().indexOf(searchValue) > -1)
    });
}

function searchPast() {
    var searchDate = document.getElementById("searchDate").value;

    if (!searchDate) {
        alert("날짜를 선택해주세요.");
        return;
    }

    var formattedDate = searchDate.replace(/-/g, '');

    $.ajax({
        url: '/ajax/searchByDate',
        type: 'GET',
        data: {
            date: formattedDate
        },
        success: function(response) {
            var tableBody = $("#pastDataTable tbody");
            tableBody.empty();

            response.forEach(function(item) {
                var formattedPrice = Number(item.price).toLocaleString() + "원";
                var row = `<tr>
                    <td>${item.date}</td>
                    <td>${item.name}</td>
                    <td>${formattedPrice}</td>
                    <td><a href="${item.link}" target="_blank">링크</a></td>
                </tr>`;
                tableBody.append(row);
            });
        },
        error: function() {
            alert("데이터를 가져오는데 실패했습니다.");
        }
    });
}

function filterData() {
    var keyword = document.getElementById("pastSearchKeyword").value.toLowerCase();
    var tableBody = $("#pastDataTable tbody");
    var rows = tableBody.find("tr");

    rows.each(function() {
        var row = $(this);
        var productName = row.find("td:nth-child(2)").text().toLowerCase();

        if (productName.includes(keyword)) {
            row.show();
        } else {
            row.hide();
        }
    });
}
// 여기부분은 자유검색 시 유효성을 검사하는 부분
function clearInput() {
            document.getElementById('productCode').value = '';
}

function validateForm(event) {
    event.preventDefault(); // 폼 제출을 막음

    const searchType = document.querySelector('input[name="searchType"]:checked');
    const productCode = document.getElementById('productCode').value.trim();

    if (!searchType) {
        alert("쇼핑몰을 지정해주세요");
        return;
    }

    if (!productCode) {
        alert("검색어가 없습니다");
        return;
    }

    document.getElementById('mainForm').submit(); // 폼 제출
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('mainForm').addEventListener('submit', validateForm);
});
// 3자리마다 컴마를 표시하고 원을 붙임
document.addEventListener("DOMContentLoaded", function() {
    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    document.querySelectorAll('.price').forEach(function(element) {
        const originalPrice = element.getAttribute('data-price');
        element.textContent = formatNumber(originalPrice);
    });

    document.querySelectorAll('.searchLimit').forEach(function(element) {
        const originalLimit = element.getAttribute('data-limit');
        element.textContent = formatNumber(originalLimit);
    });
});

// 이 부분은 결과분석에 대해서 검색과 전체보기 기능이 구현된것임
document.getElementById('searchCompetitorButton').addEventListener('click', function() {
    var searchQuery = document.getElementById('searchCompetitorBox').value.toLowerCase();
    var rows = document.querySelectorAll('#competitorTable tbody tr');
    rows.forEach(function(row) {
        var productName = row.querySelector('td').textContent.toLowerCase();
        if (productName.includes(searchQuery)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
});

document.getElementById('showAllButton').addEventListener('click', function() {
    var rows = document.querySelectorAll('#competitorTable tbody tr');
    rows.forEach(function(row) {
        row.style.display = '';
    });
});

//좌측 검은선의 높이에 우측 검은선의 높이를 맞추는 코드
document.addEventListener("DOMContentLoaded", function() {
    // 좌측과 우측 divider 요소 가져오기
    var leftDivider = document.querySelector(".left .divider");
    var rightDivider = document.querySelector(".right .divider");

    // 좌측 divider의 위치를 기준으로 우측 divider의 위치 조정
    function adjustDividerHeight() {
        var leftDividerTop = leftDivider.getBoundingClientRect().top;
        var rightContainerTop = document.querySelector(".right").getBoundingClientRect().top;

        // 우측 divider의 위치를 좌측 divider의 위치와 맞추기 위해 상단 여백 조정
        rightDivider.style.marginTop = (leftDividerTop - rightContainerTop) + 'px';
    }

    // 페이지 로드 시 초기 조정
    adjustDividerHeight();

    // 윈도우 크기 조정 시 재조정
    window.addEventListener("resize", adjustDividerHeight);
});



$(document).ready(function() {
	
 	// 모달 닫기 버튼에 이벤트 리스너 추가
    $('#closeModalButton').on('click', function () {
        $('#sellerDetailModal').modal('hide');
        $('.modal-backdrop').remove();
    });

    $('#closeModalButtonFooter').on('click', function () {
        $('#sellerDetailModal').modal('hide');
        $('.modal-backdrop').remove();
    });

    // 모달이 닫힐 때 백드롭을 제거하는 이벤트 추가
    $('#sellerDetailModal').on('hidden.bs.modal', function () {
        $('.modal-backdrop').remove();
    });
	
	$('#sellerDetailModal').modal('hide'); // 페이지 로드 시 모달을 숨깁니다.
	// 테이블 전체보기 기능
	originalRows = $('#dataTable tbody tr').clone();
	checkTableRows();
	
	//도움말 아이콘 기능
    $('#helpIcon').hover(
        function() {
            $('#helpTooltip').css({
                display: 'block',
                top: $(this).position().top + 'px',
                left: ($(this).position().left + $(this).width() + 10) + 'px'
            });
        },
        function() {
            $('#helpTooltip').css('display', 'none');
        }
    );
	
	// 테이블 크기를 유지하는 기능
	const table = document.getElementById('searchedDataTable');
    const tableBody = document.getElementById('tableBody');
    const rowCount = tableBody.getElementsByTagName('tr').length;
    const minRows = 10;

    if (rowCount > 0) {
        table.style.display = 'table';
        for (let i = rowCount; i < minRows; i++) {
            const dummyRow = document.createElement('tr');
            dummyRow.innerHTML = `
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            `;
            tableBody.appendChild(dummyRow);
        }
    }
	
    var autoSearchEnabled = window.thymeleafData.autoSearchEnabled;
    var intervalDays = window.thymeleafData.autoSearchInterval;
    var searchType = window.thymeleafData.searchType;
    var selectedIndices = window.thymeleafData.indices;

    // 검색 타입 설정
    if (searchType === 'all') {
        $('#allSearch').prop('checked', true);
    } else if (searchType === 'naver') {
        $('#naverSearch').prop('checked', true);
    } else if (searchType === 'coupang') {
        $('#coupangSearch').prop('checked', true);
    }

    // 선택된 인덱스 설정
    selectedIndices.forEach(function(index) {
        $('td.clickable').filter(function() {
            return $(this).text() == index;
        }).closest('tr').find('.rowCheckbox').prop('checked', true);
    });

    // 자동 감시 토글 및 주기 설정
    if (autoSearchEnabled) {
        $('#autoSearchToggle').prop('checked', true);
    }

    $('#autoSearchInterval').val(intervalDays);

    // 기존 스크립트 부분
    allRows = $('#dataTable tbody tr').clone();
    $('#mainForm').on('submit', function(event) {
        if (!$('input[name="searchType"]:checked').val()) {
            alert("탐색방법을 선택해주세요");
            event.preventDefault(); // 폼 제출을 막음
            return false;
        }

        if (!$('#productCode').val().trim()) {
            alert("검색어가 없습니다");
            event.preventDefault(); // 폼 제출을 막음
            return false;
        }
    });

    // 초기 자동 검색 설정
    if ($('#autoSearchToggle').is(':checked')) {
        setAutoSearch();
    }

    // afterSearch.html 관련 함수들 바인딩
    $('#retrySearchButton').click(retrySearch);
    $('#sortAscButton').click(sortAsc);
    $('#sortDescButton').click(sortDesc);
    $('#searchButton').click(applySearchFilter);
});


// 전역변수 선언
let isAutoSearchEnabled = false;
let intervalDays = 1;

//여기서부터 등록된 제품에 대한 검색기능
let allRows = [];
// 검색 기능
function filterByName() {
    let keyword = $('#searchKeyword').val().toLowerCase();
    $('#dataTable tbody tr').each(function() {
        $(this).toggle($(this).find('.product-name').text().toLowerCase().indexOf(keyword) > -1);
    });
}

// 전체보기 기능
function showAll() {
    $('#searchKeyword').val('');
    $('#dataTable tbody').empty().append(allRows.clone());
}

// 등록제품에 대한 검색과 전체보기 기능 종료
//여기서부터 페이징 구현 시작
let currentPage = 1;
const rowsPerPage = 10;
let totalRows = document.querySelectorAll('#searchedDataTable tbody tr').length;
let totalPages = Math.ceil(totalRows / rowsPerPage);

function displayRows() {
    let rows = document.querySelectorAll('#searchedDataTable tbody tr');
    rows.forEach((row, index) => {
        row.style.display = (index >= (currentPage - 1) * rowsPerPage && index < currentPage * rowsPerPage) ? '' : 'none';
    });
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

// 초기 로딩 시 첫 페이지 표시
document.addEventListener('DOMContentLoaded', function() {
    displayRows();
});

// 페이징 구현종료

// 입력값을 지우는 함수
function clearInput() {
    $("#productCode").val("");
}

// 전체 선택 체크박스를 클릭했을 때의 동작
function selectAllCheckboxes(source) {
    $('.rowCheckbox').prop('checked', source.checked);
}

// 데이터 추가 함수
function addRow() {
    console.log("addRow called");
    const modal = new bootstrap.Modal(document.getElementById('productModal'), {
        backdrop: 'static',
        keyboard: false
    });
    modal.show();

    const confirmAdd = document.getElementById("confirmAdd");

    // 사용자가 확인 버튼을 클릭하면 입력값을 처리
    confirmAdd.onclick = function() {
        const productName = document.getElementById("productName").value;
        const productPrice = document.getElementById("productPrice").value;
        const searchLimit = document.getElementById("searchLimit").value;
        const competitor1Product = document.getElementById("competitor1Product").value;
        const competitor1Name = document.getElementById("competitor1Name").value;
        const competitor2Product = document.getElementById("competitor2Product").value;
        const competitor2Name = document.getElementById("competitor2Name").value;

        // 가격과 검색 한도가 숫자인지 확인
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
                    // 가격과 검색 한도를 형식화
                    const formattedPrice = response.price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") + " 원";
                    const formattedSearchLimit = response.searchLimit.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") + " 원";

                    const newRow = `
                        <tr>
                            <td><input type="checkbox" class="rowCheckbox"></td>
                            <td class="clickable">${response.index}</td>
                            <td class="clickable product-name">${response.name}</td>
                            <td class="clickable">${formattedPrice}</td>
                            <td class="clickable">${formattedSearchLimit}</td>
                            <td class="clickable competitor-column">${response.competitor1Product}</td>
                            <td class="clickable competitor-column">${response.competitor1Name}</td>
                            <td class="clickable competitor-column">${response.competitor2Product}</td>
                            <td class="clickable competitor-column">${response.competitor2Name}</td>
                        </tr>
                    `;
                    $('#dataTable tbody').append(newRow);

                    // 전체 데이터 배열도 업데이트
                    allRows = $('#dataTable tbody tr').clone();

                    modal.hide(); // 성공적으로 추가된 후 모달 닫기
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
            price: cells.eq(3).find('.price').data('price'),
            searchLimit: cells.eq(4).find('.searchLimit').data('limit'),
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
	// 팝업 표시
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


// 자동 검색 기능 설정
let autoSearchTimeoutId;

function calculateNextTimeout() {
    const now = new Date();
    const nextRun = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 17, 24, 0, 0); // 다음 13:08

    if (now > nextRun) {
        nextRun.setDate(nextRun.getDate() + 1); // 오늘 13:08이 이미 지났다면 다음 날로 설정
    }

    return nextRun - now; // 밀리초 단위 시간 차이 반환
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
        autoSearchTimeoutId = setTimeout(() => {
            searchChecked();
            setRecurringSearch(intervalDays);
        }, timeout);

        console.log(`자동 검색이 ${timeout / 1000}초 후에 실행됩니다.`);
    } else if (autoSearchTimeoutId) {
        clearTimeout(autoSearchTimeoutId);
        autoSearchTimeoutId = null; 
    }
}

function setRecurringSearch(days) {
    if (days > 0) {
       //const intervalMilliseconds = days * 24 * 60 * 60 * 1000; // 일 단위 간격을 밀리초로 변환
       const intervalMilliseconds = days *1000*180; // 테스트용

        autoSearchTimeoutId = setTimeout(() => {
            searchChecked();
            setRecurringSearch(days);
        }, intervalMilliseconds);
    }
}

// 자동 검색 토글 이벤트 핸들러 추가
$('#autoSearchToggle').change(function() {
    if ($(this).is(':checked')) {
        setAutoSearch();
    } else if (autoSearchTimeoutId) {
        clearTimeout(autoSearchTimeoutId);
        autoSearchTimeoutId = null; 
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
                var row = `<tr>
                    <td>${item.date}</td>
                    <td>${item.name}</td>
                    <td>${item.price}</td>
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


// 로딩이 완료된 후 메시지 출력 및 자동 검색 설정
$(document).ready(function() {
    console.log("Document is ready");
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

    // Chart.js를 사용하여 막대 그래프 생성
    if (percentArray && percentArray.length > 0) {
        var labels = percentArray.map(item => item.shoppingMall);
        var data = percentArray.map(item => item.percent);

        var ctx = document.getElementById('percentChart').getContext('2d');
        var percentChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: '위반 비율 (%)',
                    data: data,
                    backgroundColor: 'rgba(34, 139, 34, 0.7)', // 짙은 초록색
                    borderColor: 'rgba(34, 139, 34, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100
                    },
                    x: {
                        ticks: {
                            color: 'black', // 선명한 검정색
                            font: {
                                weight: 'bold' // 굵은 글씨
                            }
                        }
                    }
                }
            }
        });
    } else {
        console.log("No data available for chart.");
    }

    if (competitorResult && competitorResult.length > 0) {
        var labels = competitorResult.map(item => item.productName + ', ' + item.shoppingMall);
        var data = competitorResult.map(item => item.averagePrice);

        var ctx = document.getElementById('competitorChart').getContext('2d');
        var competitorChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: '경쟁사 제품 사이트별 평균 가격',
                    data: data,
                    backgroundColor: 'rgba(70, 130, 180, 0.7)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    } else {
        console.log("No data available for competitor chart.");
    }
});


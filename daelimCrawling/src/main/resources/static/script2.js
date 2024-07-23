let isEditing = false;

function toggleEdit() {
    const editButton = document.getElementById("editButton");
    isEditing = !isEditing;
    if (isEditing) {
        editButton.textContent = "수정완료";
        makeTableEditable();
    } else {
        editButton.textContent = "데이터 수정";
        saveEdits();
    }
}

function makeTableEditable() {
    document.querySelectorAll("#dataTable tbody tr").forEach(row => {
        if (row.querySelector(".rowCheckbox").checked) {
            row.querySelectorAll("td.clickable:not(.price-column):not(.search-limit-column)").forEach(cell => {
                cell.setAttribute("contenteditable", "true");
                cell.classList.add("editable");
            });

            row.querySelectorAll("td.price-column, td.search-limit-column").forEach(cell => {
                const span = cell.querySelector("span");
                const textContent = span.textContent.replace(/,/g, '');
                span.setAttribute("contenteditable", "true");
                span.textContent = textContent;
                span.classList.add("editable");
            });
        }
    });
}

function saveEdits() {
    const updatedData = [];
    document.querySelectorAll("#dataTable tbody tr").forEach(row => {
        if (row.querySelector(".rowCheckbox").checked) {
            const cells = row.querySelectorAll("td.clickable");
            const priceCell = row.querySelector("td.price-column span");
            const searchLimitCell = row.querySelector("td.search-limit-column span");

            const updatedRow = {
                index: parseInt(cells[0].textContent.trim()),
                name: cells[1].textContent.trim(),
                price: parseInt(priceCell.textContent.replace(/,/g, '').trim()),
                searchLimit: parseInt(searchLimitCell.textContent.replace(/,/g, '').trim()),
                competitor1Product: cells[4].textContent.trim(),
                competitor1Name: cells[5].textContent.trim(),
                competitor2Product: cells[6].textContent.trim(),
                competitor2Name: cells[7].textContent.trim()
            };
            updatedData.push(updatedRow);

            cells.forEach(cell => {
                cell.removeAttribute("contenteditable");
                cell.classList.remove("editable");
            });

            priceCell.removeAttribute("contenteditable");
            priceCell.classList.remove("editable");
            priceCell.textContent = new Intl.NumberFormat().format(updatedRow.price);

            searchLimitCell.removeAttribute("contenteditable");
            searchLimitCell.classList.remove("editable");
            searchLimitCell.textContent = new Intl.NumberFormat().format(updatedRow.searchLimit);
        }
    });

    $.ajax({
        url: '/ajax/updateProducts',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(updatedData),
        success: function (response) {
            // 필요한 경우 화면 갱신 등의 후속 작업 수행
        },
        error: function (xhr, status, error) {
        }
    });
}



//제품등록 시 3자리마다 콤마 표시
document.addEventListener('DOMContentLoaded', function() {
    const productPriceInput = document.getElementById('productPrice');
    const searchLimitInput = document.getElementById('searchLimit');

    function formatNumberWithCommas(value) {
        return value.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    function handleInput(event) {
        let value = event.target.value.replace(/,/g, '');
        if (!isNaN(value) && value !== '') {
            value = formatNumberWithCommas(value);
            event.target.value = value;
        }
    }

    productPriceInput.addEventListener('keyup', handleInput);
    searchLimitInput.addEventListener('keyup', handleInput);
});

// 마이페이지 관련 설정
document.addEventListener("DOMContentLoaded", function() {
	
	
    const editUserIdButton = document.getElementById('editUserIdButton');
    const userIdInput = document.getElementById('userId');
    const editPasswordButton = document.getElementById('editPasswordButton');
    const passwordInput = document.getElementById('password');
    const logoutButton = document.getElementById('logoutButton');

    editUserIdButton.addEventListener('click', function() {
        if (editUserIdButton.textContent === '아이디 변경') {
            userIdInput.removeAttribute('readonly');
            editUserIdButton.textContent = '변경완료';
        } else {
            const newUserId = userIdInput.value;
            if (validateEmail(newUserId)) {
                userIdInput.setAttribute('readonly', 'readonly');
                editUserIdButton.textContent = '아이디 변경';
                // AJAX 요청으로 아이디 변경 로직 추가
                updateUserDetails({ userId: newUserId });
            } else {
                alert('유효한 이메일 형식의 아이디를 입력하세요.');
            }
        }
    });

    editPasswordButton.addEventListener('click', function() {
        if (editPasswordButton.textContent === '비밀번호 변경') {
            passwordInput.removeAttribute('readonly');
            editPasswordButton.textContent = '변경완료';
        } else {
            const newPassword = passwordInput.value;
            passwordInput.setAttribute('readonly', 'readonly');
            editPasswordButton.textContent = '비밀번호 변경';
            // AJAX 요청으로 비밀번호 변경 로직 추가
            updateUserDetails({ password: newPassword });
        }
    });

    logoutButton.addEventListener('click', function() {
	    var searchType = document.querySelector('input[name="searchType"]:checked') ? document.querySelector('input[name="searchType"]:checked').value : "";
	    var selectedItems = [];
	    
	    document.querySelectorAll('.rowCheckbox:checked').forEach(function(checkbox) {
	        selectedItems.push(checkbox.closest('tr').querySelector('.product-name').textContent.trim());
	    });
	
	    // 빈 selectedItems라도 서버로 전송
	    $.ajax({
	        url: '/save-settings',
	        type: 'GET',
	        data: {
	            searchType: searchType,
	            selectedItems: JSON.stringify(selectedItems)
	        },
	        success: function(response) {
	            window.location.href = '/logout';
	        },
	        error: function(error) {
	            console.error('Error saving settings:', error);
	            window.location.href = '/logout'; // 에러가 발생해도 로그아웃 진행
	        }
	    });
	});


    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    function updateUserDetails(details) {
        fetch('/updateUser', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(details),
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('변경이 완료되었습니다.');
            } else {
            }
        })
        .catch((error) => {
            console.error('Error:', error);
        });
    }
});


$(document).ready(function() {
    // 비데 리스트와 수전 리스트를 서버에서 받아온다.
    var bidetList = window.thymeleafData.bidetList.map(item => item.name);
    var faucetList = window.thymeleafData.faucetList.map(item => item.name);

    // jQuery UI Autocomplete 적용 함수
    function setAutocompleteSource(category) {
        if (category === "비데") {
            $("#productName").autocomplete({
                source: bidetList,
                minLength: 1,
                appendTo: "#productModal .modal-body"
            });
        } else if (category === "수전") {
            $("#productName").autocomplete({
                source: faucetList,
                minLength: 1,
                appendTo: "#productModal .modal-body"
            });
        } else {
            $("#productName").autocomplete({
                source: [],
                minLength: 1,
                appendTo: "#productModal .modal-body"
            });
        }
    }

    // 카테고리 선택 변경 이벤트
    $("#categorySelect").change(function() {
        var selectedCategory = $(this).val();
        setAutocompleteSource(selectedCategory);
    });

    // 페이지 로드 시 초기 상태 설정
    setAutocompleteSource("");
    
     // 사용자 설정 로드
    var username = /*[[${username}]]*/ '';

    $.ajax({
        url: '/get-settings',
        type: 'GET',
        data: { username: username },
        success: function(response) {
            if (response) {
                var settings = response;
                if (settings.searchType) {
                    $('input[name="searchType"][value="' + settings.searchType + '"]').prop('checked', true);
                }
                
                if (settings.selectedItems) {
                    var selectedItems = JSON.parse(settings.selectedItems);
                    selectedItems.forEach(function(item) {
                        $('#dataTable tbody tr').each(function() {
                            if ($(this).find('.product-name').text().trim() === item) {
                                $(this).find('.rowCheckbox').prop('checked', true);
                            }
                        });
                    });
                }
            }
        },
        error: function(error) {
            console.error('Error loading settings:', error);
        }
    });
    
});


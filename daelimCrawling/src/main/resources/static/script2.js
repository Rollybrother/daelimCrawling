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
            alert("수정에 실패했습니다.");
        }
    });
}


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
});

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

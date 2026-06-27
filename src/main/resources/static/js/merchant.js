let modal;
let currentPage = 0;
const pageSize = 10;

document.addEventListener("DOMContentLoaded", () => {

    modal = new bootstrap.Modal(document.getElementById("merchantModal"));

    loadMerchants();

});

async function loadMerchants() {
    const merchantName = document.getElementById("searchMerchantName").value;
    const email = document.getElementById("searchMerchantEmail").value;

    const params = new URLSearchParams();

    if (merchantName) params.append("name", merchantName);
    if (email) params.append("email", email);

    params.append("page", currentPage);
    params.append("size", pageSize);
    params.append("sortBy", "createdAt");
    params.append("direction", "desc");

    const response = await api(`/api/merchants?${params.toString()}`);

    const page = await response.json();


    const tbody = document.getElementById("merchantTable");

    tbody.innerHTML = "";

    page.content.forEach(m => {

        tbody.innerHTML += `
        <tr>

            <td>${m.merchantName}</td>
            <td>${m.merchantCode}</td>
            <td>${m.email}</td>
            <td>${m.status}</td>
            <td>${m.createdAt}</td>

            <td>

                <button class="btn btn-warning btn-sm"
                    onclick="editMerchant('${m.id}')">
                     <i class="bi bi-pencil"></i>
                </button>

            </td>

        </tr>
        `;

    });

    document.getElementById("pageInfo").innerHTML =
        `Page ${page.number + 1} of ${page.totalPages}`;

}

function openCreateModal(){

    merchantId.value="";
    merchantName.value="";
    merchantCode.value="";
    email.value="";
    mstatus.value="ACTIVE";

    modal.show();

}

async function editMerchant(id){

    const response = await api("/api/merchants/" + id);

    const m = await response.json();

    merchantId.value = m.id;
    merchantCode.value = m.merchantCode;
    merchantName.value = m.merchantName;
    email.value = m.email;
    mstatus.value = m.status;

    modal.show();

}

async function saveMerchant(){

    const id = merchantId.value;

    const merchant = {

        merchantCode:merchantCode.value,
        merchantName:merchantName.value,
        email:email.value,
        status:mstatus.value

    };

    if(id){

        await api("/api/merchants/" + id,{

            method:"PUT",

            headers:{
                "Content-Type":"application/json"
            },

            body:JSON.stringify(merchant)

        });

    }else{

        await api("/api/merchants",{

            method:"POST",

            headers:{
                "Content-Type":"application/json"
            },

            body:JSON.stringify(merchant)

        });

    }

    modal.hide();

    loadMerchants();

}

function nextPage() {
    currentPage++;
    loadMerchants();
}

function previousPage() {

    if (currentPage > 0) {
        currentPage--;
        loadMerchants();
    }

}

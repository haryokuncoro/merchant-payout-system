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

    page.data.content.forEach(m => {

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
                     Edit
                </button>

            </td>

        </tr>
        `;

    });

    document.getElementById("pageInfo").innerHTML =
        `Page ${page.data.number + 1} of ${page.data.totalPages}`;

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
    const data = m.data

    merchantId.value = data.id;
    merchantCode.value = data.merchantCode;
    merchantName.value = data.merchantName;
    email.value = data.email;
    mstatus.value = data.status;

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

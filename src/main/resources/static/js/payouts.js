let currentPage = 0;
const pageSize = 10;
let modal;

document.addEventListener("DOMContentLoaded", () => {

    modal = new bootstrap.Modal(document.getElementById("payoutModal"));


});



async function loadPayouts() {

    const merchantId = document.getElementById("merchantId").value;
    const payoutStatus = document.getElementById("payoutStatus").value;

    const params = new URLSearchParams();

    if (merchantId)
        params.append("merchantId", merchantId);
    if (payoutStatus)
        params.append("status", payoutStatus);

    params.append("page", currentPage);
    params.append("size", pageSize);
    params.append("sortBy", "createdAt");
    params.append("direction", "desc");

    const response = await api(`/api/payouts?${params}`);
    const page = await response.json();

    renderTable(page);

}

function renderTable(page) {

    const tbody = document.getElementById("payoutTable");

    tbody.innerHTML = "";

    page.data.content.forEach(payout => {

        tbody.innerHTML += `
            <tr>
                <td>${payout.merchantName}</td>
                <td>${payout.periodStart} - ${payout.periodEnd}</td>
                <td>${formatCurrency(payout.grossAmount, payout.currency)}</td>
                <td>${formatCurrency(payout.feeAmount, payout.currency)}</td>
                <td>${formatCurrency(payout.payoutAmount, payout.currency)}</td>
                <td>${payout.stripePayoutId}</td>
                <td>${badge(payout.status)}</td>
                <td>${formatDate(payout.payoutDate)}</td>
            </tr>
        `;

    });

    document.getElementById("pageInfo").innerHTML =
        `Page ${page.data.number + 1} of ${page.data.totalPages}`;

}

function openPayoutModal(){

    modal.show();

}

async function triggerPayout(){
    const payout = {

        merchantId:merchantIdModal.value,
        periodStart:periodStart.value,
        periodEnd:periodEnd.value

    };


    await api("/api/payouts",{

        method:"POST",

        headers:{
            "Content-Type":"application/json"
        },

        body:JSON.stringify(payout)

    });

    modal.hide()
    await loadPayouts()
}

function badge(status) {

    let cls = "secondary";

    if (status === "PAID")
        cls = "success";
    else if (status === "PENDING")
        cls = "warning";
    else if (status === "FAILED")
        cls = "danger";

    return `<span class="badge bg-${cls}">${status}</span>`;

}

function formatCurrency(amount, currency) {

    return new Intl.NumberFormat("id-ID", {
        style: "currency",
        currency: currency
    }).format(amount);

}

function formatDate(date) {

    return new Date(date).toLocaleString();

}

function nextPage() {

    currentPage++;
    loadPayouts();

}

function previousPage() {

    if (currentPage > 0) {
        currentPage--;
        loadPayouts();
    }

}

window.onload = async () => {

    await loadMerchants("merchantId");
    await loadMerchants("merchantIdModal");
    await loadPayouts();

};
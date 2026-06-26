let currentPage = 0;
const pageSize = 10;

async function loadMerchants() {

    const response = await api("/api/merchants");
    if(response.status == 403) {
        location.href="/login";
    }
    const page = await response.json();

    const select = document.getElementById("merchantId");

    select.innerHTML = '<option value="">All Merchants</option>';

    page.content.forEach(m => {

        select.innerHTML += `
            <option value="${m.id}">
                ${m.merchantName}
            </option>
        `;
    });

}

async function loadPayouts() {

    const merchantId = document.getElementById("merchantId").value;

    const params = new URLSearchParams();

    if (merchantId)
        params.append("merchantId", merchantId);

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

    page.content.forEach(payout => {

        tbody.innerHTML += `
            <tr>
                <td>${payout.merchantName}</td>
                <td>${payout.periodStart} - ${payout.periodEnd}</td>
                <td>${formatCurrency(payout.grossAmount)}</td>
                <td>${formatCurrency(payout.feeAmount)}</td>
                <td>${formatCurrency(payout.payoutAmount)}</td>
                <td>${payout.stripePayoutId}</td>
                <td>${badge(payout.status)}</td>
                <td>${formatDate(payout.payoutDate)}</td>
            </tr>
        `;

    });

    document.getElementById("pageInfo").innerHTML =
        `Page ${page.number + 1} of ${page.totalPages}`;

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

function formatCurrency(amount) {

    return new Intl.NumberFormat("id-ID", {
        style: "currency",
        currency: "IDR"
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

    await loadMerchants();
    await loadPayouts();

};
let currentPage = 0;
const pageSize = 10;

async function loadOrders() {

    const merchantId = document.getElementById("merchantId").value;
    const orderNo = document.getElementById("orderNo").value;
    const paymentIntent = document.getElementById("paymentIntent").value;

    const params = new URLSearchParams();

    if (merchantId) params.append("merchantId", merchantId);
    if (orderNo) params.append("orderNo", orderNo);
    if (paymentIntent) params.append("stripePaymentIntentId", paymentIntent);

    params.append("page", currentPage);
    params.append("size", pageSize);
    params.append("sortBy", "createdAt");
    params.append("direction", "desc");

    const response = await api(`/api/orders?${params.toString()}`);

    const data = await response.json();

    renderTable(data);
}

async function loadMerchants() {

    try {
        const response = await api("/api/merchants");

        if (!response.ok) {
            throw new Error("Failed to load merchants");
        }

        const merchants = await response.json();

        const select = document.getElementById("merchantId");

        select.innerHTML = `<option value="">All Merchants</option>`;


        merchants.data.content.forEach(m => {
            select.innerHTML += `
                <option value="${m.id}">
                    ${m.merchantName}
                </option>
            `;
        });

    } catch (e) {
        console.error(e);
    }

}

function renderTable(page) {

    const tbody = document.getElementById("orderTable");

    tbody.innerHTML = "";

    page.data.content.forEach(order => {

        tbody.innerHTML += `
            <tr>
                <td>${order.orderNo}</td>
                <td>${order.merchantName ?? ""}</td>
                <td>${order.amount}</td>
                <td>${order.paymentStatus}</td>
                <td>${order.paymentIntentId ?? ""}</td>
                <td>${order.paidAt}</td>
            </tr>
        `;
    });

    document.getElementById("pageInfo").innerHTML =
        `Page ${page.data.number + 1} of ${page.data.totalPages}`;
}

function nextPage() {
    currentPage++;
    loadOrders();
}

function previousPage() {

    if (currentPage > 0) {
        currentPage--;
        loadOrders();
    }

}

window.onload = async () => {
    await loadMerchants();
    await loadOrders();
};
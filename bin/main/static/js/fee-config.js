let currentPage = 0;

document.getElementById("searchBtn").onclick = loadData;
document.getElementById("saveBtn").onclick = save;
document.getElementById("clearBtn").onclick = clearForm;

async function loadData() {

    const merchantId = document.getElementById("searchMerchantId").value;
    const active = document.getElementById("searchActive").value;

    let url = `/api/fee-configs?page=${currentPage}&size=10`;

    if (merchantId)
        url += `&merchantId=${merchantId}`;

    if (active !== "")
        url += `&active=${active}`;

    const response = await api(url);

    const page = await response.json();

    let html = "";

    page.data.content.forEach(item => {

        html += `
        <tr>
            <td>${item.merchantName}</td>
            <td>${item.feeType}</td>
            <td>${item.feeValue}</td>
            <td>${item.effectiveFrom}</td>
            <td>${item.effectiveTo}</td>
            <td>${item.active}</td>
            <td>
                <button class="btn btn-warning btn-sm"
                        onclick="edit('${item.id}')">
                    Edit
                </button>
            </td>
        </tr>`;
    });

    document.getElementById("tableBody").innerHTML = html;
}

async function edit(id) {

    const response = await api(`/api/fee-configs/${id}`);
    const json = await response.json()
    const d = json.data


    document.getElementById("feeId").value = d.id;
    document.getElementById("merchantId").value = d.merchantId;
    document.getElementById("merchantName").value = d.merchantName;
    document.getElementById("feeType").value = d.feeType;
    document.getElementById("feeValue").value = d.feeValue;
    document.getElementById("effectiveFrom").value = new Date(d.effectiveFrom).toISOString().split("T")[0];
    document.getElementById("effectiveTo").value = d.effectiveTo == null ? null : new Date(d.effectiveTo).toISOString().split("T")[0];
    document.getElementById("active").value = d.active;
}

async function save() {

    const id = document.getElementById("feeId").value;
    const effectiveFromVal = new Date(document.getElementById("effectiveFrom").value).toISOString();
    let effectiveToVal = document.getElementById("effectiveTo").value || null;
    if (effectiveToVal) {
        effectiveToVal = new Date(effectiveToVal).toISOString();
    }else{}
    const body = {
        merchantId: document.getElementById("merchantId").value,
        feeType: document.getElementById("feeType").value,
        feeValue: Number(document.getElementById("feeValue").value),
        effectiveFrom: effectiveFromVal,
        effectiveTo: effectiveToVal,
        active: document.getElementById("active").value === "true"
    };

    if (id) {
        await api(`/api/fee-configs/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });
    } else {
        await api(`/api/fee-configs`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });
    }

    clearForm();
    loadData();
}

function clearForm() {

    document.getElementById("feeId").value = "";
    document.getElementById("merchantId").value = "";
    document.getElementById("merchantName").value = "";
    document.getElementById("feeType").value = "";
    document.getElementById("feeValue").value = "";
    document.getElementById("effectiveFrom").value = "";
    document.getElementById("effectiveTo").value = "";
    document.getElementById("active").value = "true";
}

window.onload = async () => {
    await loadMerchants("searchMerchantId");
    await loadData();
};
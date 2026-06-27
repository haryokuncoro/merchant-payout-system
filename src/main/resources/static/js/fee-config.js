let currentPage = 0;

loadData();

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

                <button class="btn btn-danger btn-sm"
                        onclick="remove('${item.id}')">
                    Delete
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

    const body = {
        merchantId: document.getElementById("merchantId").value,
        feeType: document.getElementById("feeType").value,
        feeValue: Number(document.getElementById("feeValue").value),
        effectiveFrom: Number(document.getElementById("effectiveFrom").value),
        effectiveTo: Number(document.getElementById("effectiveTo").value),
        active: document.getElementById("active").value === "true"
    };

    if (id) {
        await api(`/api/fee-configs/${id}`, body);
    } else {
        await api("/api/fee-configs", body);
    }

    clearForm();
    loadData();
}

async function remove(id) {

    if (!confirm("Delete this fee config?"))
        return;

    await api(`/api/fee-configs/${id}`);

    loadData();
}

function clearForm() {

    document.getElementById("feeId").value = "";
    document.getElementById("merchantId").value = "";
    document.getElementById("feeType").value = "";
    document.getElementById("feeValue").value = "";
    document.getElementById("effectiveFrom").value = "";
    document.getElementById("effectiveTo").value = "";
    document.getElementById("active").value = "true";
}
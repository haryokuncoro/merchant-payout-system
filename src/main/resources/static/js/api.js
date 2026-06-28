function getToken() {
    return sessionStorage.getItem("token");
}

async function api(url, options = {}) {

    options.headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${getToken()}`
    };

    const response = await fetch(url, options);

    if (response.status === 401 || response.status == 403) {

        sessionStorage.clear();

        window.location = "/login";

        return;
    }

    return response;
}


async function loadMerchants(elementId) {

    try {
        const response = await api("/api/merchants");

        if (!response.ok) {
            throw new Error("Failed to load merchants");
        }

        const merchants = await response.json();

        const select = document.getElementById(elementId);

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

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
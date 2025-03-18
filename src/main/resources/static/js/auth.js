// Auth Constants
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

async function login(username, password) {
    try {
        // 1. Autenticación inicial
        const response = await fetch(`${API_BASE_URL}/auth/signin`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Login failed');
        }

        const data = await response.json();
        const token = data.accessToken;

        // Guardar el token
        localStorage.setItem(TOKEN_KEY, token);

        // 2. Obtener información del usuario con el token
        const userResponse = await fetch(`${API_BASE_URL}/users/username/${username}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (userResponse.ok) {
            // Si podemos obtener la información del usuario, la guardamos
            const userData = await userResponse.json();
            localStorage.setItem(USER_KEY, JSON.stringify({
                id: userData.id, // El ID numérico que necesitamos
                username: userData.username
            }));

            return userData;
        } else {
            // Si no podemos obtener la información del usuario
            const userInfo = parseJwt(token);
            console.warn('No se pudo obtener información del usuario, usando datos del token');

            localStorage.setItem(USER_KEY, JSON.stringify({
                id: userInfo.sub, // Este puede no ser el ID numérico correcto
                username: username
            }));

            return userInfo;
        }
    } catch (error) {
        console.error('Error during login:', error);
        throw error;
    }
}

async function signup(username, email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Signup failed');
        }

        return await response.json();
    } catch (error) {
        console.error('Error during signup:', error);
        throw error;
    }
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.reload();
}

function isAuthenticated() {
    return !!getAuthToken();
}

function getAuthToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getAuthUser() {
    const userJson = localStorage.getItem(USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
}

function parseJwt(token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
        console.error('Error parsing JWT token:', e);
        return {};
    }
}

function showLoginModal() {
    const loginModal = new bootstrap.Modal(document.getElementById('login-modal'));
    loginModal.show();
}

function showSignupModal() {
    const signupModal = new bootstrap.Modal(document.getElementById('signup-modal'));
    signupModal.show();
}

function updateAuthUI() {
    const isAuth = isAuthenticated();
    const currentUser = getAuthUser();

    const authButtons = document.getElementById('auth-buttons');
    const userInfo = document.getElementById('user-info');
    const usernameDisplay = document.getElementById('username-display');
    const postForm = document.getElementById('post-form');
    const streamForm = document.getElementById('stream-form');

    if (isAuth && currentUser) {
        authButtons.classList.add('d-none');
        userInfo.classList.remove('d-none');
        usernameDisplay.textContent = currentUser.username;

        if (postForm) postForm.classList.remove('d-none');
        if (streamForm) streamForm.classList.remove('d-none');
    } else {
        authButtons.classList.remove('d-none');
        userInfo.classList.add('d-none');

        if (postForm) postForm.classList.add('d-none');
        if (streamForm) streamForm.classList.add('d-none');
    }
}
// API URL
const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    posts: {
        getAll: async (page = 0, size = 10) => {
            try {
                const response = await authFetch(`${API_BASE_URL}/posts?page=${page}&size=${size}`);
                if (!response.ok) throw new Error('Failed to fetch posts');
                return await response.json();
            } catch (error) {
                console.error('Error fetching posts:', error);
                throw error;
            }
        },

        getByStream: async (streamId, page = 0, size = 10) => {
            try {
                const response = await authFetch(`${API_BASE_URL}/posts/stream/${streamId}?page=${page}&size=${size}`);
                if (!response.ok) throw new Error('Failed to fetch posts for stream');
                return await response.json();
            } catch (error) {
                console.error('Error fetching posts by stream:', error);
                throw error;
            }
        },

        create: async (content, streamId) => {
            try {
                const currentUser = getAuthUser();
                if (!currentUser || !currentUser.id) throw new Error('User not authenticated');

                const response = await authFetch(`${API_BASE_URL}/posts/user/${currentUser.id}/stream/${streamId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ content })
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Failed to create post');
                }

                return await response.json();
            } catch (error) {
                console.error('Error creating post:', error);
                throw error;
            }
        }
    },

    streams: {
        getAll: async () => {
            try {
                const response = await authFetch(`${API_BASE_URL}/streams`);
                if (!response.ok) throw new Error('Failed to fetch streams');
                return await response.json();
            } catch (error) {
                console.error('Error fetching streams:', error);
                throw error;
            }
        },

        getById: async (id) => {
            try {
                const response = await authFetch(`${API_BASE_URL}/streams/${id}`);
                if (!response.ok) throw new Error('Failed to fetch stream');
                return await response.json();
            } catch (error) {
                console.error('Error fetching stream:', error);
                throw error;
            }
        },

        create: async (name, description) => {
            try {
                const response = await authFetch(`${API_BASE_URL}/streams`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ name, description })
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Failed to create stream');
                }

                return await response.json();
            } catch (error) {
                console.error('Error creating stream:', error);
                throw error;
            }
        }
    }
};

function authFetch(url, options = {}) {
    const token = getAuthToken();
    if (!token) {
        showLoginModal();
        return Promise.reject(new Error('Authentication required'));
    }

    const authOptions = {
        ...options,
        headers: {
            ...options.headers,
            'Authorization': `Bearer ${token}`
        }
    };

    return fetch(url, authOptions)
        .then(async response => {
            if (!response.ok) {
                const text = await response.text();
                console.error('Error response from server:', text);
                return new Response(text, {
                    status: response.status,
                    statusText: response.statusText,
                    headers: response.headers
                });
            }
            return response;
        });
}
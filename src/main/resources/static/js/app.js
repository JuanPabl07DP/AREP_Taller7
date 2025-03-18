// DOM Elements
const homeLink = document.getElementById('home-link');
const streamsLink = document.getElementById('streams-link');
const loginBtn = document.getElementById('login-btn');
const signupBtn = document.getElementById('signup-btn');
const logoutBtn = document.getElementById('logout-btn');

const homeSection = document.getElementById('home-section');
const streamsSection = document.getElementById('streams-section');

const postContent = document.getElementById('post-content');
const postSubmit = document.getElementById('post-submit');
const charCount = document.getElementById('char-count');
const streamSelect = document.getElementById('stream-select');
const postError = document.getElementById('post-error');

const streamName = document.getElementById('stream-name');
const streamDescription = document.getElementById('stream-description');
const streamSubmit = document.getElementById('stream-submit');
const streamError = document.getElementById('stream-error');

const loginForm = document.getElementById('login-form');
const loginUsername = document.getElementById('login-username');
const loginPassword = document.getElementById('login-password');
const loginError = document.getElementById('login-error');

const signupForm = document.getElementById('signup-form');
const signupUsername = document.getElementById('signup-username');
const signupEmail = document.getElementById('signup-email');
const signupPassword = document.getElementById('signup-password');
const signupError = document.getElementById('signup-error');

document.addEventListener('DOMContentLoaded', () => {
    updateAuthUI();

    if (isAuthenticated()) {
        loadStreams();
        loadPosts();
    } else {
        showLoginModal();
    }

    setupEventListeners();
});

function setupEventListeners() {
    homeLink.addEventListener('click', navigateToHome);
    streamsLink.addEventListener('click', navigateToStreams);

    loginBtn.addEventListener('click', () => showLoginModal());
    signupBtn.addEventListener('click', () => showSignupModal());
    logoutBtn.addEventListener('click', handleLogout);

    if (loginForm) loginForm.addEventListener('submit', handleLogin);
    if (signupForm) signupForm.addEventListener('submit', handleSignup);
    if (postContent) postContent.addEventListener('input', updateCharCount);
    if (postSubmit) postSubmit.addEventListener('click', createPost);
    if (streamSubmit) streamSubmit.addEventListener('click', createStream);
}

function navigateToHome(e) {
    e.preventDefault();
    homeSection.classList.remove('d-none');
    streamsSection.classList.add('d-none');
    homeLink.classList.add('active');
    streamsLink.classList.remove('active');

    if (isAuthenticated()) {
        loadPosts();
    }
}

function navigateToStreams(e) {
    e.preventDefault();
    homeSection.classList.add('d-none');
    streamsSection.classList.remove('d-none');
    homeLink.classList.remove('active');
    streamsLink.classList.add('active');

    if (isAuthenticated()) {
        loadStreams();
    }
}

async function handleLogin(e) {
    e.preventDefault();
    loginError.textContent = '';

    try {
        await login(loginUsername.value, loginPassword.value);

        const loginModal = bootstrap.Modal.getInstance(document.getElementById('login-modal'));
        loginModal.hide();

        updateAuthUI();
        loadStreams();
        loadPosts();

        loginForm.reset();
    } catch (error) {
        loginError.textContent = error.message;
    }
}

async function handleSignup(e) {
    e.preventDefault();
    signupError.textContent = '';

    try {
        await signup(signupUsername.value, signupEmail.value, signupPassword.value);

        alert('Registration successful! Please login.');
        const signupModal = bootstrap.Modal.getInstance(document.getElementById('signup-modal'));
        signupModal.hide();

        showLoginModal();

        signupForm.reset();
    } catch (error) {
        signupError.textContent = error.message;
    }
}

function handleLogout() {
    logout();
    updateAuthUI();
    navigateToHome({ preventDefault: () => {} });
}

function updateCharCount() {
    charCount.textContent = postContent.value.length;
}

async function createPost() {
    postError.textContent = '';

    const content = postContent.value.trim();
    const selectedStreamId = streamSelect.value;

    if (!content) {
        postError.textContent = 'Post content cannot be empty';
        return;
    }

    if (content.length > 140) {
        postError.textContent = 'Post cannot exceed 140 characters';
        return;
    }

    if (!selectedStreamId) {
        postError.textContent = 'Please select a stream';
        return;
    }

    try {
        await api.posts.create(content, selectedStreamId);

        postContent.value = '';
        charCount.textContent = '0';
        loadPosts();
    } catch (error) {
        postError.textContent = error.message;
    }
}

async function loadPosts() {
    const postsContainer = document.getElementById('posts-container');
    const postsLoading = document.getElementById('posts-loading');

    if (!postsContainer) return;

    try {
        postsContainer.innerHTML = '';
        if (postsLoading) {
            postsLoading.classList.remove('d-none');
        }

        const response = await api.posts.getAll();
        const posts = response.content || response;

        if (postsLoading) {
            postsLoading.classList.add('d-none');
        }

        if (!posts || posts.length === 0) {
            postsContainer.innerHTML = '<div class="alert alert-info">No posts yet. Be the first to post!</div>';
            return;
        }

        posts.forEach(post => {
            const postElement = createPostElement(post);
            postsContainer.appendChild(postElement);
        });
    } catch (error) {
        if (postsLoading) {
            postsLoading.classList.add('d-none');
        }
        postsContainer.innerHTML = `<div class="alert alert-danger">Error loading posts: ${error.message}</div>`;
    }
}

function createPostElement(post) {
    const postDate = new Date(post.createdAt).toLocaleString();

    const postElement = document.createElement('div');
    postElement.className = 'card post-card mb-3';
    postElement.innerHTML = `
        <div class="card-body">
            <div class="post-header">
                <span class="post-username">@${post.username}</span>
                <span class="post-date">${postDate}</span>
            </div>
            <p class="post-content">${post.content}</p>
            <div class="post-footer">
                <span>Stream: ${post.streamName}</span>
            </div>
        </div>
    `;
    return postElement;
}

async function createStream() {
    streamError.textContent = '';

    const name = streamName.value.trim();
    const description = streamDescription.value.trim();

    if (!name) {
        streamError.textContent = 'Stream name cannot be empty';
        return;
    }

    try {
        await api.streams.create(name, description);

        streamName.value = '';
        streamDescription.value = '';
        loadStreams();

        updateStreamSelect();
    } catch (error) {
        streamError.textContent = error.message;
    }
}

async function loadStreams() {
    const streamsContainer = document.getElementById('streams-container');
    const streamsLoading = document.getElementById('streams-loading');
    const popularStreams = document.getElementById('popular-streams');

    if (!streamsContainer) return;

    try {
        streamsContainer.innerHTML = '';
        if (streamsLoading) streamsLoading.classList.remove('d-none');

        const streams = await api.streams.getAll();

        if (streamsLoading) streamsLoading.classList.add('d-none');

        if (!streams || streams.length === 0) {
            streamsContainer.innerHTML = '<div class="col-12"><div class="alert alert-info">No streams yet. Create one!</div></div>';
            if (popularStreams) popularStreams.innerHTML = '<p class="text-muted">No streams available</p>';
            return;
        }

        streams.forEach(stream => {
            const streamElement = createStreamElement(stream);
            streamsContainer.appendChild(streamElement);
        });

        updateStreamSelect(streams);

        if (popularStreams) {
            popularStreams.innerHTML = '';
            streams.slice(0, 5).forEach(stream => {
                const streamItem = document.createElement('div');
                streamItem.className = 'mb-2';
                streamItem.innerHTML = `
                    <a href="#" class="stream-link" data-stream-id="${stream.id}">
                        ${stream.name}
                    </a>
                    <p class="small text-muted mb-0">${stream.description || ''}</p>
                `;
                popularStreams.appendChild(streamItem);
            });

            document.querySelectorAll('.stream-link').forEach(link => {
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    const streamId = e.currentTarget.getAttribute('data-stream-id');
                    loadPostsByStream(streamId);
                });
            });
        }
    } catch (error) {
        if (streamsLoading) streamsLoading.classList.add('d-none');
        streamsContainer.innerHTML = `<div class="col-12"><div class="alert alert-danger">Error loading streams: ${error.message}</div></div>`;
        if (popularStreams) popularStreams.innerHTML = '<p class="text-danger">Failed to load streams</p>';
    }
}

function createStreamElement(stream) {
    const streamCol = document.createElement('div');
    streamCol.className = 'col-md-4 mb-4';

    const createdAt = stream.createdAt ? new Date(stream.createdAt).toLocaleDateString() : 'N/A';

    streamCol.innerHTML = `
        <div class="card stream-card h-100">
            <div class="stream-header">
                <h5 class="stream-name">${stream.name}</h5>
            </div>
            <div class="card-body">
                <p class="card-text">${stream.description || 'No description'}</p>
                <p class="stream-info">Created: ${createdAt}</p>
            </div>
            <div class="card-footer bg-transparent">
                <button class="btn btn-sm btn-outline-primary view-stream-btn" data-stream-id="${stream.id}">
                    View Posts
                </button>
            </div>
        </div>
    `;

    setTimeout(() => {
        const viewStreamBtn = streamCol.querySelector('.view-stream-btn');
        if (viewStreamBtn) {
            viewStreamBtn.addEventListener('click', () => {
                const streamId = viewStreamBtn.getAttribute('data-stream-id');
                navigateToHome({ preventDefault: () => {} });
                loadPostsByStream(streamId);
            });
        }
    }, 0);

    return streamCol;
}

async function loadPostsByStream(streamId) {
    const postsContainer = document.getElementById('posts-container');
    const postsLoading = document.getElementById('posts-loading');

    if (!postsContainer) return;

    try {
        postsContainer.innerHTML = '';
        if (postsLoading) {
            postsLoading.classList.remove('d-none');
        }

        const stream = await api.streams.getById(streamId);

        const response = await api.posts.getByStream(streamId);
        const posts = response.content || response;

        if (postsLoading) {
            postsLoading.classList.add('d-none');
        }

        const streamHeader = document.createElement('div');
        streamHeader.className = 'mb-4';
        streamHeader.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                <h3>Posts in "${stream.name}"</h3>
                <button class="btn btn-outline-primary btn-sm" id="back-to-all">Back to All Posts</button>
            </div>
            <p class="text-muted">${stream.description || ''}</p>
            <hr>
        `;
        postsContainer.appendChild(streamHeader);

        document.getElementById('back-to-all').addEventListener('click', loadPosts);

        if (!posts || posts.length === 0) {
            postsContainer.innerHTML += '<div class="alert alert-info">No posts in this stream yet.</div>';
            return;
        }

        posts.forEach(post => {
            const postElement = createPostElement(post);
            postsContainer.appendChild(postElement);
        });
    } catch (error) {
        if (postsLoading) {
            postsLoading.classList.add('d-none');
        }
        postsContainer.innerHTML = `<div class="alert alert-danger">Error loading posts: ${error.message}</div>`;
    }
}

function updateStreamSelect(streams) {
    if (!streamSelect) return;

    const currentSelection = streamSelect.value;

    while (streamSelect.options.length > 1) {
        streamSelect.remove(1);
    }

    if (!streams) {
        api.streams.getAll()
            .then(fetchedStreams => {
                populateStreamSelect(fetchedStreams, currentSelection);
            })
            .catch(error => {
                console.error('Error fetching streams for select:', error);
            });
    } else {
        populateStreamSelect(streams, currentSelection);
    }
}

function populateStreamSelect(streams, currentSelection) {
    if (!streamSelect || !streams || streams.length === 0) return;

    streams.forEach(stream => {
        const option = document.createElement('option');
        option.value = stream.id;
        option.textContent = stream.name;
        if (stream.id.toString() === currentSelection) {
            option.selected = true;
        }
        streamSelect.appendChild(option);
    });
}
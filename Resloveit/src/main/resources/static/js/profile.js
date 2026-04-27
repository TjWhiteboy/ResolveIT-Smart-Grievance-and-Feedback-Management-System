/**
 * Profile System Logic - ResolveIT
 * Handles Tabs, AJAX Uploads, Previews, and Global UI Sync
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // ── 1. Tab Switching & URL Sync ──
    const tabBtns = document.querySelectorAll('.tab-btn');
    const sections = document.querySelectorAll('.tab-section');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.dataset.tab;

            // Update Buttons
            tabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Update Sections
            sections.forEach(s => s.classList.remove('active'));
            document.getElementById(target).classList.add('active');

            // Update URL search param without reload
            const url = new URL(window.location);
            url.searchParams.set('tab', target);
            window.history.pushState({}, '', url);
        });
    });

    // Handle initial tab from URL
    const params = new URLSearchParams(window.location.search);
    const activeTab = params.get('tab');
    if (activeTab) {
        const targetBtn = document.querySelector(`.tab-btn[data-tab="${activeTab}"]`);
        if (targetBtn) targetBtn.click();
    }

    // ── 2. Profile Photo AJAX Upload ──
    const pfpInput = document.getElementById('pfp-upload');
    const pfpPreview = document.getElementById('pfp-preview');
    const uploadLabel = document.querySelector('.pfp-edit-btn');

    if (pfpInput) {
        pfpInput.addEventListener('change', async function() {
            const file = this.files[0];
            if (!file) return;

            // Basic validation
            if (!file.type.startsWith('image/')) {
                showToast('Only image files are allowed', 'error');
                return;
            }
            if (file.size > 5 * 1024 * 1024) { // 5MB
                showToast('Image size should be less than 5MB', 'error');
                return;
            }

            // UI Feedback: Loading
            uploadLabel.innerHTML = '<div class="spinner"></div>';
            uploadLabel.style.pointerEvents = 'none';

            const formData = new FormData();
            formData.append('file', file);

            try {
                const response = await fetch('/profile/upload-pfp', {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (result.success) {
                    // Update Local Preview with cache busting
                    const newSrc = result.path;
                    pfpPreview.src = newSrc;
                    
                    // Update Global Avatars (Navbar & Sidebar)
                    document.querySelectorAll('.avatar img, .user-avatar-small img').forEach(img => {
                        img.src = newSrc;
                    });
                    
                    // Fallback for letter avatars (replace span with img if first upload)
                    document.querySelectorAll('.avatar, .user-avatar-small').forEach(container => {
                        if (!container.querySelector('img')) {
                            container.innerHTML = `<img src="${newSrc}" alt="PFP">`;
                        }
                    });

                    showToast(result.message, 'success');
                } else {
                    showToast(result.message, 'error');
                }
            } catch (error) {
                console.error('Upload Error:', error);
                showToast('Connection error. Please try again.', 'error');
            } finally {
                uploadLabel.innerHTML = '<i class="fa-solid fa-camera"></i>';
                uploadLabel.style.pointerEvents = 'auto';
                pfpInput.value = ''; // Reset input
            }
        });
    }

    // ── 3. Delete Account Modal ──
    const deleteBtn = document.getElementById('delete-account-btn');
    const deleteModal = document.getElementById('delete-modal');
    const cancelDelete = document.getElementById('cancel-delete');

    if (deleteBtn && deleteModal) {
        deleteBtn.addEventListener('click', () => deleteModal.classList.add('active'));
        cancelDelete.addEventListener('click', () => deleteModal.classList.remove('active'));
        
        // Close on clicking outside
        deleteModal.addEventListener('click', (e) => {
            if (e.target === deleteModal) deleteModal.classList.remove('active');
        });
    }

    // ── 4. Form Submission Loading Feedback ──
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                const originalText = submitBtn.innerHTML;
                submitBtn.innerHTML = '<div class="spinner"></div> Saving...';
            }
        });
    });

    // ── 5. Toast System Helpers ──
    function showToast(message, type = 'success') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        const icon = type === 'success' ? 'fa-circle-check' : 'fa-circle-xmark';
        
        toast.innerHTML = `
            <i class="fa-solid ${icon}"></i>
            <span>${message}</span>
        `;

        container.appendChild(toast);

        // Auto remove
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 400);
        }, 4000);
    }

    // ── 6. Browser & Session Info Detection ──
    const browserDisplay = document.getElementById('browser-info');
    if (browserDisplay) {
        const ua = navigator.userAgent;
        let browser = "Unknown Browser";
        let os = "Unknown OS";

        if (ua.includes("Chrome") && !ua.includes("Edg")) browser = "Chrome";
        else if (ua.includes("Firefox")) browser = "Firefox";
        else if (ua.includes("Safari") && !ua.includes("Chrome")) browser = "Safari";
        else if (ua.includes("Edg")) browser = "Edge";

        if (ua.includes("Windows")) os = "Windows";
        else if (ua.includes("Mac")) os = "macOS";
        else if (ua.includes("Linux")) os = "Linux";
        else if (ua.includes("Android")) os = "Android";
        else if (ua.includes("iPhone")) os = "iOS";

        browserDisplay.textContent = `${browser} on ${os}`;
    }

    // ── 7. Preference Toggle Logic (Immediate UI Sync) ──
    const darkModeToggle = document.getElementById('dark-mode-profile');
    if (darkModeToggle) {
        darkModeToggle.addEventListener('change', function() {
            // Re-use dashboard.js logic if possible, or simple override
            const theme = this.checked ? 'dark' : 'light';
            document.body.setAttribute('data-theme', theme);
            localStorage.setItem('theme', theme);
        });
    }
});

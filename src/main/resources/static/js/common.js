/**
 * common.js - Shared UI Logic for ResolveIT
 * Handles Navbar Dropdowns, Theme Toggle, and Global Notifications
 */

document.addEventListener('DOMContentLoaded', function() {
    // ── Theme Management ──
    initTheme();

    // ── Dropdown Management ──
    initDropdown('notif-bell', 'notif-menu');
    initDropdown('user-avatar', 'profile-dropdown-menu');

    // Handle clicks outside dropdowns to close them
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.notification-dropdown') && !e.target.closest('.profile-dropdown')) {
            document.querySelectorAll('.dropdown-menu').forEach(m => m.classList.remove('active'));
        }
    });

    // Handle ESC key to close dropdowns
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.dropdown-menu').forEach(m => m.classList.remove('active'));
        }
    });

    // Mark all as read listener
    const markAllBtn = document.getElementById('mark-all-read');
    if (markAllBtn) {
        markAllBtn.addEventListener('click', async (e) => {
            e.stopPropagation();
            try {
                await fetch('/api/notifications/read-all', { method: 'POST' });
                fetchLatestNotifications();
                fetchUnreadCount();
            } catch (err) {
                console.error('Failed to mark all as read', err);
            }
        });
    }

    // ── Notification Polling (Initial Count) ──
    if (document.getElementById('notif-bell')) {
        fetchUnreadCount();
    }
});

/**
 * Normalizes theme and sets up the toggle button listener.
 * Applies data-theme to BOTH body and html element so all CSS selectors work.
 */
function initTheme() {
    const currentTheme = localStorage.getItem('theme') || 'light';
    applyTheme(currentTheme);

    const themeToggle = document.getElementById('theme-toggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            const isDark = document.body.getAttribute('data-theme') === 'dark';
            const newTheme = isDark ? 'light' : 'dark';
            applyTheme(newTheme);
            localStorage.setItem('theme', newTheme);
        });
    }
}

/**
 * Applies theme to body + html root, and syncs the moon/sun toggle icon.
 */
function applyTheme(theme) {
    document.body.setAttribute('data-theme', theme);
    document.documentElement.setAttribute('data-theme', theme);

    // Sync icon state
    const moon = document.getElementById('theme-icon-moon');
    const sun  = document.getElementById('theme-icon-sun');
    if (moon && sun) {
        if (theme === 'dark') {
            moon.style.display = 'none';
            sun.style.display  = 'inline';
        } else {
            moon.style.display = 'inline';
            sun.style.display  = 'none';
        }
    }
}

/**
 * Universal toggle for navbar dropdowns
 */
function initDropdown(triggerId, menuId) {
    const trigger = document.getElementById(triggerId);
    const menu = document.getElementById(menuId);
    if (!trigger || !menu) return;

    trigger.addEventListener('click', (e) => {
        e.stopPropagation();
        
        const wasActive = menu.classList.contains('active');
        
        // Close ALL dropdowns first
        document.querySelectorAll('.dropdown-menu').forEach(m => m.classList.remove('active'));
        
        // Toggle the target menu only if it wasn't just active
        if (!wasActive) {
            menu.classList.add('active');
        }
        
        // If opening notification menu, fetch latest
        if (menu.classList.contains('active') && triggerId === 'notif-bell') {
            fetchLatestNotifications();
        }
    });
}

/**
 * Fetches unread count and updates the badge
 */
async function fetchUnreadCount() {
    try {
        const res = await fetch('/api/notifications/unread-count');
        if (!res.ok) return;
        const data = await res.json();
        const badge = document.getElementById('notif-badge');
        if (badge) {
            if (data.count > 0) {
                badge.innerText = data.count > 9 ? '9+' : data.count;
                badge.style.display = 'block';
            } else {
                badge.style.display = 'none';
            }
        }
    } catch (e) {
        console.warn('Could not fetch unread count');
    }
}

/**
 * Fetches latest notifications for the dropdown
 */
async function fetchLatestNotifications() {
    const list = document.getElementById('notif-list');
    if (!list) return;
    
    try {
        const res = await fetch('/api/notifications');
        const data = await res.json();
        
        if (data.length === 0) {
            list.innerHTML = '<div class="notif-empty">No new notifications</div>';
            return;
        }

        list.innerHTML = data.map(n => `
            <a href="${n.targetUrl || '#'}" class="notif-item ${n.read ? '' : 'unread'}" onclick="markAsRead(${n.id})">
                <div class="notif-icon" style="background: ${getNotifTypeColor(n.type)}">
                    <i class="${getNotifTypeIcon(n.type)}"></i>
                </div>
                <div class="notif-content">
                    <p class="notif-message">${n.message}</p>
                    <span class="notif-time">${timeAgo(n.createdAt)}</span>
                </div>
            </a>
        `).join('');
    } catch (e) {
        list.innerHTML = '<div class="notif-empty">Error loading notifications</div>';
    }
}

/**
 * Helpers for icons and colors
 */
function getNotifTypeIcon(type) {
    switch(type) {
        case 'ASSIGNMENT': return 'fa-solid fa-user-plus';
        case 'STATUS_UPDATE': return 'fa-solid fa-sync';
        case 'SLA_ALERT': return 'fa-solid fa-clock';
        case 'RESOLVED': return 'fa-solid fa-check-circle';
        case 'INFO': return 'fa-solid fa-circle-info';
        case 'UPDATE': return 'fa-solid fa-arrows-rotate';
        case 'SUCCESS': return 'fa-solid fa-circle-check';
        case 'WARNING': return 'fa-solid fa-triangle-exclamation';
        default: return 'fa-solid fa-bell';
    }
}

function getNotifTypeColor(type) {
    switch(type) {
        case 'ASSIGNMENT': return '#3b82f6';
        case 'STATUS_UPDATE': return '#8b5cf6';
        case 'SLA_ALERT': return '#f59e0b';
        case 'RESOLVED': return '#10b981';
        case 'INFO': return '#3b82f6'; // Blue
        case 'UPDATE': return '#8b5cf6'; // Purple
        case 'SUCCESS': return '#10b981'; // Green
        case 'WARNING': return '#ef4444'; // Red
        default: return '#64748b';
    }
}

function timeAgo(dateStr) {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = Math.floor((now - date) / 1000);
    if (diff < 60) return 'Just now';
    if (diff < 3600) return Math.floor(diff/60) + 'm ago';
    if (diff < 86400) return Math.floor(diff/3600) + 'h ago';
    return date.toLocaleDateString();
}

window.markAsRead = async (id) => {
    try {
        await fetch(`/api/notifications/${id}/read`, { method: 'POST' });
        fetchUnreadCount(); // Update badge
    } catch (e) {}
};

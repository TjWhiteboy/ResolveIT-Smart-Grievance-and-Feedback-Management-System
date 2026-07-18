/**
 * Staff Dashboard Module - Chart Initialization & Interactivity
 */

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    setupToasts();
});

function initCharts() {
    // 1. Productivity Chart (Line)
    const productivityCtx = document.getElementById('productivityChart');
    if (productivityCtx && typeof productivityData !== 'undefined') {
        new Chart(productivityCtx, {
            type: 'line',
            data: {
                labels: Object.keys(productivityData),
                datasets: [{
                    label: 'Resolved Complaints',
                    data: Object.values(productivityData),
                    borderColor: '#4f46e5',
                    backgroundColor: 'rgba(79, 70, 229, 0.1)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 3,
                    pointBackgroundColor: '#fff',
                    pointBorderColor: '#4f46e5',
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { borderDash: [5, 5] }, ticks: { stepSize: 1 } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // 2. Status Distribution (Doughnut)
    const statusCtx = document.getElementById('statusChart');
    if (statusCtx && typeof statusData !== 'undefined') {
        new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: Object.keys(statusData),
                datasets: [{
                    data: Object.values(statusData),
                    backgroundColor: ['#dbeafe', '#fef3c7', '#ede9fe', '#d1fae5', '#fee2e2'],
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { boxWidth: 12, padding: 15, font: { size: 11 } } }
                },
                cutout: '70%'
            }
        });
    }

    // 3. Category Distribution (Bar)
    const categoryCtx = document.getElementById('categoryChart');
    if (categoryCtx && typeof categoryData !== 'undefined') {
        new Chart(categoryCtx, {
            type: 'bar',
            data: {
                labels: Object.keys(categoryData),
                datasets: [{
                    data: Object.values(categoryData),
                    backgroundColor: '#3b82f6',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, display: false },
                    x: { grid: { display: false } }
                }
            }
        });
    }
}

/**
 * Handle Success/Error Toasts from Flash Attributes
 */
function setupToasts() {
    // We check for success/error flash attributes usually passed by ThymeLeaf
    // But we can also look for URL params or hidden elements
    const successMsg = document.querySelector('[data-flash-success]')?.dataset.flashSuccess;
    const errorMsg = document.querySelector('[data-flash-error]')?.dataset.flashError;
    
    if (successMsg || errorMsg) {
        showToast(successMsg || errorMsg, successMsg ? 'success' : 'error');
    }
}

function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type} animate-slide-up`;
    toast.innerHTML = `
        <div class="toast-icon">
            <i class="fa-solid ${type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i>
        </div>
        <div class="toast-content">${message}</div>
    `;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => toast.remove(), 500);
    }, 4000);
}

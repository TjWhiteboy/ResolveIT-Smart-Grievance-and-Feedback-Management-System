document.addEventListener('DOMContentLoaded', function() {
    // ── Status Donut Chart (Staff & User Dashboard) ──
    const chartCtx = document.getElementById('statusChart');
    if (chartCtx && typeof Chart !== 'undefined' && !chartCtx.hasAttribute('data-admin-managed')) {
        const counts = {
            new: parseInt(chartCtx.dataset.new || 0),
            review: parseInt(chartCtx.dataset.review || 0),
            progress: parseInt(chartCtx.dataset.progress || 0),
            resolved: parseInt(chartCtx.dataset.resolved || 0),
            denied: parseInt(chartCtx.dataset.denied || 0)
        };

        new Chart(chartCtx, {
            type: 'doughnut',
            data: {
                labels: ['New', 'Review', 'In Progress', 'Resolved', 'Denied'],
                datasets: [{
                    data: [counts.new, counts.review, counts.progress, counts.resolved, counts.denied],
                    backgroundColor: ['#3b82f6', '#f59e0b', '#8b5cf6', '#10b981', '#ef4444'],
                    borderWidth: 0,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20,
                            font: { family: 'Inter', size: 11 }
                        }
                    }
                },
                cutout: '70%'
            }
        });
    }

    // ── UI Enhancements ──
    const detailLinks = document.querySelectorAll('.view-details');
    detailLinks.forEach(link => {
        link.addEventListener('click', () => {
            // Optional: Show loading overlay
        });
    });

    const tooltips = document.querySelectorAll('[title]');
    tooltips.forEach(t => {
        // Here you could initialize Poppers.js or Tippy.js if available
    });
});

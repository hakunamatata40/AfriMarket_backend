/* AfriMarket Admin — Core JS */

/* ---- Clock ---- */
function updateClock() {
  const el = document.getElementById('clock');
  if (!el) return;
  const now = new Date();
  el.textContent = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}
setInterval(updateClock, 1000);
updateClock();

/* ---- CountUp animation ---- */
function countUp(el, target, duration = 1200, prefix = '', suffix = '') {
  const start = performance.now();
  const startVal = 0;
  const isFloat = target % 1 !== 0;

  function frame(time) {
    const elapsed = time - start;
    const progress = Math.min(elapsed / duration, 1);
    const ease = 1 - Math.pow(1 - progress, 3); // ease-out cubic
    const current = startVal + (target - startVal) * ease;
    el.textContent = prefix + (isFloat ? current.toFixed(0) : Math.round(current)).toLocaleString('fr-FR') + suffix;
    if (progress < 1) requestAnimationFrame(frame);
  }
  requestAnimationFrame(frame);
}

/* Trigger countUp on all .kpi-value elements */
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.kpi-value[data-value]').forEach(el => {
    const raw = parseFloat(el.dataset.value) || 0;
    countUp(el, raw, 1200, el.dataset.prefix || '', el.dataset.suffix || '');
  });
});

/* ---- Toast notifications ---- */
function showToast(message, type = 'success') {
  const container = document.getElementById('toastContainer') ||
    (() => {
      const c = document.createElement('div');
      c.id = 'toastContainer';
      c.className = 'toast-container';
      document.body.appendChild(c);
      return c;
    })();

  const icons = { success: '✓', error: '⚠', warn: '◉' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${icons[type] || '✓'}</span><span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = 'toast-out 0.4s ease both';
    toast.addEventListener('animationend', () => toast.remove());
  }, 3000);
}

/* Show flash messages as toasts */
document.addEventListener('DOMContentLoaded', () => {
  const flashSuccess = document.getElementById('flashSuccess');
  if (flashSuccess) showToast(flashSuccess.textContent, 'success');

  const flashError = document.getElementById('flashError');
  if (flashError) showToast(flashError.textContent, 'error');
});

/* ---- Modal helpers ---- */
function openModal(id) {
  const m = document.getElementById(id);
  if (m) { m.classList.add('open'); }
}
function closeModal(id) {
  const m = document.getElementById(id);
  if (m) { m.classList.remove('open'); }
}

/* Close modal on overlay click */
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
  }
});

/* ---- Confirm before destructive actions ---- */
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', e => {
      if (!confirm(el.dataset.confirm)) e.preventDefault();
    });
  });
});

/* ---- Dashboard charts (called from dashboard page) ---- */
function initDashboardCharts(txData, catData) {
  // Line chart — Transactions 30 jours
  const txCtx = document.getElementById('txChart');
  if (txCtx) {
    new Chart(txCtx, {
      type: 'line',
      data: {
        labels: txData.labels,
        datasets: [{
          label: 'Transactions (FCFA)',
          data: txData.values,
          borderColor: '#4ADE80',
          backgroundColor: (ctx) => {
            const gradient = ctx.chart.ctx.createLinearGradient(0, 0, 0, 220);
            gradient.addColorStop(0, 'rgba(74,222,128,0.3)');
            gradient.addColorStop(1, 'rgba(74,222,128,0.0)');
            return gradient;
          },
          borderWidth: 2,
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#4ADE80',
          pointRadius: 3,
          pointHoverRadius: 6,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: { duration: 1500, easing: 'easeInOutQuart' },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#162416',
            borderColor: 'rgba(74,222,128,0.2)',
            borderWidth: 1,
            titleColor: '#4ADE80',
            bodyColor: '#D4E8D4',
            callbacks: {
              label: ctx => ' ' + ctx.parsed.y.toLocaleString('fr-FR') + ' FCFA'
            }
          }
        },
        scales: {
          x: {
            grid: { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: 'rgba(74,222,128,0.6)', font: { size: 11 } }
          },
          y: {
            grid: { color: 'rgba(255,255,255,0.04)' },
            ticks: {
              color: 'rgba(74,222,128,0.6)',
              font: { size: 11 },
              callback: v => v.toLocaleString('fr-FR')
            }
          }
        }
      }
    });
  }

  // Donut chart — Répartition catégories
  const catCtx = document.getElementById('catChart');
  if (catCtx) {
    new Chart(catCtx, {
      type: 'doughnut',
      data: {
        labels: catData.labels,
        datasets: [{
          data: catData.values,
          backgroundColor: ['#C8973A', '#4ADE80', '#2DD4BF', '#FBBF24', '#A78BFA', '#F87171'],
          borderColor: '#162416',
          borderWidth: 3,
          hoverOffset: 8,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: { duration: 1500, easing: 'easeInOutQuart' },
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#D4E8D4', font: { size: 11 }, padding: 12, boxWidth: 10 }
          },
          tooltip: {
            backgroundColor: '#162416',
            borderColor: 'rgba(74,222,128,0.2)',
            borderWidth: 1,
            titleColor: '#C8973A',
            bodyColor: '#D4E8D4',
          }
        },
        cutout: '65%',
      }
    });
  }
}

/* ---- Animate progress bars on load ---- */
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.progress-bar-fill[data-width]').forEach(el => {
    setTimeout(() => { el.style.width = el.dataset.width + '%'; }, 300);
  });
});

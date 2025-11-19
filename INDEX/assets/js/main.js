// ========================================
// Synapse Documentation - Main JavaScript
// ========================================

// Theme Toggle
function initThemeToggle() {
    const themeToggle = document.getElementById('theme-toggle');
    const html = document.documentElement;
    
    // Load saved theme
    const savedTheme = localStorage.getItem('theme') || 'light';
    html.setAttribute('data-theme', savedTheme);
    updateThemeButton(savedTheme);
    
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            const currentTheme = html.getAttribute('data-theme');
            const newTheme = currentTheme === 'light' ? 'dark' : 'light';
            
            html.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            updateThemeButton(newTheme);
        });
    }
}

function updateThemeButton(theme) {
    const button = document.getElementById('theme-toggle');
    if (button) {
        button.textContent = theme === 'light' ? '🌙 Тёмная' : '☀️ Светлая';
    }
}

// Generate Table of Contents from headings
function generateTOC() {
    const content = document.querySelector('.content');
    const sidebar = document.querySelector('.sidebar ul');
    
    if (!content || !sidebar) return;
    
    const headings = content.querySelectorAll('h2, h3');
    
    if (headings.length === 0) return;
    
    // Clear existing TOC (except the first "Навигация" item if present)
    const navItems = sidebar.querySelectorAll('li');
    navItems.forEach((item, index) => {
        if (index > 0) item.remove();
    });
    
    headings.forEach((heading, index) => {
        // Add ID to heading if it doesn't have one
        if (!heading.id) {
            heading.id = `section-${index}`;
        }
        
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = `#${heading.id}`;
        a.textContent = heading.textContent;
        
        // Indent h3 more than h2
        if (heading.tagName === 'H3') {
            a.style.paddingLeft = '20px';
            a.style.fontSize = '0.9em';
        }
        
        li.appendChild(a);
        sidebar.appendChild(li);
    });
    
    // Highlight current section on scroll
    window.addEventListener('scroll', highlightCurrentSection);
}

// Highlight active section in TOC
function highlightCurrentSection() {
    const sections = document.querySelectorAll('.content h2, .content h3');
    const tocLinks = document.querySelectorAll('.sidebar ul li a');
    
    let currentSection = null;
    
    sections.forEach(section => {
        const rect = section.getBoundingClientRect();
        if (rect.top <= 150 && rect.top >= -rect.height) {
            currentSection = section.id;
        }
    });
    
    tocLinks.forEach(link => {
        const li = link.parentElement;
        if (link.getAttribute('href') === `#${currentSection}`) {
            li.classList.add('active');
        } else {
            li.classList.remove('active');
        }
    });
}

// Smooth scroll to anchor links
function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').slice(1);
            const target = document.getElementById(targetId);
            
            if (target) {
                const offsetTop = target.offsetTop - 100; // Account for sticky header
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
}

// Copy code blocks to clipboard
function initCodeCopy() {
    const codeBlocks = document.querySelectorAll('pre code');
    
    codeBlocks.forEach(block => {
        const pre = block.parentElement;
        const button = document.createElement('button');
        button.className = 'copy-button';
        button.textContent = '📋 Копировать';
        button.style.cssText = `
            position: absolute;
            top: 10px;
            right: 10px;
            padding: 5px 10px;
            background: var(--accent-primary);
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 0.8em;
            opacity: 0;
            transition: opacity 0.3s ease;
        `;
        
        pre.style.position = 'relative';
        pre.appendChild(button);
        
        pre.addEventListener('mouseenter', () => {
            button.style.opacity = '1';
        });
        
        pre.addEventListener('mouseleave', () => {
            button.style.opacity = '0';
        });
        
        button.addEventListener('click', () => {
            const text = block.textContent;
            navigator.clipboard.writeText(text).then(() => {
                button.textContent = '✅ Скопировано!';
                setTimeout(() => {
                    button.textContent = '📋 Копировать';
                }, 2000);
            });
        });
    });
}

// Initialize Mermaid diagrams
function initMermaid() {
    if (typeof mermaid !== 'undefined') {
        mermaid.initialize({
            startOnLoad: true,
            theme: document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'default'
        });
    }
}

// Update Mermaid theme when switching themes
function updateMermaidTheme() {
    // Note: Mermaid diagrams don't need to be redrawn for theme changes
    // They adapt automatically through CSS variables
    // Reloading the page would be required for a true Mermaid theme switch,
    // but it's not necessary for our use case
}

// Mermaid Zoom Modal
function initMermaidZoom() {
    // Create modal structure
    const modal = document.createElement('div');
    modal.className = 'mermaid-modal';
    modal.innerHTML = `
        <div class="mermaid-modal-close" title="Закрыть (ESC)">×</div>
        <div class="mermaid-modal-content"></div>
        <div class="mermaid-zoom-controls">
            <button class="zoom-btn" id="zoom-out" title="Уменьшить (Ctrl + колёсико вниз)">−</button>
            <button class="zoom-btn" id="zoom-reset" title="Вернуть 100%">↻</button>
            <button class="zoom-btn" id="zoom-in" title="Увеличить (Ctrl + колёсико вверх)">+</button>
        </div>
    `;
    document.body.appendChild(modal);
    
    const modalContent = modal.querySelector('.mermaid-modal-content');
    const closeBtn = modal.querySelector('.mermaid-modal-close');
    const zoomInBtn = document.getElementById('zoom-in');
    const zoomOutBtn = document.getElementById('zoom-out');
    const zoomResetBtn = document.getElementById('zoom-reset');
    
    let currentScale = 1;
    let currentDiagram = null;
    
    // Add click handlers to all mermaid diagrams
    document.querySelectorAll('.mermaid').forEach(diagram => {
        diagram.addEventListener('click', () => {
            openModal(diagram);
        });
        
        // Add hint
        const hint = document.createElement('div');
        hint.style.cssText = `
            position: absolute;
            top: 10px;
            right: 10px;
            background: var(--accent-primary);
            color: white;
            padding: 5px 10px;
            border-radius: 5px;
            font-size: 0.8em;
            opacity: 0;
            transition: opacity 0.3s ease;
            pointer-events: none;
        `;
        hint.textContent = '🔍 Нажмите для увеличения';
        diagram.style.position = 'relative';
        diagram.appendChild(hint);
        
        diagram.addEventListener('mouseenter', () => {
            hint.style.opacity = '1';
        });
        
        diagram.addEventListener('mouseleave', () => {
            hint.style.opacity = '0';
        });
    });
    
    function openModal(diagram) {
        currentDiagram = diagram.cloneNode(true);
        currentDiagram.style.cursor = 'default';
        
        // Reset scale to 1 - CSS will handle initial sizing to fill screen
        currentScale = 1;
        currentDiagram.style.transform = 'scale(1)';
        currentDiagram.style.transformOrigin = 'center';
        currentDiagram.style.transition = 'transform 0.3s ease';
        
        // Remove the hint from cloned diagram
        const hint = currentDiagram.querySelector('div[style*="pointer-events"]');
        if (hint) hint.remove();
        
        modalContent.innerHTML = '';
        modalContent.appendChild(currentDiagram);
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Re-initialize mermaid for the cloned diagram
        if (typeof mermaid !== 'undefined') {
            mermaid.init(undefined, currentDiagram);
        }
    }
    
    function closeModal() {
        modal.classList.remove('active');
        document.body.style.overflow = 'auto';
        currentScale = 1;
        currentDiagram = null;
    }
    
    function zoom(delta) {
        if (!currentDiagram) return;
        
        currentScale += delta;
        currentScale = Math.max(0.3, Math.min(currentScale, 5)); // Limit 0.3x to 5x for larger diagrams
        currentDiagram.style.transform = `scale(${currentScale})`;
        currentDiagram.style.transformOrigin = 'center';
    }
    
    function resetZoom() {
        if (!currentDiagram) return;
        currentScale = 1;
        currentDiagram.style.transform = 'scale(1)';
    }
    
    // Event listeners
    closeBtn.addEventListener('click', closeModal);
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });
    
    zoomInBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        zoom(0.25);
    });
    
    zoomOutBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        zoom(-0.25);
    });
    
    zoomResetBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        resetZoom();
    });
    
    // ESC key to close
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modal.classList.contains('active')) {
            closeModal();
        }
    });
    
    // Mouse wheel zoom
    modalContent.addEventListener('wheel', (e) => {
        if (e.ctrlKey) {
            e.preventDefault();
            const delta = e.deltaY > 0 ? -0.1 : 0.1;
            zoom(delta);
        }
    });
}

// Search functionality (basic)
function initSearch() {
    const searchInput = document.getElementById('search-input');
    
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            const content = document.querySelector('.content');
            
            if (!query) {
                // Remove highlights
                content.innerHTML = content.innerHTML.replace(/<mark>/g, '').replace(/<\/mark>/g, '');
                return;
            }
            
            // Simple search highlighting (can be improved)
            // This is a basic implementation
        });
    }
}

// Initialize everything when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    initThemeToggle();
    generateTOC();
    initSmoothScroll();
    initCodeCopy();
    initMermaid();
    
    // Initialize Mermaid zoom after a short delay to ensure diagrams are rendered
    setTimeout(() => {
        initMermaidZoom();
    }, 500);
    
    initSearch();
});


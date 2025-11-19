// ========================================
// Synapse Documentation - Main JavaScript
// ========================================

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
            theme: 'default'
        });
    }
}

// Mermaid Zoom Modal
function initMermaidZoom() {
    // Create modal structure
    const modal = document.createElement('div');
    modal.className = 'mermaid-modal';
    modal.innerHTML = `
        <div class="mermaid-modal-close" title="Закрыть (ESC)">×</div>
        <div class="pan-hint">💡 Зажмите <strong>Пробел</strong> и перетаскивайте мышкой</div>
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
    const panHint = modal.querySelector('.pan-hint');
    const zoomInBtn = document.getElementById('zoom-in');
    const zoomOutBtn = document.getElementById('zoom-out');
    const zoomResetBtn = document.getElementById('zoom-reset');
    
    let currentScale = 1;
    let currentDiagram = null;
    let isSpacePressed = false;
    let isDragging = false;
    let startX = 0;
    let startY = 0;
    let scrollLeft = 0;
    let scrollTop = 0;
    
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
        
        // Remove the hint from cloned diagram
        const hint = currentDiagram.querySelector('div[style*="pointer-events"]');
        if (hint) hint.remove();
        
        // Wrap diagram in a wrapper for proper scrolling
        const wrapper = document.createElement('div');
        wrapper.className = 'mermaid-modal-wrapper';
        wrapper.appendChild(currentDiagram);
        
        modalContent.innerHTML = '';
        modalContent.appendChild(wrapper);
        
        // Reset scale to 1
        currentScale = 1;
        currentDiagram.style.transform = 'scale(1)';
        
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Show hint for 4 seconds
        panHint.classList.add('visible');
        setTimeout(() => {
            panHint.classList.remove('visible');
        }, 4000);
        
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
        
        // Apply transform to diagram
        currentDiagram.style.transform = `scale(${currentScale})`;
        
        // Adjust wrapper size to accommodate scaled content
        const wrapper = currentDiagram.parentElement;
        if (wrapper && wrapper.classList.contains('mermaid-modal-wrapper')) {
            if (currentScale > 1) {
                // Make wrapper larger when zoomed in to enable scrolling
                wrapper.style.width = `${currentScale * 100}%`;
                wrapper.style.height = `${currentScale * 100}%`;
            } else {
                // Reset to default when zoomed out
                wrapper.style.width = '';
                wrapper.style.height = '';
            }
        }
    }
    
    function resetZoom() {
        if (!currentDiagram) return;
        currentScale = 1;
        currentDiagram.style.transform = 'scale(1)';
        
        // Reset wrapper size
        const wrapper = currentDiagram.parentElement;
        if (wrapper && wrapper.classList.contains('mermaid-modal-wrapper')) {
            wrapper.style.width = '';
            wrapper.style.height = '';
        }
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
    
    // Pan with Space + Mouse Drag
    document.addEventListener('keydown', (e) => {
        if (e.code === 'Space' && modal.classList.contains('active') && !isSpacePressed) {
            e.preventDefault();
            isSpacePressed = true;
            modalContent.classList.add('grabbable');
            
            // Show hint when space is pressed
            panHint.textContent = '✋ Теперь перетаскивайте мышкой';
            panHint.classList.add('visible');
        }
    });
    
    document.addEventListener('keyup', (e) => {
        if (e.code === 'Space') {
            isSpacePressed = false;
            isDragging = false;
            modalContent.classList.remove('grabbable', 'grabbing');
            
            // Hide hint when space is released
            panHint.classList.remove('visible');
        }
    });
    
    modalContent.addEventListener('mousedown', (e) => {
        if (isSpacePressed) {
            e.preventDefault();
            isDragging = true;
            modalContent.classList.add('grabbing');
            modalContent.classList.remove('grabbable');
            
            startX = e.pageX - modalContent.offsetLeft;
            startY = e.pageY - modalContent.offsetTop;
            scrollLeft = modalContent.scrollLeft;
            scrollTop = modalContent.scrollTop;
        }
    });
    
    modalContent.addEventListener('mousemove', (e) => {
        if (!isDragging || !isSpacePressed) return;
        
        e.preventDefault();
        const x = e.pageX - modalContent.offsetLeft;
        const y = e.pageY - modalContent.offsetTop;
        const walkX = (x - startX) * 2; // Multiply by 2 for faster scrolling
        const walkY = (y - startY) * 2;
        
        modalContent.scrollLeft = scrollLeft - walkX;
        modalContent.scrollTop = scrollTop - walkY;
    });
    
    modalContent.addEventListener('mouseup', () => {
        if (isDragging) {
            isDragging = false;
            if (isSpacePressed) {
                modalContent.classList.remove('grabbing');
                modalContent.classList.add('grabbable');
            }
        }
    });
    
    modalContent.addEventListener('mouseleave', () => {
        if (isDragging) {
            isDragging = false;
            modalContent.classList.remove('grabbing');
            if (isSpacePressed) {
                modalContent.classList.add('grabbable');
            }
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


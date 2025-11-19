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
    function tryInit() {
        if (typeof mermaid !== 'undefined') {
            mermaid.initialize({
                startOnLoad: true,
                theme: 'default',
                securityLevel: 'loose'
            });
            return true;
        }
        return false;
    }
    
    // Try immediately
    if (!tryInit()) {
        // If Mermaid not loaded yet, wait for it
        let attempts = 0;
        const checkInterval = setInterval(() => {
            attempts++;
            if (tryInit() || attempts > 20) {
                clearInterval(checkInterval);
                if (attempts > 20) {
                    console.warn('Mermaid library failed to load after 2 seconds');
                }
            }
        }, 100);
    }
}

// Mermaid Fullscreen
function initMermaidFullscreen() {
    // Create fullscreen container
    const fullscreen = document.createElement('div');
    fullscreen.className = 'mermaid-fullscreen';
    fullscreen.innerHTML = `
        <div class="mermaid-fullscreen-close">×</div>
        <div class="mermaid-fullscreen-content"></div>
    `;
    document.body.appendChild(fullscreen);
    
    const content = fullscreen.querySelector('.mermaid-fullscreen-content');
    const closeBtn = fullscreen.querySelector('.mermaid-fullscreen-close');
    
    function addClickHandlers() {
        // Find all rendered mermaid diagrams (they should have SVG inside)
        const diagrams = document.querySelectorAll('.mermaid');
        
        diagrams.forEach(diagram => {
            // Skip if already has click handler
            if (diagram.dataset.hasClickHandler === 'true') return;
            
            // Check if diagram has SVG (is rendered)
            const svg = diagram.querySelector('svg');
            if (!svg) return; // Skip if not rendered yet
            
            diagram.dataset.hasClickHandler = 'true';
            diagram.style.cursor = 'pointer';
            
            diagram.addEventListener('click', () => {
                // Clone the entire HTML of the diagram including SVG
                const clone = document.createElement('div');
                clone.innerHTML = diagram.outerHTML;
                const clonedDiagram = clone.firstChild;
                
                // Show fullscreen
                content.innerHTML = '';
                content.appendChild(clonedDiagram);
                fullscreen.classList.add('active');
                document.body.style.overflow = 'hidden';
            });
        });
    }
    
    // Try to add handlers immediately
    addClickHandlers();
    
    // Also try after a delay in case Mermaid is still rendering
    setTimeout(addClickHandlers, 500);
    setTimeout(addClickHandlers, 1500);
    
    // Watch for new diagrams being added
    const observer = new MutationObserver(() => {
        addClickHandlers();
    });
    
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
    
    function closeFullscreen() {
        fullscreen.classList.remove('active');
        document.body.style.overflow = 'auto';
        content.innerHTML = '';
    }
    
    // Close handlers
    closeBtn.addEventListener('click', closeFullscreen);
    
    // Close on ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && fullscreen.classList.contains('active')) {
            closeFullscreen();
        }
    });
    
    // Close on click outside
    fullscreen.addEventListener('click', (e) => {
        if (e.target === fullscreen) {
            closeFullscreen();
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
    
    // Initialize fullscreen after Mermaid has rendered all diagrams
    // Mermaid needs time to process and render SVG
    setTimeout(() => {
        initMermaidFullscreen();
    }, 2000);
    
    initSearch();
});


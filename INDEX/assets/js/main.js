// ========================================
// Synapse Documentation - Main JavaScript
// ========================================

// Generate Table of Contents from headings
function generateTOC() {
    const content = document.querySelector('.content');
    const sidebar = document.querySelector('.sidebar');
    const sidebarList = document.querySelector('.sidebar ul');
    
    if (!content || !sidebar || !sidebarList) return;
    
    const headings = content.querySelectorAll('h2, h3');
    
    if (headings.length === 0) {
        // Hide sidebar if no headings
        sidebar.style.display = 'none';
        return;
    }
    
    // Clear existing TOC (except the first "Навигация" item if present)
    const navItems = sidebarList.querySelectorAll('li');
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
        
        // Add click handler for manual highlight
        a.addEventListener('click', function(e) {
            // Remove active class from all items
            document.querySelectorAll('.sidebar ul li').forEach(item => {
                item.classList.remove('active');
            });
            
            // Add active class to clicked item
            li.classList.add('active');
            
            // Enable manual highlight mode
            isManualHighlight = true;
            lastScrollPosition = window.scrollY || window.pageYOffset;
        });
        
        li.appendChild(a);
        sidebarList.appendChild(li);
    });
    
    // Highlight current section on scroll
    window.addEventListener('scroll', highlightCurrentSection);
    window.addEventListener('scroll', detectUserScroll);
}

// Variables for manual highlight control
let isManualHighlight = false;
let isProgrammaticScroll = false;
let lastScrollPosition = 0;

// Highlight active section in TOC
function highlightCurrentSection() {
    // Skip auto-highlight if manual highlight is active
    if (isManualHighlight) {
        return;
    }
    
    const sections = document.querySelectorAll('.content h2, .content h3');
    const tocLinks = document.querySelectorAll('.sidebar ul li a');
    
    // Get current scroll position (top edge of viewport)
    const scrollPosition = window.scrollY || window.pageYOffset;
    
    // Calculate reference point: header height + 20px offset
    const header = document.querySelector('.header');
    const headerHeight = header ? header.offsetHeight : 0;
    const offset = headerHeight + 20;
    const referencePoint = scrollPosition + offset;
    
    let currentSection = null;
    
    // Find the section that contains the reference point
    // That is: the last section whose top is at or before the reference point
    sections.forEach(section => {
        const sectionTop = section.offsetTop;
        
        // Check if this section's top is at or above the reference point
        if (sectionTop <= referencePoint) {
            currentSection = section.id;
        }
    });
    
    // Highlight the corresponding TOC link
    tocLinks.forEach(link => {
        const li = link.parentElement;
        if (link.getAttribute('href') === `#${currentSection}`) {
            li.classList.add('active');
        } else {
            li.classList.remove('active');
        }
    });
}

// Detect user scroll (not programmatic)
function detectUserScroll() {
    // Ignore programmatic scroll
    if (isProgrammaticScroll) {
        return;
    }
    
    const currentScrollPosition = window.scrollY || window.pageYOffset;
    
    // If scroll position changed and we're in manual mode, immediately re-enable auto-highlight
    if (isManualHighlight && Math.abs(currentScrollPosition - lastScrollPosition) > 3) {
        // Re-enable auto-highlight immediately
        isManualHighlight = false;
        highlightCurrentSection();
    }
    
    lastScrollPosition = currentScrollPosition;
}

// Smooth scroll to anchor links
function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').slice(1);
            const target = document.getElementById(targetId);
            
            if (target) {
                // Mark as programmatic scroll
                isProgrammaticScroll = true;
                
                const offsetTop = target.offsetTop - 100; // Account for sticky header
                const startPosition = window.scrollY || window.pageYOffset;
                const distance = Math.abs(offsetTop - startPosition);
                
                // Estimate scroll duration (rough approximation)
                const duration = Math.min(distance / 2, 1000); // Max 1 second
                
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
                
                // Wait for scroll to finish, then allow user scroll detection
                setTimeout(() => {
                    isProgrammaticScroll = false;
                }, duration + 100);
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

// Mermaid - Open in separate page
function initMermaidLinks() {
    function attachClickHandler(diagram) {
        // Skip if already has handler
        if (diagram.dataset.hasClickHandler === 'true') return;
        
        // Check if diagram has SVG (is rendered)
        const svg = diagram.querySelector('svg');
        if (!svg) return;
        
        // Mark as processed
        diagram.dataset.hasClickHandler = 'true';
        
        // Make it clickable
        diagram.style.cursor = 'pointer';
        diagram.title = 'Открыть схему на отдельной странице';
        
        diagram.addEventListener('click', () => {
            // Open the diagram's dedicated page
            const diagramUrl = diagram.dataset.diagramUrl;
            if (diagramUrl) {
                window.open(diagramUrl, '_blank');
            }
        });
    }
    
    // Process all existing diagrams
    function processAllDiagrams() {
        const diagrams = document.querySelectorAll('.mermaid');
        diagrams.forEach(attachClickHandler);
    }
    
    // Initial check
    processAllDiagrams();
    
    // Watch for Mermaid rendering (SVG insertion)
    const observer = new MutationObserver((mutations) => {
        processAllDiagrams();
    });
    
    // Observe the entire document for changes
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
    
    // Also retry after delays (fallback)
    setTimeout(processAllDiagrams, 500);
    setTimeout(processAllDiagrams, 1000);
    setTimeout(processAllDiagrams, 2000);
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

// Scroll-to-top button
function initScrollToTopButton() {
    // Avoid duplicates if script is loaded twice
    if (document.getElementById('scroll-to-top')) return;

    const btn = document.createElement('button');
    btn.id = 'scroll-to-top';
    btn.className = 'scroll-to-top';
    btn.type = 'button';
    btn.setAttribute('aria-label', 'Наверх');
    btn.title = 'Наверх';
    btn.textContent = '↑';

    const prefersReducedMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    btn.addEventListener('click', () => {
        try {
            window.scrollTo({ top: 0, behavior: prefersReducedMotion ? 'auto' : 'smooth' });
        } catch (_) {
            // Fallback for older browsers
            window.scrollTo(0, 0);
        }
    });

    document.body.appendChild(btn);

    const SHOW_AFTER_PX = 400;
    const updateVisibility = () => {
        const y = window.scrollY || window.pageYOffset || 0;
        if (y >= SHOW_AFTER_PX) {
            btn.classList.add('visible');
        } else {
            btn.classList.remove('visible');
        }
    };

    updateVisibility();
    window.addEventListener('scroll', updateVisibility, { passive: true });
    window.addEventListener('resize', updateVisibility);
}

// Initialize everything when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    generateTOC();
    initSmoothScroll();
    initCodeCopy();
    initMermaidLinks();
    initSearch();
    initScrollToTopButton();
});


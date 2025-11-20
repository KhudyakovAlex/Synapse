#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Synapse Documentation Converter
Converts Markdown files to HTML with unified styling
"""

import os
import re
from pathlib import Path
from typing import List, Tuple

# Try to import markdown library
try:
    import markdown
    MARKDOWN_AVAILABLE = True
except ImportError:
    MARKDOWN_AVAILABLE = False
    print("Warning: markdown library not found. Install with: pip install markdown")
    print("Falling back to simple converter...")

# HTML Template for Mermaid Diagram Page
DIAGRAM_TEMPLATE = """<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title} - Synapse Diagram</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {{
            --bg-primary: #ffffff;
            --bg-secondary: #f8f9fa;
            --text-primary: #333333;
            --accent-primary: #667eea;
            --accent-secondary: #764ba2;
        }}
        
        * {{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }}
        
        body {{
            font-family: 'IBM Plex Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: var(--bg-primary);
            color: var(--text-primary);
            margin: 0;
            padding: 0;
            width: 100vw;
            height: 100vh;
            overflow: auto;
            display: flex;
            align-items: center;
            justify-content: center;
            user-select: none;
        }}
        
        .diagram-container {{
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            box-sizing: border-box;
        }}
        
        .mermaid {{
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            transform-origin: top left;
            user-select: none;
        }}
        
        .mermaid svg {{
            max-width: 100%;
            max-height: 100%;
            width: auto !important;
            height: auto !important;
        }}
    </style>
    <script>
        window.mermaid = {{
            startOnLoad: true,
            theme: "default",
            securityLevel: "loose"
        }};
    </script>
    <script defer src="{js_path}assets/js/mermaid.min.js"></script>
    <script>
        // Zoom with mouse wheel
        let currentScale = 1;
        const minScale = 0.5;
        const maxScale = 5;
        const scaleStep = 0.1;
        
        // Pan with mouse drag
        let isPanning = false;
        let startX = 0;
        let startY = 0;
        let scrollLeft = 0;
        let scrollTop = 0;
        let shiftPressed = false;
        
        document.addEventListener('DOMContentLoaded', () => {{
            const mermaid = document.querySelector('.mermaid');
            
            // Set cursor
            document.body.style.cursor = 'grab';
            
            // Zoom with wheel
            document.addEventListener('wheel', (e) => {{
                e.preventDefault();
                
                if (e.deltaY < 0) {{
                    currentScale = Math.min(currentScale + scaleStep, maxScale);
                }} else {{
                    currentScale = Math.max(currentScale - scaleStep, minScale);
                }}
                
                mermaid.style.transform = `scale(${{currentScale}})`;
            }}, {{ passive: false }});
            
            // Track Shift key
            document.addEventListener('keydown', (e) => {{
                if (e.key === 'Shift' && !shiftPressed) {{
                    shiftPressed = true;
                    document.body.style.cursor = 'text';
                    document.body.style.userSelect = 'text';
                    mermaid.style.userSelect = 'text';
                }}
            }});
            
            document.addEventListener('keyup', (e) => {{
                if (e.key === 'Shift') {{
                    shiftPressed = false;
                    document.body.style.cursor = isPanning ? 'grabbing' : 'grab';
                    document.body.style.userSelect = 'none';
                    mermaid.style.userSelect = 'none';
                    // Clear selection when Shift is released
                    window.getSelection().removeAllRanges();
                }}
            }});
            
            // Pan with mouse (only if Shift not pressed)
            document.addEventListener('mousedown', (e) => {{
                if (shiftPressed) {{
                    // Clear previous selection to start fresh
                    window.getSelection().removeAllRanges();
                    return; // Allow text selection
                }}
                
                isPanning = true;
                document.body.style.cursor = 'grabbing';
                startX = e.pageX - document.documentElement.scrollLeft;
                startY = e.pageY - document.documentElement.scrollTop;
                scrollLeft = document.documentElement.scrollLeft;
                scrollTop = document.documentElement.scrollTop;
                e.preventDefault();
            }});
            
            document.addEventListener('mousemove', (e) => {{
                if (!isPanning) return;
                
                const x = e.pageX - document.documentElement.scrollLeft;
                const y = e.pageY - document.documentElement.scrollTop;
                const walkX = (x - startX);
                const walkY = (y - startY);
                
                window.scrollTo(scrollLeft - walkX, scrollTop - walkY);
            }});
            
            document.addEventListener('mouseup', () => {{
                if (isPanning) {{
                    isPanning = false;
                    document.body.style.cursor = shiftPressed ? 'text' : 'grab';
                }}
            }});
        }});
    </script>
</head>
<body>
    <div class="diagram-container">
        <div class="mermaid">
{diagram_code}
        </div>
    </div>
</body>
</html>
"""

# HTML Template
HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title} - Synapse Documentation</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="{css_path}assets/css/style.css">
    <script>
        window.mermaid = {{
            startOnLoad: true,
            theme: "default",
            securityLevel: "loose"
        }};
    </script>
    <script defer src="{js_path}assets/js/mermaid.min.js"></script>
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <h1><a href="{root_path}index.html">🔆 Synapse Docs</a></h1>
            <nav class="header-nav">
                <a href="{root_path}index.html">Главная</a>
                <a href="https://github.com/KhudyakovAlex/Synapse" target="_blank">GitHub</a>
            </nav>
        </div>
    </header>

    <!-- Breadcrumbs -->
    <nav class="breadcrumbs">
        {breadcrumbs}
    </nav>

    <!-- Main Container -->
    <div class="container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <h3>📑 Содержание</h3>
            <ul>
                <!-- TOC will be auto-generated by JS -->
            </ul>
        </aside>

        <!-- Content -->
        <main class="content">
            <p class="source-link" style="display: inline-block;">
                <a href="{github_url}" target="_blank">
                    📄 Исходный файл на GitHub
                </a>
            </p>
            
            {content}
        </main>
    </div>

    <!-- Footer -->
    <footer class="footer">
        <p>
            <strong>Synapse Project</strong> © 2025 | 
            <a href="https://github.com/KhudyakovAlex/Synapse" target="_blank">GitHub</a>
        </p>
        <p style="margin-top: 10px; font-size: 0.9em;">
            Автоматизированная система управления освещением на базе DALI
        </p>
    </footer>

    <script src="{js_path}assets/js/main.js"></script>
</body>
</html>
"""


class MarkdownConverter:
    def __init__(self, repo_root: str):
        self.repo_root = Path(repo_root)
        self.index_root = self.repo_root / "INDEX"
        
    def convert_heading(self, text: str) -> str:
        """Convert markdown headings to HTML"""
        # H1
        text = re.sub(r'^# (.+)$', r'<h1>\1</h1>', text, flags=re.MULTILINE)
        # H2
        text = re.sub(r'^## (.+)$', r'<h2>\1</h2>', text, flags=re.MULTILINE)
        # H3
        text = re.sub(r'^### (.+)$', r'<h3>\1</h3>', text, flags=re.MULTILINE)
        # H4
        text = re.sub(r'^#### (.+)$', r'<h4>\1</h4>', text, flags=re.MULTILINE)
        return text
    
    def convert_bold(self, text: str) -> str:
        """Convert markdown bold to HTML"""
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        return text
    
    def convert_italic(self, text: str) -> str:
        """Convert markdown italic to HTML"""
        text = re.sub(r'\*(.+?)\*', r'<em>\1</em>', text)
        text = re.sub(r'_(.+?)_', r'<em>\1</em>', text)
        return text
    
    def convert_code_inline(self, text: str) -> str:
        """Convert inline code to HTML"""
        text = re.sub(r'`([^`]+)`', r'<code>\1</code>', text)
        return text
    
    def convert_lists(self, text: str) -> str:
        """Convert markdown lists to HTML"""
        lines = text.split('\n')
        result = []
        in_ul = False
        in_ol = False
        
        for line in lines:
            # Unordered list
            if re.match(r'^- (.+)$', line):
                if not in_ul:
                    if in_ol:
                        result.append('</ol>')
                        in_ol = False
                    result.append('<ul>')
                    in_ul = True
                item = re.sub(r'^- (.+)$', r'\1', line)
                result.append(f'<li>{item}</li>')
            # Ordered list
            elif re.match(r'^\d+\.\s+(.+)$', line):
                if not in_ol:
                    if in_ul:
                        result.append('</ul>')
                        in_ul = False
                    result.append('<ol>')
                    in_ol = True
                item = re.sub(r'^\d+\.\s+(.+)$', r'\1', line)
                result.append(f'<li>{item}</li>')
            else:
                if in_ul:
                    result.append('</ul>')
                    in_ul = False
                if in_ol:
                    result.append('</ol>')
                    in_ol = False
                result.append(line)
        
        if in_ul:
            result.append('</ul>')
        if in_ol:
            result.append('</ol>')
            
        return '\n'.join(result)
    
    def convert_code_blocks(self, text: str) -> str:
        """Convert code blocks to HTML, preserving Mermaid diagrams"""
        # Mermaid blocks
        text = re.sub(
            r'```mermaid\n(.*?)```',
            r'<div class="mermaid">\1</div>',
            text,
            flags=re.DOTALL
        )
        
        # Regular code blocks
        def replace_code(match):
            lang = match.group(1) or ''
            code = match.group(2)
            # Escape HTML entities
            code = code.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            return f'<pre><code class="language-{lang}">{code}</code></pre>'
        
        text = re.sub(
            r'```(\w+)?\n(.*?)```',
            replace_code,
            text,
            flags=re.DOTALL
        )
        
        return text
    
    def convert_line_breaks(self, text: str) -> str:
        """Convert markdown line breaks (two spaces at end of line) to <br>"""
        # Two spaces + newline = <br>
        # Support both Unix (\n) and Windows (\r\n) line endings
        text = re.sub(r'  \r?\n', '<br>\n', text)
        return text
    
    def convert_paragraphs(self, text: str) -> str:
        """Wrap text paragraphs in <p> tags"""
        lines = text.split('\n')
        result = []
        paragraph = []
        
        for line in lines:
            stripped = line.strip()
            
            # Skip if it's already HTML
            if stripped.startswith('<'):
                if paragraph:
                    result.append('<p>' + ' '.join(paragraph) + '</p>')
                    paragraph = []
                result.append(line)
            elif stripped == '':
                if paragraph:
                    result.append('<p>' + ' '.join(paragraph) + '</p>')
                    paragraph = []
                result.append('')
            else:
                paragraph.append(stripped)
        
        if paragraph:
            result.append('<p>' + ' '.join(paragraph) + '</p>')
        
        return '\n'.join(result)
    
    def simple_markdown_to_html(self, md_text: str) -> str:
        """Convert markdown to HTML using simple regex"""
        # Order matters!
        html = md_text
        
        # Line breaks first (before code blocks)
        html = self.convert_line_breaks(html)
        
        # Code blocks (to protect code from other conversions)
        html = self.convert_code_blocks(html)
        
        # Headings
        html = self.convert_heading(html)
        
        # Bold and italic
        html = self.convert_bold(html)
        html = self.convert_italic(html)
        
        # Inline code
        html = self.convert_code_inline(html)
        
        # Lists
        html = self.convert_lists(html)
        
        # Paragraphs (last)
        # html = self.convert_paragraphs(html)
        
        return html
    
    def generate_breadcrumbs(self, rel_path: str) -> str:
        """Generate breadcrumb navigation"""
        parts = rel_path.split('/')
        breadcrumbs = ['<a href="../index.html">Главная</a>', '<span>›</span>']
        
        if len(parts) > 1:
            section = parts[0]
            section_name = "PRD" if section == "PRD" else "PDS"
            breadcrumbs.append(f'<a href="../index.html#{section.lower()}">{section_name}</a>')
            breadcrumbs.append('<span>›</span>')
        
        filename = Path(parts[-1]).stem
        breadcrumbs.append(f'<span>{filename}</span>')
        
        return '\n        '.join(breadcrumbs)
    
    def get_relative_paths(self, file_path: Path) -> Tuple[str, str, str]:
        """Get relative paths for CSS, JS, and root"""
        # Determine depth from INDEX root
        try:
            rel_from_index = file_path.relative_to(self.index_root)
            depth = len(rel_from_index.parts) - 1
        except ValueError:
            depth = 0
        
        prefix = '../' * depth if depth > 0 else ''
        
        return prefix, prefix, prefix
    
    def create_diagram_page(self, diagram_code: str, diagram_index: int, output_file: Path, title: str) -> str:
        """Create a separate HTML page for a diagram and return its URL"""
        # Create filename for diagram page
        diagram_filename = f"{output_file.stem}_diagram_{diagram_index}.html"
        diagram_path = output_file.parent / diagram_filename
        
        # Get relative paths
        css_path, js_path, root_path = self.get_relative_paths(diagram_path)
        
        # Back URL (relative to diagram page)
        back_url = output_file.name
        
        # Fill diagram template
        diagram_html = DIAGRAM_TEMPLATE.format(
            title=f"{title} - Диаграмма {diagram_index + 1}",
            js_path=js_path,
            back_url=back_url,
            diagram_code=diagram_code
        )
        
        # Write diagram page
        with open(diagram_path, 'w', encoding='utf-8') as f:
            f.write(diagram_html)
        
        print(f"  > Created diagram: {diagram_path}")
        
        # Return relative URL
        return diagram_filename
    
    def convert_file(self, md_file: Path):
        """Convert a single markdown file to HTML"""
        print(f"Converting: {md_file}")
        
        # Read markdown
        with open(md_file, 'r', encoding='utf-8') as f:
            md_content = f.read()
        
        # Get title from first heading
        title_match = re.search(r'^#\s+(.+)$', md_content, re.MULTILINE)
        title = title_match.group(1) if title_match else md_file.stem
        
        # Determine output path first (we need it for diagram pages)
        try:
            rel_path = md_file.relative_to(self.repo_root)
        except ValueError:
            print(f"Error: {md_file} is not in repo root")
            return
        
        output_file = self.index_root / rel_path.with_suffix('.html')
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        # Convert markdown to HTML
        if MARKDOWN_AVAILABLE:
            # Use proper markdown library with extensions
            md = markdown.Markdown(extensions=[
                'extra',  # Includes: tables, fenced_code, attr_list, def_list, footnotes, abbr
                'nl2br',  # Convert single newlines to <br>
                'sane_lists',  # Better list handling
            ])
            html_content = md.convert(md_content)
            
            # Post-process: Convert Mermaid code blocks to div.mermaid
            # AND create separate pages for each diagram
            diagram_counter = [0]  # Use list to allow modification in nested function
            
            def replace_mermaid(match):
                code = match.group(1)
                # Unescape HTML entities for Mermaid
                code = code.replace('&quot;', '"')
                code = code.replace('&amp;', '&')
                code = code.replace('&lt;', '<')
                code = code.replace('&gt;', '>')
                
                # Create separate page for this diagram
                diagram_url = self.create_diagram_page(code, diagram_counter[0], output_file, title)
                diagram_counter[0] += 1
                
                # Return div with data-diagram-url attribute
                return f'<div class="mermaid" data-diagram-url="{diagram_url}">{code}</div>'
            
            html_content = re.sub(
                r'<pre><code class="language-mermaid">(.*?)</code></pre>',
                replace_mermaid,
                html_content,
                flags=re.DOTALL
            )
        else:
            # Fallback to simple converter
            html_content = self.simple_markdown_to_html(md_content)
        
        # Generate breadcrumbs
        breadcrumbs = self.generate_breadcrumbs(str(rel_path))
        
        # Get relative paths
        css_path, js_path, root_path = self.get_relative_paths(output_file)
        
        # GitHub URL
        github_url = f"https://github.com/KhudyakovAlex/Synapse/blob/master/{rel_path}"
        
        # Fill template
        html = HTML_TEMPLATE.format(
            title=title,
            css_path=css_path,
            js_path=js_path,
            root_path=root_path,
            breadcrumbs=breadcrumbs,
            github_url=github_url,
            content=html_content
        )
        
        # Write HTML
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html)
        
        print(f"  > Created: {output_file}")
    
    def convert_all(self):
        """Convert all markdown files in PRD and PDS folders"""
        print("="*60)
        print("Synapse Documentation Converter")
        print("="*60)
        
        # Find all .md files
        md_files = []
        for folder in ['PRD', 'PDS']:
            folder_path = self.repo_root / folder
            if folder_path.exists():
                md_files.extend(folder_path.glob('*.md'))
        
        print(f"\nFound {len(md_files)} markdown files\n")
        
        # Convert each file
        for md_file in md_files:
            try:
                self.convert_file(md_file)
            except Exception as e:
                print(f"  X Error converting {md_file}: {e}")
        
        print("\n" + "="*60)
        print("Conversion complete!")
        print("="*60)


if __name__ == '__main__':
    # Get repository root (current directory)
    repo_root = os.path.dirname(os.path.abspath(__file__))
    
    # Create converter and run
    converter = MarkdownConverter(repo_root)
    converter.convert_all()


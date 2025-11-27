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
    <link rel="icon" type="image/png" href="../assets/img/Synapse_ico.png">
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
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
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
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
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
                    document.body.style.webkitUserSelect = 'text';
                    document.body.style.mozUserSelect = 'text';
                    document.body.style.msUserSelect = 'text';
                    document.body.style.userSelect = 'text';
                    mermaid.style.webkitUserSelect = 'text';
                    mermaid.style.mozUserSelect = 'text';
                    mermaid.style.msUserSelect = 'text';
                    mermaid.style.userSelect = 'text';
                }}
            }});
            
            document.addEventListener('keyup', (e) => {{
                if (e.key === 'Shift') {{
                    shiftPressed = false;
                    document.body.style.cursor = isPanning ? 'grabbing' : 'grab';
                    // Don't change user-select back to none - keep selection visible
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
    <link rel="icon" type="image/png" href="{css_path}assets/img/Synapse_ico.png">
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
    <!-- Logo Bar -->
    <div class="header" style="background: var(--accent-primary); padding: 20px 0; margin-bottom: 0;">
        <div class="header-container" style="max-width: 1400px; margin: 0 auto; padding: 0 20px; display: flex; justify-content: space-between; align-items: center;">
            <a href="{root_path}index.html" style="margin-left: 10px;">
                <img src="{root_path}assets/img/Synapse_black.png" alt="Synapse" style="height: 50px; display: block;">
            </a>
            <div style="display: flex; align-items: center; gap: 20px;">
                <a href="https://t.me/+az-cc3wBJssxNzcy" target="_blank" style="display: flex; align-items: center; color: white; text-decoration: none; transition: opacity 0.2s ease;">
                    <img src="{root_path}assets/img/telegram.svg" alt="Telegram" style="width: 24px; height: 24px; display: block;">
                </a>
                <a href="{github_url}" target="_blank" style="color: white; text-decoration: none; font-weight: 500; font-size: 1.1em; transition: opacity 0.2s ease; margin-right: 10px;">GitHub →</a>
            </div>
        </div>
    </div>

    <!-- Page Title -->
    <div class="page-title-container" style="max-width: 1400px; margin: 0 auto; padding: 40px 20px 20px 20px;">
        {page_title}
    </div>

    <!-- Main Container -->
    <div class="container" style="max-width: 1400px; margin: 0 auto; padding: 0 20px 60px 20px;">
        <!-- Sidebar -->
        <aside class="sidebar">
            <h3>Содержание</h3>
            <ul>
                <!-- TOC will be auto-generated by JS -->
            </ul>
        </aside>

        <!-- Content -->
        <main class="content">
            {content}
        </main>
    </div>

    <script src="{js_path}assets/js/main.js"></script>
</body>
</html>
"""


class MarkdownConverter:
    def __init__(self, repo_root: str):
        self.repo_root = Path(repo_root)
        self.index_root = Path(repo_root) / "INDEX"
        
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
    
    def fix_lists_in_html(self, html: str) -> str:
        """Fix lists that might have been broken - convert paragraphs with list items to proper <ul>/<ol> with nested support"""
        
        def fix_paragraph_with_list(match):
            para_content = match.group(1)
            full_match = match.group(0)
            
            # Split content by newlines (both \n and <br />)
            # First, replace <br /> with \n for easier processing
            content_normalized = para_content.replace('<br />', '\n')
            lines = content_normalized.split('\n')
            
            # Find list items with their indentation levels
            list_items = []
            list_start_line = None
            
            for i, line in enumerate(lines):
                # Find lines starting with "- " (with optional leading whitespace)
                match_item = re.match(r'^(\s*)- (.+)$', line)
                if match_item:
                    indent = len(match_item.group(1))  # Number of spaces before "-"
                    item_text = match_item.group(2).strip()
                    if list_start_line is None:
                        list_start_line = i
                    list_items.append((i, indent, item_text))
            
            if list_items and len(list_items) >= 1:
                result_parts = []
                
                # Text before list
                if list_start_line > 0:
                    before_lines = [lines[i].strip() for i in range(list_start_line) if lines[i].strip()]
                    before_text = ' '.join(before_lines).strip()
                    if before_text:
                        result_parts.append(f'<p>{before_text}</p>')
                
                # Build nested unordered list
                def build_nested_list(items, start_idx=0, current_level=0):
                    """Recursively build nested list structure"""
                    if start_idx >= len(items):
                        return '', start_idx
                    
                    html_parts = []
                    i = start_idx
                    
                    while i < len(items):
                        line_idx, indent, item_text = items[i]
                        
                        # If this item is at a deeper level, recurse
                        if indent > current_level:
                            nested_html, next_idx = build_nested_list(items, i, indent)
                            html_parts.append(nested_html)
                            i = next_idx
                            continue
                        
                        # If this item is at a shallower level, we're done with this list
                        if indent < current_level:
                            break
                        
                        # Same level - add as list item
                        html_parts.append(f'  <li>{item_text}')
                        i += 1
                        
                        # Check if next item is nested
                        if i < len(items) and items[i][1] > current_level:
                            nested_html, next_idx = build_nested_list(items, i, items[i][1])
                            html_parts.append(nested_html)
                            html_parts.append('  </li>')
                            i = next_idx
                        else:
                            html_parts.append('  </li>')
                    
                    list_html = '<ul>\n' + '\n'.join(html_parts) + '\n</ul>'
                    return list_html, i
                
                list_html, _ = build_nested_list(list_items)
                result_parts.append(list_html)
                
                # Text after list (if any)
                last_item_line = list_items[-1][0]
                if last_item_line + 1 < len(lines):
                    after_lines = [lines[i].strip() for i in range(last_item_line + 1, len(lines)) if lines[i].strip()]
                    after_text = ' '.join(after_lines).strip()
                    if after_text and not re.match(r'^\s*- ', after_text):
                        result_parts.append(f'<p>{after_text}</p>')
                
                return '\n'.join(result_parts)
            
            return full_match
        
        # Process paragraphs that might contain lists
        # Match paragraphs containing "- " pattern
        html = re.sub(
            r'<p>((?:[^<]|<(?!\/p>))*- .+?)</p>',
            fix_paragraph_with_list,
            html,
            flags=re.MULTILINE | re.DOTALL
        )
        
        return html
    
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
    
    def preprocess_markdown(self, md_content: str) -> str:
        """Preprocess markdown to ensure lists are properly formatted"""
        lines = md_content.split('\n')
        result = []
        
        for i, line in enumerate(lines):
            # Check if current line is a list item
            is_list_item = re.match(r'^\s*- ', line) or re.match(r'^\s*\d+\.\s+', line)
            
            if is_list_item and i > 0:
                # Check if previous line is not empty and not a list item
                prev_line = lines[i-1].strip()
                if prev_line and not prev_line.endswith(':') and not re.match(r'^\s*- ', lines[i-1]) and not re.match(r'^\s*\d+\.\s+', lines[i-1]):
                    # Check if previous line doesn't end with punctuation that suggests continuation
                    if not prev_line.endswith((':', ';', ',')):
                        # This might be a list that needs a blank line before it
                        # But we'll be conservative - only add if it looks like a list start
                        pass
            
            result.append(line)
        
        return '\n'.join(result)
    
    def convert_file(self, md_file: Path):
        """Convert a single markdown file to HTML"""
        import sys
        print(f"Converting: {md_file}")
        sys.stdout.flush()
        
        # Read markdown
        try:
            with open(md_file, 'r', encoding='utf-8') as f:
                md_content = f.read()
        except Exception as e:
            print(f"  X Error reading {md_file}: {e}")
            sys.stdout.flush()
            raise
        
        # Preprocess markdown to fix list formatting
        md_content = self.preprocess_markdown(md_content)
        
        # Special handling for SynapsePDS_DB.md - inject schema from SynapsePDS_DB_scheme.md
        if md_file.name == 'SynapsePDS_DB.md':
            scheme_file = md_file.parent / 'SynapsePDS_DB_scheme.md'
            if scheme_file.exists():
                with open(scheme_file, 'r', encoding='utf-8') as f:
                    scheme_content = f.read()
                # Extract only the mermaid block from scheme file
                mermaid_match = re.search(r'(```mermaid.*?```)', scheme_content, re.DOTALL)
                if mermaid_match:
                    mermaid_block = mermaid_match.group(1)
                    # Insert mermaid block after metadata (after "Последнее изменение:" line)
                    md_content = re.sub(
                        r'(\*\*Последнее изменение:\*\*[^\n]*\n)',
                        r'\1\n' + mermaid_block + '\n',
                        md_content,
                        count=1
                    )
        
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
            # Note: nl2br conflicts with list processing, so we handle line breaks manually
            md = markdown.Markdown(extensions=[
                'extra',  # Includes: tables, fenced_code, attr_list, def_list, footnotes, abbr
                'sane_lists',  # Better list handling
            ])
            html_content = md.convert(md_content)
            
            # Convert two spaces + newline to <br> (markdown line breaks)
            html_content = re.sub(r'  \n', '<br>\n', html_content)
            
            # Fix lists that might have been broken
            html_content = self.fix_lists_in_html(html_content)
            
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
        
        # Extract h1 from content for page title
        h1_match = re.search(r'<h1>(.*?)</h1>', html_content, re.DOTALL)
        if h1_match:
            page_title = f'<h1 style="color: var(--accent-primary); font-size: 2.25em; margin: 0; padding-bottom: 15px; border-bottom: 3px solid var(--accent-primary); font-weight: normal;">{h1_match.group(1)}</h1>'
            # Remove h1 from content
            html_content = re.sub(r'<h1>.*?</h1>', '', html_content, count=1, flags=re.DOTALL)
        else:
            page_title = ''
        
        # Fill template
        html = HTML_TEMPLATE.format(
            title=title,
            css_path=css_path,
            js_path=js_path,
            root_path=root_path,
            breadcrumbs=breadcrumbs,
            github_url=github_url,
            page_title=page_title,
            content=html_content
        )
        
        # Write HTML
        try:
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(html)
            print(f"  > Created: {output_file}")
            sys.stdout.flush()
        except Exception as e:
            print(f"  X Error writing {output_file}: {e}")
            sys.stdout.flush()
            raise
    
    def convert_all(self):
        """Convert all markdown files in PRD and PDS folders"""
        import sys
        print("="*60)
        print("Synapse Documentation Converter")
        print("="*60)
        sys.stdout.flush()
        
        # Find all .md files
        md_files = []
        for folder in ['PRD', 'PDS']:
            folder_path = self.repo_root / folder
            if folder_path.exists():
                md_files.extend(folder_path.glob('*.md'))
        
        print(f"\nFound {len(md_files)} markdown files\n")
        sys.stdout.flush()
        
        # Convert each file
        for md_file in md_files:
            try:
                self.convert_file(md_file)
                sys.stdout.flush()  # Flush after each file
            except Exception as e:
                print(f"  X Error converting {md_file}: {e}")
                sys.stdout.flush()
        
        print("\n" + "="*60)
        print("Conversion complete!")
        print("="*60)
        sys.stdout.flush()


if __name__ == '__main__':
    # Get repository root (parent of INDEX directory)
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    # Create converter and run
    converter = MarkdownConverter(repo_root)
    converter.convert_all()
    
    # Update diagrams in index.html
    print("\n" + "="*60)
    print("Updating diagrams in INDEX/index.html...")
    print("="*60)
    import sys
    sys.stdout.flush()  # Принудительно отправляем вывод
    try:
        import subprocess
        script_path = os.path.join(os.path.dirname(__file__), 'update_diagrams.py')
        # Use -u flag for unbuffered output to prevent hangs in Windows
        subprocess.run(['python', '-u', script_path], check=True, timeout=60)
    except subprocess.TimeoutExpired:
        print("Warning: update_diagrams.py timed out after 60 seconds")
        print("You may need to run update_diagrams.py manually")
    except Exception as e:
        print(f"Warning: Failed to update diagrams: {e}")
        print("You may need to run update_diagrams.py manually")
    finally:
        sys.stdout.flush()  # Принудительно отправляем вывод перед завершением


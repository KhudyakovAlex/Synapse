#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Synapse Diagram Updater
Automatically updates Mermaid diagrams in INDEX/index.html from source files
"""

import re
import sys
from pathlib import Path

def extract_mermaid_from_md(md_file_path):
    """Extract Mermaid diagram content from markdown file"""
    try:
        with open(md_file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Find mermaid code block
        match = re.search(r'```mermaid\n(.*?)\n```', content, re.DOTALL)
        if match:
            return match.group(1).strip()
        return None
    except Exception as e:
        print(f"   [ERROR] Failed to read {md_file_path}: {e}")
        sys.stdout.flush()
        return None

def update_diagram_in_html(html_path, diagram_id, new_diagram_content):
    """Update a specific diagram in the HTML file"""
    try:
        with open(html_path, 'r', encoding='utf-8') as f:
            html_content = f.read()
        
        # Find the diagram div and replace its content
        pattern = rf'(<div class="mermaid" id="{diagram_id}"[^>]*>)(.*?)(</div>)'
        
        def replace_diagram(match):
            opening_tag = match.group(1)
            closing_tag = match.group(3)
            return f'{opening_tag}\n{new_diagram_content}\n            {closing_tag}'
        
        updated_html = re.sub(pattern, replace_diagram, html_content, flags=re.DOTALL)
        
        with open(html_path, 'w', encoding='utf-8') as f:
            f.write(updated_html)
        
        return html_content != updated_html
    except Exception as e:
        print(f"   [ERROR] Failed to update {html_path}: {e}")
        sys.stdout.flush()
        return False

def create_diagram_page(diagram_content, title, output_path):
    """Create a standalone HTML page for a diagram"""
    template = Path(__file__).parent / 'process_diagram.html'
    with open(template, 'r', encoding='utf-8') as f:
        html_content = f.read()
    
    # Replace placeholder with diagram content
    html_content = html_content.replace('<!-- DIAGRAM_CONTENT_PLACEHOLDER -->', diagram_content)
    html_content = html_content.replace('<title>Процесс разработки - Synapse</title>', 
                                       f'<title>{title} - Synapse</title>')
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(html_content)

def main():
    # Paths
    base_dir = Path(__file__).parent.parent
    index_html = base_dir / 'INDEX' / 'index.html'
    process_md = base_dir / 'Project' / 'process.md'
    mindmap_md = base_dir / 'Project' / 'mindmap.md'
    process_diagram_html = base_dir / 'INDEX' / 'process_diagram_full.html'
    mindmap_diagram_html = base_dir / 'INDEX' / 'mindmap_diagram_full.html'
    
    print("Synapse Diagram Updater")
    print("=" * 50)
    sys.stdout.flush()
    
    # Update process diagram
    print(f"\n1. Reading process diagram from: {process_md}")
    sys.stdout.flush()
    process_diagram = extract_mermaid_from_md(process_md)
    if process_diagram:
        # Update click links to use relative paths
        process_diagram = process_diagram.replace(
            'click PRD "https://khudyakovalex.github.io/Synapse/INDEX/PRD/SynapsePRD.html"',
            'click PRD "PRD/SynapsePRD.html"'
        )
        # Add click handlers for other nodes
        if 'click DSUI' not in process_diagram:
            process_diagram += '\n    click DSUI "PDS/SynapsePDS_APP_UI.html"'
        if 'click DSFW' not in process_diagram:
            process_diagram += '\n    click DSFW "PDS/SynapsePDS_FW.html"'
        if 'click DSAPP' not in process_diagram:
            process_diagram += '\n    click DSAPP "PDS/SynapsePDS_APP.html"'
        
        if update_diagram_in_html(index_html, 'process-diagram', process_diagram):
            print("   [OK] Process diagram updated in index.html")
        else:
            print("   [-] Process diagram unchanged in index.html")
        sys.stdout.flush()
        
        # Create standalone diagram page
        create_diagram_page(process_diagram, 'Процесс разработки', process_diagram_html)
        print("   [OK] Created process_diagram_full.html")
        sys.stdout.flush()
    else:
        print("   [FAIL] Failed to extract process diagram")
        sys.stdout.flush()
    
    # Update mindmap diagram
    print(f"\n2. Reading mindmap from: {mindmap_md}")
    sys.stdout.flush()
    mindmap_diagram = extract_mermaid_from_md(mindmap_md)
    if mindmap_diagram:
        if update_diagram_in_html(index_html, 'mindmap-diagram', mindmap_diagram):
            print("   [OK] Mindmap updated in index.html")
        else:
            print("   [-] Mindmap unchanged in index.html")
        sys.stdout.flush()
        
        # Create standalone diagram page
        create_diagram_page(mindmap_diagram, 'Мозгокарта', mindmap_diagram_html)
        print("   [OK] Created mindmap_diagram_full.html")
        sys.stdout.flush()
    else:
        print("   [FAIL] Failed to extract mindmap")
        sys.stdout.flush()
    
    print("\n" + "=" * 50)
    print("Done! Diagrams updated in INDEX/index.html")
    sys.stdout.flush()

if __name__ == '__main__':
    main()


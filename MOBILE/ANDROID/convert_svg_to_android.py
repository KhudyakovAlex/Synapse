#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Конвертация SVG иконок в Android Vector Drawable формат
"""

import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path

# Папки для обработки
FOLDERS = ["Controller", "Location", "Luminaire", "System"]

# Пути
SCRIPT_DIR = Path(__file__).parent
IMG_DIR = SCRIPT_DIR / "IMG"
OUTPUT_DIR = SCRIPT_DIR / "app" / "src" / "main" / "res" / "drawable"

def sanitize_filename(filename):
    """Преобразует имя файла в формат Android (lowercase, underscore)"""
    # Убираем расширение
    name = filename.replace('.svg', '')
    
    # Заменяем спецсимволы на пробелы
    name = re.sub(r'[,=\-\s]+', '_', name)
    
    # Убираем множественные подчеркивания
    name = re.sub(r'_+', '_', name)
    
    # Lowercase
    name = name.lower()
    
    # Убираем подчеркивания в начале и конце
    name = name.strip('_')
    
    return name + '.xml'

def rgb_to_hex(color):
    """Конвертирует rgb(r,g,b) и именованные цвета в #RRGGBB"""
    if not color or color.lower() == 'none':
        return ''
    
    # Если уже в HEX формате
    if color.startswith('#'):
        return color
    
    # Конвертируем rgb(r,g,b) в #RRGGBB
    rgb_match = re.match(r'rgb\((\d+),\s*(\d+),\s*(\d+)\)', color)
    if rgb_match:
        r, g, b = map(int, rgb_match.groups())
        return f'#{r:02X}{g:02X}{b:02X}'
    
    # Словарь именованных цветов
    named_colors = {
        'black': '#000000',
        'white': '#FFFFFF',
        'red': '#FF0000',
        'green': '#008000',
        'blue': '#0000FF',
        'yellow': '#FFFF00',
        'cyan': '#00FFFF',
        'magenta': '#FF00FF',
        'gray': '#808080',
        'grey': '#808080',
    }
    
    color_lower = color.lower()
    if color_lower in named_colors:
        return named_colors[color_lower]
    
    # Если формат неизвестен, возвращаем как есть
    return color

def parse_svg(svg_path):
    """Парсит SVG и извлекает необходимые данные"""
    try:
        tree = ET.parse(svg_path)
        root = tree.getroot()
        
        # Namespace для SVG
        ns = {'svg': 'http://www.w3.org/2000/svg'}
        
        # Извлекаем размеры
        width = root.get('width', '24')
        height = root.get('height', '24')
        
        # Убираем единицы измерения (px, dp и т.д.)
        width = re.sub(r'[^\d.]', '', width) or '24'
        height = re.sub(r'[^\d.]', '', height) or '24'
        
        # Извлекаем viewBox
        viewbox = root.get('viewBox', f'0 0 {width} {height}')
        vb_parts = viewbox.split()
        viewport_width = vb_parts[2] if len(vb_parts) >= 3 else width
        viewport_height = vb_parts[3] if len(vb_parts) >= 4 else height
        
        # Извлекаем пути
        paths = []
        for path in root.findall('.//svg:path', ns):
            path_data = path.get('d', '')
            if path_data:
                fill = rgb_to_hex(path.get('fill', '#000000'))
                stroke = rgb_to_hex(path.get('stroke', ''))
                stroke_width = path.get('stroke-width', '')
                
                paths.append({
                    'data': path_data,
                    'fill': fill,
                    'stroke': stroke,
                    'stroke_width': stroke_width
                })
        
        # Если путей нет, ищем в корне без namespace
        if not paths:
            for path in root.findall('.//path'):
                path_data = path.get('d', '')
                if path_data:
                    fill = rgb_to_hex(path.get('fill', '#000000'))
                    stroke = rgb_to_hex(path.get('stroke', ''))
                    stroke_width = path.get('stroke-width', '')
                    
                    paths.append({
                        'data': path_data,
                        'fill': fill,
                        'stroke': stroke,
                        'stroke_width': stroke_width
                    })
        
        return {
            'width': width,
            'height': height,
            'viewport_width': viewport_width,
            'viewport_height': viewport_height,
            'paths': paths
        }
    except Exception as e:
        print(f"  [ERROR] Ошибка парсинга: {e}")
        return None

def generate_android_xml(svg_data):
    """Генерирует Android Vector Drawable XML"""
    xml_lines = [
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{svg_data["width"]}dp"',
        f'    android:height="{svg_data["height"]}dp"',
        f'    android:viewportWidth="{svg_data["viewport_width"]}"',
        f'    android:viewportHeight="{svg_data["viewport_height"]}">'
    ]
    
    for path in svg_data['paths']:
        xml_lines.append('    <path')
        
        if path['fill']:
            xml_lines.append(f'        android:fillColor="{path["fill"]}"')
        
        if path['stroke']:
            xml_lines.append(f'        android:strokeColor="{path["stroke"]}"')
        
        if path['stroke_width']:
            xml_lines.append(f'        android:strokeWidth="{path["stroke_width"]}"')
        
        # pathData всегда последний атрибут
        xml_lines.append(f'        android:pathData="{path["data"]}"/>')
    
    xml_lines.append('</vector>')
    
    return '\n'.join(xml_lines)

def convert_folder(folder_name):
    """Конвертирует все SVG из папки"""
    folder_path = IMG_DIR / folder_name
    
    if not folder_path.exists():
        print(f"  [SKIP] Папка не найдена: {folder_name}")
        return 0, 0
    
    svg_files = list(folder_path.glob('*.svg'))
    
    if not svg_files:
        print(f"  [SKIP] SVG файлы не найдены в: {folder_name}")
        return 0, 0
    
    success_count = 0
    error_count = 0
    prefix = folder_name.lower()
    
    for svg_file in svg_files:
        try:
            # Генерируем имя выходного файла
            sanitized_name = sanitize_filename(svg_file.name)
            output_name = f"{prefix}_{sanitized_name}"
            output_path = OUTPUT_DIR / output_name
            
            # Парсим SVG
            svg_data = parse_svg(svg_file)
            
            if not svg_data or not svg_data['paths']:
                print(f"  [ERROR] {svg_file.name} -> Нет путей в SVG")
                error_count += 1
                continue
            
            # Генерируем Android XML
            android_xml = generate_android_xml(svg_data)
            
            # Сохраняем
            output_path.write_text(android_xml, encoding='utf-8')
            
            print(f"  [OK] {svg_file.name} -> {output_name}")
            success_count += 1
            
        except Exception as e:
            print(f"  [ERROR] {svg_file.name} -> {e}")
            error_count += 1
    
    return success_count, error_count

def main():
    print("Начало конвертации SVG -> Android XML\n")
    
    # Проверяем наличие папок
    if not IMG_DIR.exists():
        print(f"[ERROR] Папка IMG не найдена: {IMG_DIR}")
        return
    
    # Создаём выходную папку если нужно
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    
    print("Установка зависимостей...")
    print("[OK] Встроенные библиотеки доступны\n")
    
    # Конвертируем каждую папку
    total_success = 0
    total_error = 0
    
    for folder in FOLDERS:
        print(f"Обработка папки: {folder}")
        success, errors = convert_folder(folder)
        total_success += success
        total_error += errors
        print()
    
    # Итоги
    print("=" * 60)
    print("Результаты конвертации:")
    print(f"   Всего файлов: {total_success + total_error}")
    print(f"   Успешно: {total_success}")
    print(f"   Ошибок: {total_error}")
    print("=" * 60)
    print(f"\nРезультаты сохранены в: {OUTPUT_DIR}")
    print("Готово!")

if __name__ == "__main__":
    main()

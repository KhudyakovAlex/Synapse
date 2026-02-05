#!/usr/bin/env python3
"""
Конвертер SVG в Android Vector Drawable XML
Автоматически обрабатывает все SVG из папки IMG/ и создает соответствующие папки в drawable/
"""

import os
import subprocess
import sys
from pathlib import Path

# Пути
PROJECT_ROOT = Path(__file__).parent
IMG_DIR = PROJECT_ROOT / "IMG"
DRAWABLE_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "drawable"

# Папки для обработки
FOLDERS = ["Controller", "Location", "Luminaire", "System"]

def install_dependencies():
    """Установка необходимых библиотек"""
    print("Установка зависимостей...")
    try:
        import xml.etree.ElementTree as ET
        print("[OK] Встроенные библиотеки доступны\n")
    except ImportError:
        print("[ERROR] Ошибка импорта библиотек")
        sys.exit(1)

def sanitize_filename(name):
    """Преобразование имени файла в формат Android (lowercase, underscore)"""
    # Удаляем расширение
    name = name.replace('.svg', '')
    # Заменяем пробелы и спецсимволы на подчеркивание
    name = name.replace(' ', '_').replace('-', '_').replace(',', '_').replace('=', '_')
    # Приводим к lowercase
    name = name.lower()
    # Удаляем повторяющиеся подчеркивания
    while '__' in name:
        name = name.replace('__', '_')
    return name

def convert_svg_to_xml(svg_path, output_dir, folder_prefix):
    """Конвертация одного SVG файла в Android XML"""
    try:
        import xml.etree.ElementTree as ET
        import re
        
        # Получаем имя файла
        filename = svg_path.stem
        sanitized_name = sanitize_filename(filename)
        
        # Добавляем префикс папки для уникальности
        output_name = f"{folder_prefix}_{sanitized_name}.xml"
        output_path = output_dir / output_name
        
        # Читаем SVG
        with open(svg_path, 'r', encoding='utf-8') as f:
            svg_content = f.read()
        
        # Парсим SVG
        # Удаляем namespace для упрощения парсинга
        svg_content_clean = re.sub(r'xmlns[^=]*="[^"]*"', '', svg_content)
        root = ET.fromstring(svg_content_clean)
        
        # Получаем размеры
        width = root.get('width', '24')
        height = root.get('height', '24')
        viewBox = root.get('viewBox', f'0 0 {width} {height}')
        
        # Удаляем единицы измерения
        width = re.sub(r'[^0-9.]', '', str(width))
        height = re.sub(r'[^0-9.]', '', str(height))
        
        if not width or not height:
            width = height = '24'
        
        # Создаем Android Vector Drawable
        vector_xml = f'''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{width}dp"
    android:height="{height}dp"
    android:viewportWidth="{viewBox.split()[2] if len(viewBox.split()) > 2 else width}"
    android:viewportHeight="{viewBox.split()[3] if len(viewBox.split()) > 3 else height}">
'''
        
        # Извлекаем пути
        paths = root.findall('.//path')
        for path in paths:
            path_data = path.get('d', '')
            fill = path.get('fill', '#000000')
            stroke = path.get('stroke', '')
            stroke_width = path.get('stroke-width', '')
            
            if path_data:
                vector_xml += f'    <path\n'
                if fill and fill != 'none':
                    vector_xml += f'        android:fillColor="{fill}"\n'
                if stroke and stroke != 'none':
                    vector_xml += f'        android:strokeColor="{stroke}"\n'
                    if stroke_width:
                        vector_xml += f'        android:strokeWidth="{stroke_width}"\n'
                vector_xml += f'        android:pathData="{path_data}"/>\n'
        
        vector_xml += '</vector>\n'
        
        # Сохраняем
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(vector_xml)
        
        return True, output_name
    except Exception as e:
        return False, str(e)

def main():
    print("Начало конвертации SVG -> Android XML\n")
    
    # Проверка наличия папки IMG
    if not IMG_DIR.exists():
        print(f"[ERROR] Папка {IMG_DIR} не найдена")
        sys.exit(1)
    
    # Установка зависимостей
    install_dependencies()
    
    # Создание папки drawable если не существует
    DRAWABLE_DIR.mkdir(parents=True, exist_ok=True)
    
    # Статистика
    total_files = 0
    success_count = 0
    error_count = 0
    errors = []
    
    # Обработка каждой папки
    for folder in FOLDERS:
        folder_path = IMG_DIR / folder
        if not folder_path.exists():
            print(f"[WARN] Папка {folder} не найдена, пропускаем")
            continue
        
        print(f"Обработка папки: {folder}")
        
        # Получаем все SVG файлы
        svg_files = list(folder_path.glob("*.svg"))
        total_files += len(svg_files)
        
        # Префикс для имен файлов (lowercase)
        prefix = folder.lower()
        
        # Конвертация каждого файла
        for svg_file in svg_files:
            success, result = convert_svg_to_xml(svg_file, DRAWABLE_DIR, prefix)
            
            if success:
                success_count += 1
                print(f"  [OK] {svg_file.name} -> {result}")
            else:
                error_count += 1
                error_msg = f"{svg_file.name}: {result}"
                errors.append(error_msg)
                print(f"  [ERROR] {error_msg}")
        
        print()
    
    # Итоговая статистика
    print("=" * 60)
    print(f"Результаты конвертации:")
    print(f"   Всего файлов: {total_files}")
    print(f"   Успешно: {success_count}")
    print(f"   Ошибок: {error_count}")
    print("=" * 60)
    
    if errors:
        print("\nСписок ошибок:")
        for error in errors:
            print(f"   - {error}")
    
    print(f"\nРезультаты сохранены в: {DRAWABLE_DIR}")
    print("Готово!")

if __name__ == "__main__":
    main()

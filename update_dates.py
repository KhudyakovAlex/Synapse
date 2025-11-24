#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для автоматического обновления даты изменения в MD-файлах
Обновляет только файлы, которые были изменены и добавлены в Git индекс
"""

import re
import subprocess
from datetime import datetime
from pathlib import Path

def get_staged_md_files():
    """Получить список MD-файлов, добавленных в индекс Git"""
    result = subprocess.run(
        ['git', 'diff', '--cached', '--name-only', '--diff-filter=ACM'],
        capture_output=True,
        text=True,
        encoding='utf-8'
    )
    files = result.stdout.strip().split('\n')
    # Фильтруем только MD-файлы в нужных директориях
    md_files = [f for f in files if f.endswith('.md') and (
        f.startswith('PRD/') or 
        f.startswith('PDS/') or
        f.startswith('MOBILE/')
    )]
    return [f for f in md_files if f]  # Убираем пустые строки

def update_date_in_file(filepath):
    """Обновить дату в файле"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Проверяем, есть ли строка с "Последнее изменение"
        if '**Последнее изменение:**' not in content:
            return False
        
        # Получаем текущую дату в формате DD.MM.YYYY
        current_date = datetime.now().strftime('%d.%m.%Y')
        
        # Заменяем дату
        pattern = r'\*\*Последнее изменение:\*\* \d{2}\.\d{2}\.\d{4}'
        replacement = f'**Последнее изменение:** {current_date}'
        new_content = re.sub(pattern, replacement, content)
        
        # Если дата изменилась, записываем файл
        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            
            # Добавляем обновленный файл в индекс
            subprocess.run(['git', 'add', filepath])
            print(f'✓ Обновлена дата в файле: {filepath}')
            return True
    except Exception as e:
        print(f'✗ Ошибка при обновлении {filepath}: {e}')
    
    return False

def main():
    """Основная функция"""
    print('=' * 60)
    print('Автоматическое обновление дат в документах')
    print('=' * 60)
    
    staged_files = get_staged_md_files()
    
    if not staged_files:
        print('Нет измененных MD-файлов для обновления')
        return 0
    
    print(f'\nНайдено файлов для проверки: {len(staged_files)}')
    
    updated_count = 0
    for filepath in staged_files:
        if update_date_in_file(filepath):
            updated_count += 1
    
    if updated_count > 0:
        print(f'\n✓ Автоматически обновлено дат: {updated_count}')
    else:
        print('\nДаты не требуют обновления')
    
    print('=' * 60)
    return 0

if __name__ == '__main__':
    exit(main())


#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для автоматического обновления даты изменения в MD-файлах
Обновляет только файлы, которые были изменены и добавлены в Git индекс
"""

import re
import subprocess
import sys
from datetime import datetime
from pathlib import Path

def get_repo_root():
    """Получить корневую директорию Git репозитория"""
    result = subprocess.run(
        ['git', 'rev-parse', '--show-toplevel'],
        capture_output=True,
        text=True,
        encoding='utf-8',
        timeout=10
    )
    if result.returncode != 0:
        raise RuntimeError("Не удалось определить корень Git репозитория")
    return Path(result.stdout.strip())

def get_staged_md_files():
    """Получить список MD-файлов, добавленных в индекс Git"""
    result = subprocess.run(
        ['git', 'diff', '--cached', '--name-only', '--diff-filter=ACM'],
        capture_output=True,
        text=True,
        encoding='utf-8',
        timeout=10
    )
    files = result.stdout.strip().split('\n')
    # Фильтруем только MD-файлы в нужных директориях
    md_files = [f for f in files if f.endswith('.md') and (
        f.startswith('PRD/') or 
        f.startswith('PDS/') or
        f.startswith('MOBILE/')
    )]
    return [f for f in md_files if f]  # Убираем пустые строки

def update_date_in_file(repo_root, relative_path):
    """Обновить дату в файле"""
    filepath = repo_root / relative_path
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
            
            # Добавляем обновленный файл в индекс (используем относительный путь для Git)
            subprocess.run(['git', 'add', relative_path], cwd=repo_root, timeout=10)
            print(f'[OK] Обновлена дата в файле: {relative_path}')
            sys.stdout.flush()  # Принудительно отправляем вывод
            return True
    except Exception as e:
        print(f'[ERROR] Ошибка при обновлении {relative_path}: {e}')
    
    return False

def main():
    """Основная функция"""
    print('=' * 60)
    print('Автоматическое обновление дат в документах')
    print('=' * 60)
    
    try:
        repo_root = get_repo_root()
    except RuntimeError as e:
        print(f'[ERROR] {e}')
        return 1
    
    staged_files = get_staged_md_files()
    
    if not staged_files:
        print('Нет измененных MD-файлов для обновления')
        return 0
    
    print(f'\nНайдено файлов для проверки: {len(staged_files)}')
    sys.stdout.flush()  # Принудительно отправляем вывод
    
    updated_count = 0
    for relative_path in staged_files:
        if update_date_in_file(repo_root, relative_path):
            updated_count += 1
        sys.stdout.flush()  # Принудительно отправляем вывод после каждого файла
    
    if updated_count > 0:
        print(f'\n[OK] Автоматически обновлено дат: {updated_count}')
    else:
        print('\nДаты не требуют обновления')
    
    print('=' * 60)
    print('Скрипт завершён успешно')
    sys.stdout.flush()  # Принудительно отправляем весь вывод
    return 0

if __name__ == '__main__':
    exit_code = main()
    sys.stdout.flush()  # Принудительно отправляем весь вывод перед выходом
    sys.exit(exit_code)


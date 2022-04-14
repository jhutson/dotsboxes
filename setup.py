#!/usr/bin/env python
from setuptools import find_packages
from setuptools import setup

setup(
    name='dotsquares',
    version='1.0.0.dev1',
    description='Dots and Squares Game',
    long_description='Core game logic for Dots and Squares game.',
    long_description_content_type='text/plain',
    author='Jimmy Hutson',
    author_email='jimmy@hutsonandfriends.com',
    license='GPL-3.0-only',
    classifiers=[
        'Development Status :: 1 - Planning',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: GNU General Public License v3 (GPLv3)',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.10',
        'Topic :: Games/Entertainment'
    ],
    keywords='game dots squares',
    python_requires='>=3'
)
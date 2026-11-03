#!/usr/bin/env python3
"""
Syntax-checks every Java file with a real Java parser (tree-sitter). This is NOT a compiler:
it catches unbalanced braces, missing semicolons, bad text blocks etc., but not type errors or
missing imports. Use it when no Maven/JDK with network access is available.

    pip install tree-sitter tree-sitter-java
    python3 infra/tools/check_java_syntax.py
"""
import glob, os, sys
import tree_sitter_java
from tree_sitter import Language, Parser

parser = Parser(Language(tree_sitter_java.language()))
root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', 'backend'))

def first_error(node):
    if node.type == 'ERROR' or node.is_missing: return node
    for c in node.children:
        if c.has_error or c.is_missing:
            e = first_error(c)
            if e: return e
    return None

files = sorted(glob.glob(f'{root}/**/*.java', recursive=True)); bad = 0
for f in files:
    tree = parser.parse(open(f, 'rb').read())
    if tree.root_node.has_error:
        bad += 1; e = first_error(tree.root_node); line = e.start_point[0] + 1 if e else '?'
        print(f"SYNTAX ERROR  {os.path.relpath(f, root)}:{line}")
print(f"{len(files)} Java files checked, {bad} with syntax errors")
sys.exit(1 if bad else 0)

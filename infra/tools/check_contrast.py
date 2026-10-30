#!/usr/bin/env python3
"""
WCAG contrast check for every text/background pair in frontend/src/index.css, across
light/dark x amber/indigo/teal. Reads the CSS variables directly, so it can't drift from what ships.

    python3 infra/tools/check_contrast.py        # exits 1 if any pair fails

AA needs 4.5:1 for normal text and 3:1 for UI boundaries (e.g. input borders, focus rings).
"""
import os, re, sys
css = open(os.path.join(os.path.dirname(__file__), '..', '..', 'frontend', 'src', 'index.css')).read()

def block(selector_regex):
    m = re.search(selector_regex + r'\s*\{(.*?)\}', css, re.S)
    return dict(re.findall(r'--([\w-]+)\s*:\s*(#[0-9A-Fa-f]{6})', m.group(1))) if m else {}

base = {'light': block(r':root, \[data-theme="light"\]'), 'dark': block(r'\[data-theme="dark"\]')}
accents = {(t, a): block(rf'\[data-theme="{t}"\]\[data-accent="{a}"\]') for t in base for a in ('indigo', 'teal')}

def lum(h):
    c = [int(h[i:i+2], 16) / 255 for i in (1, 3, 5)]
    c = [x / 12.92 if x <= .03928 else ((x + .055) / 1.055) ** 2.4 for x in c]
    return .2126 * c[0] + .7152 * c[1] + .0722 * c[2]
def ratio(a, b):
    la, lb = sorted((lum(a), lum(b)), reverse=True); return (la + .05) / (lb + .05)

TEXT = 4.5; UI = 3.0
def pairs(v):
    W = '#FFFFFF'
    out = []
    for fg in ('ink', 'ink-soft', 'muted'):
        for bg in ('bg', 'surface', 'surface-2'): out.append((fg, bg, TEXT))
    for bg in ('bg', 'surface', 'accent-soft'): out.append(('accent', bg, TEXT))
    out += [('on-accent', 'accent-solid', TEXT), ('accent', 'surface-2', TEXT)]
    for fg, soft in (('success', 'success-soft'), ('danger', 'danger-soft'), ('info', 'info-soft')):
        for bg in (soft, 'surface', 'bg'): out.append((fg, bg, TEXT))
    for solid in ('solid-green', 'solid-amber', 'solid-purple', 'solid-red'): out.append((W, solid, TEXT))
    for p in ('student', 'teacher', 'admin'):
        for end in ('from', 'to'):
            out += [('brand-text', f'brand-{p}-{end}', TEXT), ('brand-text-soft', f'brand-{p}-{end}', TEXT)]
    out += [('side-text', 'side-bg', TEXT), ('side-muted', 'side-bg', TEXT), (W, 'side-active', TEXT), ('side-text', 'side-active', TEXT)]
    out += [('field', 'bg', UI), ('field', 'surface', UI), ('accent', 'bg', UI), ('accent', 'surface', UI)]
    return out

fails = checked = 0; worst = (99, '')
for theme in ('light', 'dark'):
    for accent in ('amber', 'indigo', 'teal'):
        v = dict(base[theme]); v.update(accents.get((theme, accent), {}))
        for fg, bg, need in pairs(v):
            f = fg if fg.startswith('#') else v.get(fg); b = v.get(bg) if not bg.startswith('#') else bg
            if f is None or b is None: continue   # dark theme reuses brand/light-only tokens
            r = ratio(f, b); checked += 1
            if r < worst[0]: worst = (r, f'{theme}/{accent}: {fg} on {bg}')
            if r < need:
                fails += 1; print(f"FAIL {theme:5}/{accent:6}  {fg:16} on {bg:16} {r:5.2f}:1 (needs {need})")
print(f"{checked} colour pairs checked across 6 theme combinations; {fails} below WCAG AA")
print(f"lowest ratio: {worst[0]:.2f}:1  ({worst[1]})")
sys.exit(1 if fails else 0)

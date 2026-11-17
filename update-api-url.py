#!/usr/bin/env python3
"""
update-api-url.py — Inject the production backend URL into all HTML files.

Usage (run once after deploying backend):
    python update-api-url.py https://joash-portfolio-backend.onrender.com

This updates the <meta name="api-base-url" content="..."> tag in every
frontendport HTML file so the frontend talks to your Render backend.
"""

import sys
import os
import re
import pathlib

def update_api_url(new_url: str, root: str = "frontendport") -> None:
    root_path = pathlib.Path(root)
    if not root_path.exists():
        print(f"ERROR: Directory '{root}' not found. Run from portfolio root.")
        sys.exit(1)

    pattern = re.compile(r'(<meta\s+name=["\']api-base-url["\']\s+content=["\'])([^"\']*)(["\']\s*>)', re.IGNORECASE)
    updated = []

    for html_file in root_path.rglob("*.html"):
        text = html_file.read_text(encoding="utf-8")
        new_text, count = pattern.subn(rf'\g<1>{new_url}\g<3>', text)
        if count > 0:
            html_file.write_text(new_text, encoding="utf-8")
            updated.append(str(html_file))
            print(f"  ✓ Updated: {html_file}")

    if not updated:
        print("No HTML files contained an api-base-url meta tag. Add them first.")
    else:
        print(f"\nUpdated {len(updated)} file(s) → API base URL set to: {new_url}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python update-api-url.py <backend-url>")
        print("Example: python update-api-url.py https://joash-portfolio-backend.onrender.com")
        sys.exit(1)
    update_api_url(sys.argv[1])

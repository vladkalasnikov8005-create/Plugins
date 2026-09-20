#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Простой статический сервер для сайта-каталога.

    python3 serve.py [порт]        # по умолчанию 8123
"""
import functools
import http.server
import os
import socketserver
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "site")


class Handler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()

    def log_message(self, fmt, *args):
        pass


def main():
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8123
    handler = functools.partial(Handler, directory=ROOT)
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.ThreadingTCPServer(("0.0.0.0", port), handler) as httpd:
        print(f"KSEPSP-каталог: http://0.0.0.0:{port}/  (корень: {ROOT})", flush=True)
        httpd.serve_forever()


if __name__ == "__main__":
    main()

#!/usr/bin/python

import SimpleHTTPServer
import SocketServer
import sys, os, os.path
import signal, thread

os.chdir(os.path.dirname(os.path.abspath(sys.argv[0])))

NETWORK = '127.0.0.1'
PORT = 40080

# Redirect logging in the handler from stderr to a log file
sys.stderr = open('webserve.log', 'w')

Handler = SimpleHTTPServer.SimpleHTTPRequestHandler
Handler.server_version = 'docker-common/1.0'
Handler.sys_version = ''

SocketServer.TCPServer.allow_reuse_address = True
httpd = SocketServer.TCPServer((NETWORK, PORT), Handler)

def on_sigterm(signum, stackframe):
    def kill_httpd():
        httpd.shutdown()
        httpd.server_close()
        sys.exit(0)
    thread.start_new_thread(kill_httpd, ())
signal.signal(signal.SIGTERM, on_sigterm)

print('Serving files over HTTP on {} port {}'.format(NETWORK, PORT))
httpd.serve_forever(poll_interval=0.5)

#!/usr/bin/env python

import requests

def send_message(action_urls, request_path, to_wds, headers, **kwargs):
    if to_wds is not None:
        kwargs['toWds'] = to_wds
        
    request_path = action_urls[0] + request_path
    response = requests.post(request_path, headers=headers, params=kwargs)
    return CollectorSyncResponse(request_path, response)
    
class CollectorSyncResponse:
    
    def __init__(self, request_path, response):
        self.success = response.status_code == 200
        self.request_path = request_path
        self.status_code = response.status_code
        if not self.success:
            self.error_body = response.text
        self.json_response = response.json()
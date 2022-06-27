## Copyright (C) SHOUT TV, Inc - All Rights Reserved
## Unauthorized copying of this file, via any medium is strictly prohibited
## Proprietary and confidential

import requests
import time

def send_message(action_urls, request_path, to_wds, headers, **kwargs):
    """Send a message to a SHOUT collector.

    Args:
        action_urls (List[str]): A list of collector URLs from the SRD.
        request_path (str): The collector request path to append to the URL.
        to_wds (str): A SHOUT document server domain name where the response
            document will be published. Value must be provided by a SRD object.
        headers (Dict[str,str]): Dictionary of key-value pairs representing HTTP headers
            to send in the request.
        **kwargs: Key-value pairs representing post variables to send in the request.

    Returns:
        A :class:`~.CollectorResponse` object.
    """
    if to_wds is not None:
        kwargs['toWds'] = to_wds
    request_path = action_urls[0] + request_path
    response = requests.post(request_path, headers=headers, params=kwargs)
    return CollectorResponse(to_wds, request_path, response)

class CollectorResponse:
    """Class that represents the server's response from a SHOUT collector server.
    
    *Constructor is private --- use the* :func:`.send_message` *function to generate
    CollectorResponse objects*

    Args:
        wds_url: The WDS (Web Data Store) URL where the response document will
            eventually appear.
        response: The response object representing the HTTP response.
    """

    POST_REQUEST, PRE_RESPONSE_JSON, POST_RESPONSE_JSON = range(3)
    #WAIT_TIMES = [0.5, 1.0, 2.0, 4.0, 5.0]
    WAIT_TIMES = [0.1, 0.25, 0.5, 1.0, 2.0, 4.0, 5.0]

    def __init__(self, wds_url, request_path, response):
        self.state = CollectorResponse.POST_REQUEST
        self.wds_url = wds_url
        self.request_path = request_path
        self.encryptKey = None
        self.collector_response = response
        self.json_response = None
        self.success = response.status_code == 200
        self.status_code = response.status_code
        if not self.success:
            self.error_body = response.text

    def wait_json(self):
        """Block the current thread until the response JSON docmument has been received.

        After this method returns, the :meth:`json` method may be used.

        Raises:
            IllegalStateException: The request somehow was not sent.
                :func:`.send_message` should be the only source of
                CollectorResponse objects.
            TimeoutException: The response document could not be accessed within
                the timeout period.
            HTTPError: An unsuccessful HTTP response was received.
        """
        if not self.success:
            raise IllegalStateException('initial request failed')
        if self.state == CollectorResponse.POST_RESPONSE_JSON:
            return

        self.state = CollectorResponse.PRE_RESPONSE_JSON

        #TODO: use the standard url library
        collector_json = self.collector_response.json()
        if 'encryptKey' in collector_json:
            self.encryptKey = collector_json['encryptKey']

        if 'estimatedWaitTime' in collector_json:
            time.sleep(float(collector_json['estimatedWaitTime']) / 1000)
        i = 0
        while True:
            #TODO: use url lib for this
            self.json_response = requests.get('http://' + self.wds_url + '/' + collector_json['ticket'] + '/response.json')
            self.status_code = self.json_response.status_code
            self.success = self.json_response.status_code == 200
            if self.success:
                break
            elif self.json_response.status_code == 404:
                if i >= len(CollectorResponse.WAIT_TIMES):
                    raise TimeoutException()
                time.sleep(CollectorResponse.WAIT_TIMES[i])
                i += 1
            else:
                self.json_response.raise_for_status()

        self.state = CollectorResponse.POST_RESPONSE_JSON

    def json(self):
        """Convert the HTTP response to a JSON object.

        Returns:
            Dict: The JSON object.

        Raises:
            IllegalStateException: The response has not yet been received. Use
                :meth:`wait_json` first.
            ValueError: A JSON decoding error.
        """
        if self.state != CollectorResponse.POST_RESPONSE_JSON:
            raise IllegalStateException('no response json available yet')
        return self.json_response.json()

class IllegalStateException(RuntimeError):
    """The attempted operation would have resulted in an invalid state in the
    current object."""
    def __init__(self, message):
        super(RuntimeError, self).__init__(message)

class TimeoutException(RuntimeError):
    """A timeout occurred."""
    pass

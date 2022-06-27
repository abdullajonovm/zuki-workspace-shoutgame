#!/usr/bin/python

import time
import urllib
import base64
import hmac
import hashlib

def aws_signed_request(paramMap, public_key, private_key, endPoint='ec2.amazonaws.com', version='2013-02-01'):
    
    """
    Copyright (c) 2010-2012 Ulrich Mierendorff

    Permission is hereby granted, free of charge, to any person obtaining a
    copy of this software and associated documentation files (the "Software"),
    to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense,
    and/or sell copies of the Software, and to permit persons to whom the
    Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
    THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
    FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
    DEALINGS IN THE SOFTWARE.
    """
    
    """
    Parameters:
        paramMap - a dictionary of parameters, for example
                    {'Operation': 'ItemLookup',
                     'ItemId': 'B000X9FLKM',
                     'ResponseGroup': 'Small'}
        public_key - your "Access Key ID"
        private_key - your "Secret Access Key"
        endPoint [optional] - default ec2.amazonaws.com
        version [optional] - default 2013-02-01
    """
    
    """
    example
    
    https://ec2.amazonaws.com/?AWSAccessKeyId=TheAccessId&Action=DescribeInstances&Filter.1.Name=group-name&Fi
lter.1.Value.1=test-1&Filter.2.Name=instance-state-code&Filter.2.Value.1=16&SignatureMethod=HmacSHA256&SignatureVer
sion=2&Timestamp=2014-02-06T23%3A46%3A46Z&Version=2013-02-01&Signature=6kWrV3zqpr44dhCd9jEpP00AcLBZ82L9B01Bdbn%2B62
U%3D
    """
    
    
    # some paramters
    method = 'GET'
    host = endPoint.strip()
    uri = '/'
    
    # additional parameters
    paramMap['SignatureVersion'] = '2'
    paramMap['SignatureMethod'] = 'HmacSHA256'
    paramMap['AWSAccessKeyId'] = public_key
    paramMap['Timestamp'] = time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())
    paramMap['Version'] = version
    
    # create the canonicalized query
    canonicalized_query = [urllib.quote(param).replace('%7E', '~') + '=' + urllib.quote(paramMap[param], safe = '').replace('%7E', '~')
                            for param in sorted(paramMap.keys())]
    canonicalized_query = '&'.join(canonicalized_query)
    
    # create the string to sign
    string_to_sign = method + '\n' + host + '\n' + uri + '\n' + canonicalized_query;
    
    # calculate HMAC with SHA256 and base64-encoding
    signature = base64.b64encode(hmac.new(key=private_key, msg=string_to_sign, digestmod=hashlib.sha256).digest())
    
    # encode the signature for the request
    signature = urllib.quote(signature).replace('%7E', '~')

    return 'https://' + host + uri + '?' + canonicalized_query + '&Signature=' + signature

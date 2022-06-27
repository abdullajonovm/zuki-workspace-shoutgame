## Copyright (C) SHOUT TV, Inc - All Rights Reserved
## Unauthorized copying of this file, via any medium is strictly prohibited
## Proprietary and confidential

import base64, os
import scrypt

from Crypto import Random
from Crypto.Cipher import AES

def scrypt_encrypt(password, N=15, r=8, p=1):
    """One-way encrypt a user generated password using the scrypt algorithm.

    Args:
        password (str): The user generated password to encrypt.
        N (int): Optional. The scrypt parameter controlling CPU cost
        r (int): Optional. The scrypt parameter controlling memory cost
        p (int): Optional. The scrypt parameter controlling parallelization.

    Returns:
        str: An encrypted string of the user generated password.
    """
    salt = os.urandom(64)
    hash = scrypt.hash(password, salt, N=2**N, r=r, p=p)

    params = hex(N << 16L | r << 8 | p)[2:-1]
    result = "$s0$" + params + "$" + base64.b64encode(salt) + "$" + base64.b64encode(hash)
    return result




BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
unpad = lambda s : s[0:-ord(s[-1])]

def aes_decrypt(key, enc):
    enc = base64.b64decode(enc)
    pp = key[:16]
    iv = key[16:]
    cipher = AES.new(pp, AES.MODE_CBC, iv )
    return unpad(cipher.decrypt( enc ))

def aes_encrypt(key, raw):
    raw = pad(raw)
    pp = key[:16]
    iv = key[16:]
    cipher = AES.new( pp, AES.MODE_CBC, iv )
    return base64.b64encode( iv + cipher.encrypt( raw ) )
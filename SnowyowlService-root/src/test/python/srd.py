## Copyright (C) SHOUT TV, Inc - All Rights Reserved
## Unauthorized copying of this file, via any medium is strictly prohibited
## Proprietary and confidential

import requests
import hashlib, random

class SRD:
    """Queries a remote SRD document.

    Args:
        srd_endpoint (str): A URL string to an SRD document. Should use values from
            :class:`.env`.
    """
    def __init__(self, srd_endpoint):
        self.srd_endpoint = srd_endpoint
        self.srd_json = self.fetch_srd()

    def fetch_srd(self):
        """Downloads and parses the remote SRD document.
        
        Raises:
            InvalidSrdException: If the SRD document cannot be parsed.
        """
        response = requests.get(self.srd_endpoint)
        response.raise_for_status()
        json = response.json()
        try:
            if json['docType'] != 'srd/2.0':
                raise InvalidSrdException('invalid srd docType: ' + json['docType'])
        except KeyError:
            raise InvalidSrdException('invalid srd: missing docType')
        return response.json()

    def action(self, action, shout_subscriber_uuid):
        """Retrieves the specified action from the current SRD document for the specified user.

        Args:
            action (str): The action name to retrieve.
            shout_subscriber_uuid: The unique SHOUT identifier for the current
                subscriber, or ``None`` if the current action isn't applicable
                to a specific subscriber.

        Returns:
            List[str]: A list of SHOUT server URLs to be used to perform the specified action.
        """
        try:
            server_set_name = self.srd_json['action'][action]
        except KeyError as e:
            raise InvalidSrdException('srd missing element: ' + str(e[0]))
        domain_set = self.domain_set(server_set_name, shout_subscriber_uuid)
        #TODO: use the url library for this
        return ['https://'+d for d in domain_set]

    def domain_set(self, server_set_name, shout_subscriber_uuid):
        try:
            server_set = self.srd_json['server'][server_set_name]
            select_method = server_set['selectMethod']
            domain_sets = server_set['domainNameSets']
        except KeyError as e:
            raise InvalidSrdException('srd missing element: ' + str(e[0]))

        if select_method != 'sha256%4':
            raise InvalidSrdException('unknown select method: ' + str(select_method))
        if type(domain_sets) != list or len(domain_sets) == 0:
            raise InvalidSrdException('invalid domain name sets: ' + repr(domain_sets))

        if shout_subscriber_uuid is None:
            domain_set_index = random.randint(0, len(domain_sets)-1)
        else:
            sha = hashlib.sha256()
            sha.update(shout_subscriber_uuid)
            sha_hex = sha.hexdigest()
            dividend = int(sha_hex[:8], 16)
            domain_set_index = dividend % len(domain_sets)
        domain_set = domain_sets[domain_set_index]

        return domain_set

    def wds(self, shout_subscriber_uuid):
        """Retrieves the appropriate WDS (Web Data Store) URL for the specified subscriber.

        Args:
            shout_subscriber_uuid: The unique SHOUT identifier for the current
                subscriber, or ``None`` if the current action isn't applicable
                to a specific subscriber.

        Returns:
            str: The WDS URL for the specified subscriber.
        """
        srd_entry = self.domain_set('wds', shout_subscriber_uuid)[0]
        return srd_entry.split(':')[0]

class InvalidSrdException(RuntimeError):
    """Error representing an invalid SRD document."""
    def __init__(self, message):
        super(RuntimeError, self).__init__(message)

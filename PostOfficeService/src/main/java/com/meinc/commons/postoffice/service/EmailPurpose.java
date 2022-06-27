package com.meinc.commons.postoffice.service;

public enum EmailPurpose {
  /**
   * An unsolicited promotional email. Unsolicited means the recipient is not
   * presently opted-in to receive promotional email. Promotional means the
   * email primarily promotes (markets) a product or service.
   */
  BULK,

  /**
   * A solicited promotional email. Solicited means the recipient is presently
   * opted-in to receive promotional email. Promotional means means the email
   * primarily promotes (markets) a product or service.
   */
  PROMOTIONAL,

  /**
   * A solicited transactional email. Solicited means the recipient is presently
   * opted-in to receive transactional email. Transactional means the email
   * primarily assists the recipient in fulfilling or managing an agreed upon
   * product or service.
   */
  TRANSACTIONAL
}

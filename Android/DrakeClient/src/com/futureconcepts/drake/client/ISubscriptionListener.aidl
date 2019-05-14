package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.Contact;

oneway interface ISubscriptionListener
{
    /**
     * Called when:
     *  <ul>
     *  <li> the request a contact has sent to client
     *  </ul>
     *
     * @see info.guardianproject.otr.app.im.engine.SubscriptionRequestListener#onSubScriptionRequest(Contact from)
     */
    void onSubScriptionRequest(in Contact from);

    /**
     * Called when the request is approved by user.
     *
     * @see info.guardianproject.otr.app.im.engine.SubscriptionRequestListener#onSubscriptionApproved(String contact)
     */
    void onSubscriptionApproved(String contact);

    /**
     * Called when a subscription request is declined.
     *
     * @see info.guardianproject.otr.app.im.engine.ContactListListener#onSubscriptionDeclined(String contact)
     */
    void onSubscriptionDeclined(String contact);
}

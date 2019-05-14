package com.futureconcepts.drake.client;

oneway interface IInvitationListener
{
    /**
     * Called when a new invitation received.
     *
     * @param id the id of the invitation in content provider.
     */
    void onGroupInvitation(long id);
}

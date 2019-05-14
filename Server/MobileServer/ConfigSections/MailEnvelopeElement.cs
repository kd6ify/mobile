using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Configuration;
using System.Linq;
using System.Net;
using System.Net.Mail;
using System.Text;

namespace FutureConcepts.Mobile.Server.ConfigSections
{
    public class MailEnvelopeElement : ConfigurationElement
    {
        [ConfigurationProperty("subject", IsRequired = true)]
        public String Subject
        {
            get
            {
                return (String)this["subject"];
            }
        }


        [ConfigurationProperty("from", DefaultValue = "software@futurec.net", IsRequired = false)]
        public String From
        {
            get
            {
                return (String)this["from"];
            }
        }
    }
}

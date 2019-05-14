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
    public class MailSection : ConfigurationSection
    {
        [ConfigurationProperty("envelope", IsRequired = true)]
        public MailEnvelopeElement Envelope
        {
            get
            {
                return (MailEnvelopeElement)this["envelope"];
            }
        }

        public static MailSection GetSection(String sectionName)
        {
            return (MailSection)ConfigurationManager.GetSection(sectionName);
        }
    }
}

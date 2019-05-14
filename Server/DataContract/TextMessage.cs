using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    public class TextMessage
    {
        public String Sender { get; set; }
        public long TimeSent { get; set; }
        public String Message { get; set; }
    }
}

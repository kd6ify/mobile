using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.Server.WebService
{
    public class DateHelper
    {
        public static DateTime Now
        {
            get
            {
                return DateTime.Now.ToUniversalTime();
            }
        }
    }
}

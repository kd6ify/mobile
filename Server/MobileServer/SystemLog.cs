using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.Server.WebService;

namespace FutureConcepts.Mobile.Server
{
    public class SystemLog
    {
        public static void Log(int level, String proc, String message)
        {
            String msg = String.Format("{0} {1} - {2}", DateTime.Now.ToShortTimeString(), proc, message);
            Debug.WriteLine(msg);
            Diag.WriteLine(msg);
        }
    }
}

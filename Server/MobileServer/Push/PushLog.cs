using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.Server
{
    public class PushLog
    {
        public static readonly int LEVEL_DEBUG = 5;

        public static void Log(int level, String proc, String message)
        {
            Debug.WriteLine(String.Format("{0} {1} - {2}", DateTime.Now.ToShortTimeString(), proc, message));
        }
    }
}

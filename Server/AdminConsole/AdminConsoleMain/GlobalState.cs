using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Configuration;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Threading;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.DataModel;

namespace FutureConcepts.Mobile.AdminConsole
{
    public class GlobalState
    {
        private static GlobalState _instance;

        private static Object _staticLock = new Object();

        public static GlobalState Instance
        {
            get
            {
                lock (_staticLock)
                {
                    if (_instance == null)
                    {
                        _instance = new GlobalState();
                    }
                    return _instance;
                }
            }
        }

        private GlobalState()
        {
        }

        /// <summary>
        /// The last-fetched Server Status
        /// </summary>
        public ServerStatus ServerStatus
        {
            get;
            set;
        }
    }
}

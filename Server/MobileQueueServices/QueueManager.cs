using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.Queue
{
    public class QueueManager
    {
        private static Dictionary<String, MobileQueue> _dict = new Dictionary<String, MobileQueue>();

        private static Object _lock = new Object();

        public static bool ContainsDeviceId(String deviceId)
        {
            bool result = false;
            lock (_lock)
            {
                if (_dict.ContainsKey(deviceId))
                {
                    result = true; ;
                }
            }
            return result;
        }

        public static MobileQueue GetQueue(String deviceId)
        {
            lock (_lock)
            {
                if (_dict.ContainsKey(deviceId))
                {
                    return _dict[deviceId];
                }
                else
                {
                    MobileQueue queue = new MobileQueue(deviceId);
                    _dict.Add(deviceId, queue);
                    return _dict[deviceId];
                }
            }
        }

        private static int _maxQueueSize = 0;

        public static int MaxQueueSize
        {
            get
            {
                if (_maxQueueSize == 0)
                {
                    String maxQueueSizeString = ConfigurationManager.AppSettings["MaxQueueSize"];
                    if (maxQueueSizeString != null)
                    {
                        _maxQueueSize = Convert.ToInt32(maxQueueSizeString);
                    }
                    else
                    {
                        _maxQueueSize = 50;
                    }
                }
                return _maxQueueSize;
            }
        }
    }
}

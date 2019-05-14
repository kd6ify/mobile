using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.Server.Push
{
    public class ResyncRequiredException : Exception
    {
        private String _resyncUrl;

        public ResyncRequiredException(String resyncUrl)
        {
            ResyncUrl = resyncUrl;
        }

        public String ResyncUrl
        {
            get
            {
                return _resyncUrl;
            }
            set
            {
                _resyncUrl = value;
            }
        }
    }
}

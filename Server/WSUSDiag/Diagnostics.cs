using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;

using FutureConcepts.Settings;

using Microsoft.UpdateServices.ClientServicing;
using Microsoft.UpdateServices.Administration;


namespace FutureConcepts.Mobile.Server.WSUSDiag
{
    public class Diagnostics
    {
        private WSUSSettings _wsusSettings;
        private ClientServicingProxy _csp;
        private IUpdateServer _adminProxy;

        public void CheckBasic()
        {
            try
            {
                _wsusSettings = new WSUSSettings();
                Debug.WriteLine(String.Format("WSUS Server: {0}:{1} {2}", _wsusSettings.AdminServiceHost, _wsusSettings.AdminServicePort, _wsusSettings.AdminServiceUseSSL ? "encrypted" : "unencrypted"));
                _adminProxy = AdminProxy.GetUpdateServer(_wsusSettings.AdminServiceHost, _wsusSettings.AdminServiceUseSSL, _wsusSettings.AdminServicePort);
                _csp = new ClientServicingProxy();
            }
            catch (Exception e)
            {
                ErrorLogger.DumpToDebug(e);
            }
        }
    }
}

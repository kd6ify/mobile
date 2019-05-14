using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics;
using System.IdentityModel.Selectors;
using System.Linq;
using System.Security;
using System.Security.Cryptography;
using System.ServiceModel;
using System.Text;

using FutureConcepts.Settings;

using FutureConcepts.Mobile.DataContract;

using FutureConcepts.Data.Access.Web.Security;

namespace FutureConcepts.Mobile.Server.WebService
{
    public class WebUserNamePasswordValidator : UserNamePasswordValidator
    {
        private FutureConcepts.Settings.ConnectionSettings _connectionSettings;
        private String _ldapPath;
        private String _ldapDomain;
        private Boolean _authenticationEnabled = true;

        public WebUserNamePasswordValidator()
        {
            String authenticationEnabledString = ConfigurationManager.AppSettings["AuthenticationEnabled"];
            if (authenticationEnabledString != null)
            {
                _authenticationEnabled = Boolean.Parse(authenticationEnabledString);
            }
            _connectionSettings = new ConnectionSettings();
            _ldapDomain = _connectionSettings.Domain;
            _ldapPath = _connectionSettings.LdapDomainPath;
        }

        private void Logit(String message)
        {
            SystemLog.Log(0, "WebUserNamePasswordValidator.Validate: ", message);
        }

        public override void Validate(String username, String password)
        {
            if (_authenticationEnabled)
            {
                DeviceContext deviceContext = null;
                Logit(String.Format("username='{0}' password='{1}'", username, password));
                try
                {
                    deviceContext = DeviceContextManager.Instance.GetDeviceContext(username);
                }
                catch (Exception e)
                {
                    Logit("DeviceContextManager.GetDeviceContext threw exception: " + e.Message);
                    throw new FaultException("device not registered");
                }
                MembershipToken authenticationContext = deviceContext.AuthenticationContext;
                if ((authenticationContext == null) || (authenticationContext.Token != password))
                {
                    Logit(String.Format("LdapAuthentication.getAuthentication({0}, {1}, {2}, {3})", _ldapPath, _ldapDomain, username, password));
                    MembershipToken result = LdapAuthentication.GetAuthentication(_ldapPath, _ldapDomain, username, password);
                    if (result.AuthenticationResult == MembershipToken.Result.OK)
                    {
                        Logit("MembershipToken.AuthenticationResult=Result.OK");
                        result.Token = Convert.ToBase64String(LdapAuthentication.GetHash(result));
                        deviceContext.AuthenticationContext = result;
                        deviceContext.SendAuthenticationToken = true;
                    }
                    else if (result.AuthenticationResult == MembershipToken.Result.PasswordExpired)
                    {
                        Logit("Password expired");
                        throw new FaultException("password has expired");
                    }
                    else
                    {
                        Logit("Authentication failed");
                        throw new FaultException("authentication failed");
                    }
                }
            }
        }
    }
}

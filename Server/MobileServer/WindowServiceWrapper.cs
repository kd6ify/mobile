using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Net.Mail;
using System.ServiceProcess;
using System.Text;

using FutureConcepts.Settings;
using FutureConcepts.Tools;

namespace FutureConcepts.Mobile.Server
{
    public class WindowServiceWrapper
    {
        private WindowService _service;

        public WindowService MyService
        {
            get { return _service; }
            set { _service = value; }
        }

        public WindowServiceWrapper(WindowService serviceBase, bool debug)
        {
            AppDomain.CurrentDomain.UnhandledException += new UnhandledExceptionEventHandler(AppDomain_UnhandledException);
            this.MyService = serviceBase;
            if (debug)
            {
                MyService.StartDebug();
                Console.ReadLine();
                MyService.EndDebug();
                Debug.WriteLine("Goodbye!");
            }
            else
            {
                ServiceBase.Run(serviceBase);
            }
        }

        static void AppDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e)
        {
            SendUnhandledExceptionToDebug(e);
            SendUnhandledExceptionToEmail(e);
        }

        static void SendUnhandledExceptionToDebug(UnhandledExceptionEventArgs e)
        {
            Debug.WriteLine("Unhandled Exception!");
            Exception exc = e.ExceptionObject as Exception;
            if (exc != null)
            {
                Debug.WriteLine("found CLS-compliant exception");
                ErrorLogger.DumpToDebug(exc);
            }
            else
            {
                string info = String.Format("--- Non-CLS-Compliant exception: Type={0}, String={1}",
                    e.ExceptionObject.GetType(), e.ExceptionObject.ToString());
                Debug.WriteLine(info);
            }
        }

        static void SendUnhandledExceptionToEmail(UnhandledExceptionEventArgs e)
        {

            MobileServerSettings mobileServerSettings = new MobileServerSettings();
            EmailSettings emailSettings = new EmailSettings();
            ConfigSections.MailSection mailConfig = ConfigSections.MailSection.GetSection("unhandledExceptionEmail");
            if (mailConfig != null)
            {
                try
                {
                    MailMessage mailMessage = new MailMessage();
                    ConfigSections.MailEnvelopeElement envelope = mailConfig.Envelope;
                    if (envelope != null)
                    {
                        mailMessage.Subject = envelope.Subject;
                        mailMessage.From = new MailAddress(envelope.From);
                        mailMessage.To.Add(mobileServerSettings.UnhandledExceptionRecipients);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.AppendLine("Caused By:");
                    Exception exception = e.ExceptionObject as Exception;
                    if (exception != null)
                    {
                        DumpExceptionDetails(exception, sb);
                    }
                    else
                    {
                        sb.AppendFormat(String.Format("--- Non-CLS-Compliant exception: Type={0}, String={1}", e.ExceptionObject.GetType(), e.ExceptionObject.ToString()));
                    }
                    mailMessage.Body = sb.ToString();
                    using (SmtpClient smtpClient = new SmtpClient(emailSettings.SMTPServer, emailSettings.SMTPServerPort))
                    {
                        smtpClient.Credentials = new NetworkCredential(emailSettings.SMTPAccount, emailSettings.SMTPPassword);
                        smtpClient.Send(mailMessage);
                    }
                }
                catch (Exception e2)
                {
                    ErrorLogger.DumpToDebug(e2);
                }
            }
        }

        static void DumpExceptionDetails(Exception e, StringBuilder sb)
        {
            sb.AppendFormat(String.Format("Exception.Message: {0}\n", e.Message));
            sb.AppendFormat(String.Format("Exception.StackTrace: {0}\n", e.StackTrace));
            sb.AppendFormat(String.Format("Exception.Source: {0}\n ", e.Source));
            if (e.Data != null)
            {
                foreach (String key in e.Data.Keys)
                {
                    sb.AppendFormat(String.Format("Exception.Data: {0}={1}\n", key, e.Data[key]));
                }
            }
            sb.AppendLine();
            if (e.InnerException != null)
            {
                sb.AppendLine("Inner Exception:");
                DumpExceptionDetails(e.InnerException, sb);
            }
        }
    }
}

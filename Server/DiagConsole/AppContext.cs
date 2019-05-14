using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Diagnostics;
using System.Net;
using System.Net.NetworkInformation;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.MsmqIntegration;
using System.Text;
using System.Timers;
using System.Xml;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;

namespace FutureConcepts.Mobile.DiagConsole
{
    public class AppContext
    {
        private DuplexChannelFactory<IDiag> _factory;
        private IDiag _proxy;
        private IDiagCallback _callback;

        public void Run(String[] args)
        {
            try
            {
                _callback = new MyDiagCallback();
                Console.WriteLine("Attemping to resolve " + args[0]);
                IPHostEntry ipHostEntry = Dns.GetHostEntry(args[0]);
                if (ipHostEntry.AddressList.Length > 0)
                {
                    IPAddress serverAddress = ipHostEntry.AddressList[0];
                    Console.WriteLine("Attempting to Connect to " + serverAddress.ToString());
                    Ping ping = new Ping();
                    PingReply pingReply = ping.Send(serverAddress);
                    Console.WriteLine("Ping reply received from " + pingReply.Address);
                    Console.WriteLine(String.Format("Ping reply time {0} ms", pingReply.RoundtripTime));
                    Console.WriteLine(String.Format("Ping reply status {0}", pingReply.Status.ToString()));
                    DoCommandLoop(serverAddress);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
        }

        private void DoCommandLoop(IPAddress serverAddress)
        {
            String endpointAddress = "net.tcp://" + serverAddress.ToString() + ":8283/Diag";
            try
            {
                _factory = new DuplexChannelFactory<IDiag>(_callback, new NetTcpBinding(SecurityMode.None, true), endpointAddress);
                _factory.Opened += new EventHandler(Factory_Opened);
                _factory.Closed += new EventHandler(Factory_Closed);
                _factory.Faulted += new EventHandler(Factory_Closed);
                _factory.Open();
                _proxy = _factory.CreateChannel();
                _proxy.Open();
                Console.WriteLine("Endpoint address " + endpointAddress + " opened");
                while (true)
                {
                    Console.Write("Diag> ");
                    String command = Console.ReadLine();
                    if (command.Length > 0)
                    {
                        String[] parts = command.Split(' ');
                        switch (parts[0].ToLower())
                        {
                            case "devices":
                                dispatchDevices(parts);
                                break;
                            case "setdevicecontext":
                                dispatchSetDeviceContext(parts);
                                break;
                            case "showdevicecontext":
                                dispatchShowDeviceContext(parts);
                                break;
                            case "sendintent":
                                dispatchSendIntent(parts);
                                break;
                            case "exit":
                                return;
                            case "quit":
                                return;
                            default:
                                Console.WriteLine("invalid command");
                                break;
                        }
                    }
                }
            }
            catch (Exception exc)
            {
                Console.WriteLine("Exception while talking with endpoint " + endpointAddress);
                Console.WriteLine(exc.Message);
            }
        }

        private void Factory_Opened(object sender, EventArgs e)
        {
            Console.WriteLine("fac opened");
        }

        private void Factory_Closed(object sender, EventArgs e)
        {
            Console.WriteLine("connection closed");
            Console.In.Close();
        }

        private void dispatchDevices(String[] parts)
        {
            _proxy.Devices();
        }

        private void dispatchSetDeviceContext(String[] parts)
        {
            _proxy.SetDeviceContext(parts[1]);
        }

        private void dispatchShowDeviceContext(String[] parts)
        {
            _proxy.ShowDeviceContext();
        }

        private void dispatchSendIntent(String[] parts)
        {
            _proxy.SendIntent(parts[1]);
        }
    }
}

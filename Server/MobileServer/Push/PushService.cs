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
using FutureConcepts.Settings;
using FutureConcepts.Tools;

namespace FutureConcepts.Mobile.Server.Push
{
    public class PushService : IDisposable
    {
        private TcpListener _tcpListener;

        private Thread _thread;

        private int _port;

        private static PushService _instance;

        private static Object _staticLock = new Object();

        private static DeviceContextManager _deviceContextManager;

        private Dictionary<String, ClientHandler> _clientHandlers;

        public static PushService Instance
        {
            get
            {
                lock (_staticLock)
                {
                    if (_instance == null)
                    {
                        _instance = new PushService();
                    }
                    return _instance;
                }
            }
        }

        private void AddClientHandler(String deviceId, ClientHandler handler)
        {
            lock (_clientHandlers)
            {
                _clientHandlers.Add(deviceId, handler);
            }
        }

        private void RemoveClientHandler(String deviceId)
        {
            lock (_clientHandlers)
            {
                _clientHandlers.Remove(deviceId);
            }
        }

        private PushService()
        {
            _clientHandlers = new Dictionary<String, ClientHandler>();
            _deviceContextManager = DeviceContextManager.Instance;
            MobileServerSettings settings = new MobileServerSettings();
            Uri pushServiceUri = new Uri(settings.PublicPushServiceAddress);
            _port = pushServiceUri.Port;
            _thread = new Thread(new ThreadStart(ThreadProc));
            _thread.Name = "PushService.TCPListener";
            _thread.Start();
        }

        public void Dispose()
        {
            if (_thread != null)
            {
                _thread.Interrupt();
            }
            if (_tcpListener != null)
            {
                _tcpListener.Stop();
                _tcpListener = null;
            }
        }

        private void Logit(DeviceContext deviceContext, String proc, String message)
        {
            DeviceLog.Log(deviceContext, 0, "Push.PushService." + proc, message);
        }

        private void Logit(String proc, String message)
        {
            PushLog.Log(0, "Push.PushService." + proc, message);
        }

        private void ThreadProc()
        {
            bool running = true;
            try
            {
                IPAddress localAddr = IPAddress.Parse("0.0.0.0");
                _tcpListener = new TcpListener(localAddr, _port);
                _tcpListener.Start();
                while (running)
                {
                    TcpClient client = _tcpListener.AcceptTcpClient();
                    if (client != null)
                    {
                        new ClientHandler(client);
                    }
                }
            }
            catch (Exception e)
            {
                Logit("ThreadProc", e.Message);
                ErrorLogger.DumpToDebug(e);
                running = false;
            }
            finally
            {
                if (_tcpListener != null)
                {
                    // Stop listening for new clients
                    _tcpListener.Stop();
                }
            }
        }

        public void DestroyOlderClientHandler(String deviceId)
        {
            if (_clientHandlers.ContainsKey(deviceId))
            {
                ClientHandler handler = _clientHandlers[deviceId];
                handler.Dispose();
            }
        }

        public void OnConnected(ClientHandler clientHandler)
        {
            String deviceId = clientHandler.DeviceContext.DeviceId;
            Debug.Assert(deviceId != null);
            Logit(clientHandler.DeviceContext, "OnConnected", deviceId);
            AddClientHandler(deviceId, clientHandler);
            DeviceContext deviceContext = clientHandler.DeviceContext;
            deviceContext.PushState = DeviceContext.PUSH_STATE_CONNECTED;
            deviceContext.LastPushStateChangeTime = DateTime.Now;
        }

        public void OnDisconnected(ClientHandler clientHandler)
        {
            if (clientHandler.DeviceContext != null)
            {
                DeviceContext deviceContext = clientHandler.DeviceContext;
                String deviceId = deviceContext.DeviceId;
                if (deviceId != null)
                {
                    Logit(clientHandler.DeviceContext, "OnDisconnected", "");
                    try
                    {
                        RemoveClientHandler(deviceId);
                        clientHandler.DeviceContext.PushState = DeviceContext.PUSH_STATE_DISCONNECTED;
                        clientHandler.DeviceContext.LastPushStateChangeTime = DateTime.Now;
                    }
                    catch (Exception e)
                    {
                        Debug.WriteLine(e.Message);
                        ErrorLogger.DumpToDebug(e);
                    }
                }
            }
        }
    }
}

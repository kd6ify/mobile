using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Messaging;
using System.Net;
using System.Net.Sockets;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Threading;
using System.Web.Script.Serialization;
using System.Xml;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.Queue;
using FutureConcepts.Mobile.Server.WebService;

using FutureConcepts.Tools;

namespace FutureConcepts.Mobile.Server.Push
{
    public class ClientHandler : IDisposable
    {
        private TcpClient _tcpClient;

        private DeviceContext _deviceContext;

        private Thread _readerThread;
        private Queue.MobileQueue _queue;
        private bool _writeNewFormat = false;

        public DeviceContext DeviceContext
        {
            get
            {
                return _deviceContext;
            }
        }

        public ClientHandler(TcpClient tcpClient)
        {
            _tcpClient = tcpClient;
            _readerThread = new Thread(new ThreadStart(ClientReader));
            _readerThread.Start();
        }

        public void Dispose()
        {
            Logit("ClientHandler", "Dispose");
            try
            {
                if (_queue != null)
                {
                    _queue.ReceiveCompleted -= new PeekCompletedEventHandler(Queue_ReceiveCompleted);
                    _queue.CancelReceiveIfNecessary();
                }
                if (_tcpClient != null)
                {
                    if (_tcpClient.Client != null)
                    {
                        _tcpClient.Client.Close();
                    }
                    _tcpClient.Close();
                    _tcpClient = null;
                }
                if (_deviceContext != null)
                {
                    if (PushService.Instance != null)
                    {
                        PushService.Instance.OnDisconnected(this);
                    }
                }
                _deviceContext = null;
            }
            catch (Exception e1)
            {
                ErrorLogger.DumpToDebug(e1);
            }
        }

        private Boolean WriteNewFormat
        {
            get
            {
                return _writeNewFormat;
            }
            set
            {
                _writeNewFormat = value;
                Logit("WriteNewFormat", String.Format("Set to {0}", _writeNewFormat));
            }
        }

        private void Logit(String proc, String message)
        {
            DeviceLog.Log(_deviceContext, 0, "Push.ClientHandler."+proc, message);
        }

        private void Queue_ReceiveCompleted(object sender, PeekCompletedEventArgs e)
        {
            try
            {
                Debug.WriteLine("Queue_ReceiveCompleted");
                WriteQueueMessage(e.Message);
            }
            catch (Exception exc)
            {
                String deviceId = "unknown";
                if (_deviceContext != null)
                {
                    deviceId = _deviceContext.DeviceId;
                }
                Logit("Queue_ReceivedCompleted", exc.Message);
                throw;
            }
        }

        private void HandleMessageAck(String messageId)
        {
            _queue.RemoveMessage(messageId);
            _queue.BeginReceiveIfNecessary();
        }

        private void WriteMessage(String label, String messageId, String body)
        {
            bool hasNext = false;
            WriteString("{");
            if (label != null)
            {
                WriteNameValue(hasNext, "Label", label);
                hasNext = true;
            }
            if (messageId != null)
            {
                WriteNameValue(hasNext, "MessageId", messageId.Replace('\\', '@'));
                hasNext = true;
            }
            if (body != null)
            {
                WriteNameValue(hasNext, "Body", body);
                hasNext = true;
            }
            WriteString("}");
            NetworkStream stream = _tcpClient.GetStream();
            stream.WriteByte(0xff);
            stream.Flush();
        }

        private void WriteNameValue(bool isNext, String name, String value)
        {
            StringBuilder sb = new StringBuilder();
            if (isNext)
            {
                sb.Append(",");
            }
            if (name != null)
            {
                sb.Append(String.Format("\"{0}\" : {1}", name, value));
            }
            byte[] bytes = Encoding.ASCII.GetBytes(sb.ToString());
            _tcpClient.GetStream().Write(bytes, 0, bytes.Length);
        }

        private void WriteString(String value)
        {
            NetworkStream stream = _tcpClient.GetStream();
            byte[] bytes = Encoding.ASCII.GetBytes(value);
            stream.Write(bytes, 0, bytes.Length);
        }

        private void WriteQueueMessage(Message message)
        {
            if (WriteNewFormat)
            {
                WriteMessage("\""+message.Label+"\"", "\""+message.Id+"\"", (String)message.Body);
            }
            else
            {
                WriteString((String)message.Body);
                NetworkStream stream = _tcpClient.GetStream();
                stream.WriteByte(0xff);
                stream.Flush();
                _queue.RemoveMessage(message.Id);
            }
        }

        private void ClientReader()
        {
            const String TAG = "ClientReader";
            bool running = true;
            PushService service = PushService.Instance;
            try
            {
                int i;
                Byte[] bytes = new Byte[256];
                NetworkStream stream = _tcpClient.GetStream();
                while (running)
                {
                    i = stream.Read(bytes, 0, bytes.Length);
                    if (i > 0)
                    {
                        // Commands are: "HELLO", "KEEPALIVE"
                        String command = System.Text.Encoding.ASCII.GetString(bytes, 0, i);
                        String[] commandParts = command.Split(' ');
                        switch (commandParts[0])
                        {
                            case "HELLO":
                                String deviceId = commandParts[1];
                                // Validate device id here
                                try
                                {
                                    _deviceContext = DeviceContextManager.Instance.GetDeviceContext(deviceId);
                                    Logit(TAG, "got HELLO");
                                    service.DestroyOlderClientHandler(deviceId); // just in case
                                    _queue = QueueManager.GetQueue(_deviceContext.DeviceId);
                                    _queue.ReceiveCompleted += new PeekCompletedEventHandler(Queue_ReceiveCompleted);
                                    _queue.BeginReceiveIfNecessary();
                                    service.OnConnected(this);
                                    if (commandParts.Length > 2)
                                    {
                                        String packageName = commandParts[2];
                                        int currentDeviceVersion = Int32.Parse(commandParts[3]);
                                        _deviceContext.CurrentTrinityVersion = currentDeviceVersion;
                                        if (packageName == "com.futureconcepts.mercury" || currentDeviceVersion > 78)
                                        {
                                            WriteNewFormat = true;
                                        }
                                        if (WriteNewFormat)
                                        {
                                            WriteMessage("\"HELLO\"", null, "\"ACK\"");
                                        }
                                        else
                                        {
                                            Intent nackIntent = new Intent("HELLO ACK");
                                        //    WriteFramedMessage(nackIntent.ToJSONString());
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                    WriteMessage("\"HELLO\"", null, "\"NACK\"");
                                    throw;
                                }
                                break;
                            case "KEEPALIVE":
                                Logit(TAG, "got KEEPALIVE");
                                //           if (_tcpClient.GetStream() != null)
                                //         {
                                //           _tcpClient.GetStream().WriteByte(0xff); // send EOM marker (zero-length message)
                                //     }
                                if (_deviceContext.PushState != DeviceContext.PUSH_STATE_CONNECTED)
                                {
                                    Logit(TAG, String.Format("got KEEPALIVE for device {0}, but device not connected, is {1}", _deviceContext.DeviceId, _deviceContext.PushState));
//                                    service.OnConnected(this);
                                }
                                break;
                            case "ACK":
                                String messageId = commandParts[1];
                                Logit(TAG, String.Format("got ACK {0}", messageId));
                                HandleMessageAck(messageId);
                                break;
                            case "CHECKUPDATE":
                                Logit(TAG, "got CHECKUPDATE - is deprecated");
                                break;
                            default:
                                Logit(TAG, "bad command: " + command);
                                break;
                        }
                    }
                    else
                    {
                        Logit(TAG, "read ! > 0 - was " + i);
                        running = false;
                    }
                }
            }
            catch (Exception e)
            {
                Logit(TAG, e.Message);
                ErrorLogger.DumpToDebug(e);
                running = false;
            }
            finally
            {
                Dispose();
            }
        }
    }
}

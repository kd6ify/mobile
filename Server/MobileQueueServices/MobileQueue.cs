using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics;
using System.Linq;
using System.Messaging;
using System.Text;

using FutureConcepts.Tools;

namespace FutureConcepts.Mobile.Queue
{
    public class MobileQueue
    {
        private MessageQueue _msgQ;

        private String _queueName;

        private int _messageCount = 0;

        private bool _showSendToQueue = false;

        private DateTime _startTime;

        private IAsyncResult _asyncResult;

        public int MessageCount
        {
            get
            {
                return _messageCount;
            }
            set
            {
                _messageCount = value;
            }
        }

        public event PeekCompletedEventHandler ReceiveCompleted;

        public MobileQueue(String deviceId)
        {
            _queueName = @".\private$\" + deviceId;
            if (MessageQueue.Exists(_queueName) == false)
            {
                _msgQ = MessageQueue.Create(_queueName, true);
                Logit("Queue", "Successfully created MSMQ Queue " + _queueName);
                _messageCount = 0;
            }
            else
            {
                _msgQ = new MessageQueue(_queueName);
                try
                {
                    Message[] messages = _msgQ.GetAllMessages();
                    _messageCount = messages.Length;
                }
                catch (Exception e)
                {
                    Logit("Queue", e.Message);
                    ErrorLogger.DumpToDebug(e);
                    _messageCount = 0;
                }
            }
            _msgQ.Formatter = new BinaryMessageFormatter();
            _msgQ.PeekCompleted += new PeekCompletedEventHandler(MsqQ_ReceiveCompleted);
            _startTime = DateTime.Now;
        }

        private void Logit(String proc, String message)
        {
            Logger.Log(0, "Push.Queue." + proc, message);
        }

        public void BeginReceiveIfNecessary()
        {
            Logger.Log(0, "BeginReceiveIfNecessary", "found " + MessageCount + " messages in the queue.");
            if (_asyncResult == null)
            {
                _asyncResult = _msgQ.BeginPeek();
            }
        }

        public void CancelReceiveIfNecessary()
        {
            if (_asyncResult != null)
            {
//                _msgQ.EndPeek(_asyncResult);
                _asyncResult = null;
            }
        }

        public void Send(String intentAction, String body)
        {
            if (_messageCount > QueueManager.MaxQueueSize) // queue full
            {
                Logit("Send", String.Format("Device queue {0} is full--clearing", _msgQ.QueueName));
                TimeSpan totalTime = _startTime - DateTime.Now;
                _startTime = DateTime.Now;
                //                    Debug.WriteLine(String.Format("inbound queue rate: {0} items/sec", _messageCount / totalTime.Seconds));
                _msgQ.Purge();
                _messageCount = 0;
            }
            Message myMessage = new Message(body);
            myMessage.Label = intentAction;
            myMessage.Formatter = new BinaryMessageFormatter();
            _msgQ.Send(myMessage, MessageQueueTransactionType.Single);
            _messageCount++;
            if (_showSendToQueue)
            {
                Logit("Send", myMessage.Id);
            }
        }

        public void RemoveMessage(String messageId)
        {
            try
            {
                Message message = _msgQ.ReceiveById(messageId.Replace("@", "\\"));
                _messageCount--;
            }
            catch (Exception e)
            {
                Logit("RemoveMessage", String.Format("MessageQueue.ReceiveById({0}) exception: {1}", messageId, e.Message));
            }
        }

        public void Purge()
        {
            _msgQ.Purge();
        }

        private void MsqQ_ReceiveCompleted(object sender, PeekCompletedEventArgs e)
        {
            Logit("MsqQ_ReceiveCompleted", "fired");
            if (ReceiveCompleted != null)
            {
                try
                {
                    Message message = e.Message;
                    // Discard all messages in queue without a label
                    if (message.Label == "")
                    {
                        Message discardedMessage = _msgQ.ReceiveById(message.Id);
                        _asyncResult = _msgQ.BeginPeek();
                    }
                    else
                    {
                        ReceiveCompleted(this, e);
                        _asyncResult = null;
                    }
                }
                catch (Exception exc)
                {
                    Logit("MsgQ_ReceiveCompleted", exc.Message);
                    ErrorLogger.DumpToDebug(exc);
                }
            }
            else
            {
                _asyncResult = null;
            }
        }

        public Message[] PeekAll()
        {
            if (_msgQ != null)
            {
                return _msgQ.GetAllMessages();
            }
            else
            {
                return null;
            }
        }
    }
}

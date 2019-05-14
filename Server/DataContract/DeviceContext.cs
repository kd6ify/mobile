using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

using FutureConcepts.Data.Access.Web.Security;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class DeviceContext : INotifyPropertyChanged
    {
        public static readonly String PUSH_STATE_CONNECTED = "Connected";
        public static readonly String PUSH_STATE_DISCONNECTED = "Disconnected";

        public event PropertyChangedEventHandler PropertyChanged;

        private String _deviceId;
        private MembershipToken _authenticationContext;
        private bool _sendAuthenticationToken;

        private void NotifyPropertyChanged(String info)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(info));
            }
        }

        public DeviceContext()
        {
        }

        public DeviceContext(String deviceId, String pushState)
        {
            _deviceId = deviceId;
            PushState = pushState;
            TrackerState = new TrackerState();
            LastPushStateChangeTime = DateTime.Now;
        }

        private String _agencyName;

        [DataMember]
        public String AgencyName
        {
            get
            {
                return _agencyName;
            }
            set
            {
                if (value != _agencyName)
                {
                    _agencyName = value;
                    NotifyPropertyChanged("AgencyName");
                }
            }
        }

        public String DeviceId
        {
            get
            {
                return _deviceId;
            }
        }

        private String _pushState;

        [DataMember]
        public String PushState
        {
            get
            {
                return _pushState;
            }
            set
            {
                if (value != _pushState)
                {
                    _pushState = value;
                    NotifyPropertyChanged("PushState");
                }
            }
        }

        private DateTime _lastPushStateChangeTime;

        [DataMember(EmitDefaultValue=false)]
        public DateTime LastPushStateChangeTime
        {
            get
            {
                return _lastPushStateChangeTime;
            }
            set
            {
                if (value != _lastPushStateChangeTime)
                {
                    _lastPushStateChangeTime = value;
                    NotifyPropertyChanged("LastPushStateChangeTime");
                }
            }
        }

        private int _currentTrinityVersion;

        [DataMember]
        public int CurrentTrinityVersion
        {
            get
            {
                return _currentTrinityVersion;
            }
            set
            {
                if (value != _currentTrinityVersion)
                {
                    _currentTrinityVersion = value;
                    NotifyPropertyChanged("CurrentTrinityVersion");
                }
            }
        }

        private TrackerState _trackerState;

        [DataMember]
        public TrackerState TrackerState
        {
            get
            {
                return _trackerState;
            }
            set
            {
                if (value != _trackerState)
                {
                    _trackerState = value;
                    NotifyPropertyChanged("TrackerState");
                }
            }
        }

        private Location _lastKnownLocation;

        [DataMember]
        public Location LastKnownLocation
        {
            get
            {
                return _lastKnownLocation;
            }
            set
            {
                if (value != _lastKnownLocation)
                {
                    _lastKnownLocation = value;
                    NotifyPropertyChanged("LastKnownLocation");
                }
            }
        }

        private int _lastSentUpdateNotificationVersion;

        [DataMember]
        public int LastSentUpdateNotificationVersion
        {
            get
            {
                return _lastSentUpdateNotificationVersion;
            }
            set
            {
                if (value != _lastSentUpdateNotificationVersion)
                {
                    _lastSentUpdateNotificationVersion = value;
                    NotifyPropertyChanged("LastSentUpdateNotificationVersion");
                }
            }
        }

        private Nullable<Guid> _assetID;

        [DataMember]
        public Nullable<Guid> AssetID
        {
            get
            {
                return _assetID;
            }
            set
            {
                if (value != _assetID)
                {
                    _assetID = value;
                    NotifyPropertyChanged("AssetID");
                }
            }
        }

        private Nullable<Guid> _equipmentID;

        [DataMember]
        public Nullable<Guid> EquipmentID
        {
            get
            {
                return _equipmentID;
            }
            set
            {
                if (value != _equipmentID)
                {
                    _equipmentID = value;
                    NotifyPropertyChanged("EquipmentID");
                }
            }
        }

        private String _equipmentName;

        [DataMember]
        public String EquipmentName
        {
            get
            {
                return _equipmentName;
            }
            set
            {
                if (value != _equipmentName)
                {
                    _equipmentName = value;
                    NotifyPropertyChanged("EquipmentName");
                }
            }
        }

        [DataMember]
        public MembershipToken AuthenticationContext
        {
            get
            {
                return _authenticationContext;
            }
            set
            {
                if (value != _authenticationContext)
                {
                    _authenticationContext = value;
                    NotifyPropertyChanged("AuthenticationContext");
                }
            }
        }

        [DataMember]
        public bool SendAuthenticationToken
        {
            get
            {
                return _sendAuthenticationToken;
            }
            set
            {
                if (value != _sendAuthenticationToken)
                {
                    _sendAuthenticationToken = value;
                    NotifyPropertyChanged("SendAuthenticationToken");
                }
            }
        }
    }
}

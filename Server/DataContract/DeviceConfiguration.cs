using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Data.Linq.Mapping;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

using FutureConcepts.Settings;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
	public partial class DeviceConfiguration : INotifyPropertyChanged
	{		
		private String _deviceId;
        private String _deviceName;
        private String _phoneNumber;
        private Nullable<Guid> _equipmentId;
        private MobileServerSettings _mobileServerSettings;
        private String _wsusBridgeServiceAddress;
        private String _mediaImagesServerAddress;
        private String _mediaImagesServerUser;
        private String _mediaImagesServerPassword;
        private String _vsmServerAddress;

		public DeviceConfiguration(DeviceContext deviceContext)
		{
            DeviceId = deviceContext.DeviceId;
            if (deviceContext.EquipmentName != null)
            {
                DeviceName = deviceContext.EquipmentName;
            }
            _equipmentId = deviceContext.EquipmentID;
            _mobileServerSettings = new MobileServerSettings();
            WSUSSettings wsusSettings = new WSUSSettings();
            _wsusBridgeServiceAddress = wsusSettings.WSUSBridgeService;
            _mediaImagesServerAddress = _mobileServerSettings.MediaImagesServerService;
            _mediaImagesServerUser = _mobileServerSettings.MediaImagesServerUserName;
            _mediaImagesServerPassword = _mobileServerSettings.MediaImagesServerPassword;
            _vsmServerAddress = _mobileServerSettings.VsmServerAddress;
		}

        [DataMember(EmitDefaultValue = false)]
        public string DeviceId
        {
            get
            {
                return _deviceId;
            }
            set
            {
                if (_deviceId != value)
                {
                    _deviceId = value;
                    SendPropertyChanged("DeviceId");
                }
            }
        }

        [DataMember(EmitDefaultValue = false)]
        public string DeviceName
        {
            get
            {
                return _deviceName;
            }
            set
            {
                if (_deviceName != value)
                {
                    _deviceName = value;
                    SendPropertyChanged("DeviceName");
                }
            }
        }

        [DataMember(EmitDefaultValue = false)]
        public string PhoneNumber
        {
            get
            {
                return _phoneNumber;
            }
            set
            {
                if (_phoneNumber != value)
                {
                    _phoneNumber = value;
                    SendPropertyChanged("PhoneNumber");
                }
            }
        }

        [DataMember]
        public Nullable<Guid> EquipmentId
        {
            get
            {
                return _equipmentId;
            }
            set
            {
                if ((_equipmentId != value))
                {
                    _equipmentId = value;
                    SendPropertyChanged("EquipmentId");
                }
            }
        }

        [DataMember]
        public String WSUSBridgeServiceAddress
        {
            get
            {
                return _wsusBridgeServiceAddress;
            }
            set
            {
                if ((_wsusBridgeServiceAddress != value))
                {
                    _wsusBridgeServiceAddress = value;
                    SendPropertyChanged("WSUSBridgeServiceAddress");
                }
            }
        }

        [DataMember]
        public String MediaImagesServerAddress
        {
            get
            {
                return _mediaImagesServerAddress;
            }
            set
            {
                if ((_mediaImagesServerAddress != value))
                {
                    _mediaImagesServerAddress = value;
                    SendPropertyChanged("MediaImagesServerAddress");
                }
            }
        }

        [DataMember]
        public String MediaImagesServerUser
        {
            get
            {
                return _mediaImagesServerUser;
            }
            set
            {
                if ((_mediaImagesServerUser != value))
                {
                    _mediaImagesServerUser = value;
                    SendPropertyChanged("MediaImagesServerUser");
                }
            }
        }

        [DataMember]
        public String MediaImagesServerPassword
        {
            get
            {
                return _mediaImagesServerPassword;
            }
            set
            {
                if ((_mediaImagesServerPassword != value))
                {
                    _mediaImagesServerPassword = value;
                    SendPropertyChanged("MediaImagesServerPassword");
                }
            }
        }

        [DataMember]
        public String VsmServerAddress
        {
            get
            {
                return _vsmServerAddress;
            }
            set
            {
                if ((_vsmServerAddress != value))
                {
                    _vsmServerAddress = value;
                    SendPropertyChanged("VsmServerAddress");
                }
            }
        }


		public event PropertyChangedEventHandler PropertyChanged;
				
		protected virtual void SendPropertyChanged(String propertyName)
		{
			if ((PropertyChanged != null))
			{
				PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
			}
		}
	}
}

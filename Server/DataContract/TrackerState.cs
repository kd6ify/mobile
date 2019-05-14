using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class TrackerState : INotifyPropertyChanged
    {
        public const String STATE_RUNNING = "Running";
        public const String STATE_STOPPED = "Stopped";
        public const String STATE_UNKNOWN = "Unknown";
        public const String MODE_UNKNOWN = "Unknown";

        public event PropertyChangedEventHandler PropertyChanged;

        private void NotifyPropertyChanged(String info)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(info));
            }
        }

        public TrackerState()
        {
            State = STATE_UNKNOWN;
            Mode = MODE_UNKNOWN;
        }

        public TrackerState(String state, String mode)
        {
            State = state;
            Mode = mode;
        }

        private String _state;

        [DataMember]
        public String State
        {
            get
            {
                return _state;
            }
            set
            {
                if (value != _state)
                {
                    _state = value;
                    NotifyPropertyChanged("State");
                }
            }
        }

        private String _mode;

        [DataMember]
        public String Mode
        {
            get
            {
                return _mode;
            }
            set
            {
                if (value != _mode)
                {
                    _mode = value;
                    NotifyPropertyChanged("Mode");
                }
            }
        }

        private Boolean _batteryLow;

        [DataMember]
        public Boolean BatteryLow
        {
            get
            {
                return _batteryLow;
            }
            set
            {
                if (value != _batteryLow)
                {
                    _batteryLow = value;
                    NotifyPropertyChanged("BatteryLow");
                }
            }
        }

        public override String ToString()
        {
            String result = null;
            if (State != STATE_STOPPED)
            {
                result = State + " / " + Mode;
            }
            else
            {
                result = State;
            }
            if (_batteryLow)
            {
                result += " / BatteryLow";
            }
            else
            {
                result += " / BatteryOkay";
            }
            return result;
        }
    }
}

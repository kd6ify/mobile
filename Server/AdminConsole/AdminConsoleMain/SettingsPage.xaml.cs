using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.DataModel;

namespace FutureConcepts.Mobile.AdminConsole
{
    /// <summary>
    /// Interaction logic for mainpage.xaml
    /// </summary>
    public partial class SettingsPage : Page
    {
        private Properties.Settings _settings;
        private bool _settingsDirty = false;

        public SettingsPage()
        {
            InitializeComponent();
            _settings = Properties.Settings.Default;
            _settings.PropertyChanged += new System.ComponentModel.PropertyChangedEventHandler(settings_PropertyChanged);
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            serverUrl.Text = _settings.ServerUrl;
            serverUrl.Focus();
        }

        private void serverUrl_TextChanged(object sender, RoutedEventArgs e)
        {
            _settings.ServerUrl = serverUrl.Text;
        }

        private void saveChangesButton_Click(object sender, RoutedEventArgs e)
        {
            if (_settingsDirty)
            {
                _settings.Save();
                _settingsDirty = false;
            }
            NavigationService.GoBack();
        }

        private void settings_PropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            _settingsDirty = true;
        }
    }
}

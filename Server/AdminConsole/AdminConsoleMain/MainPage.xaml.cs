using System;
using System.Collections.Generic;
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
    public partial class MainPage : Page
    {
        public MainPage()
        {
            InitializeComponent();
            this.Loaded += new RoutedEventHandler(MainPage_Loaded);
        }

        private void treeView1_SelectedItemChanged(object sender, RoutedPropertyChangedEventArgs<object> e)
        {
            ViewTrackerPage viewTrackerPage = new ViewTrackerPage(e.NewValue as DeviceContext);
            NavigationService.Navigate(viewTrackerPage);
        }

        private void refreshButton_Click(object sender, RoutedEventArgs e)
        {
            RefreshDataContext();
        }

        private void settingsButton_Click(object sender, RoutedEventArgs e)
        {
            SettingsPage settingsPage = new SettingsPage();
            NavigationService.Navigate(settingsPage);
        }

        private void addDeviceButton_Click(object sender, RoutedEventArgs e)
        {
            AddDevicePage addDevicePage = new AddDevicePage();
            addDevicePage.DataContext = new AddDevicePageViewModel() { Device = new MobileDevice() };
            NavigationService.Navigate(addDevicePage);
        }

        private void addDebugDeviceButton_Click(object sender, RoutedEventArgs e)
        {
            DebugDeviceCreator debugDeviceCreator = new DebugDeviceCreator();
            debugDeviceCreator.Execute();
        }

        private void MainPage_Loaded(object sender, RoutedEventArgs e)
        {
            RefreshDataContext();
        }

        private void RefreshDataContext()
        {
            serverUrl.Text = Properties.Settings.Default.ServerUrl;
            ThreadPool.QueueUserWorkItem(new WaitCallback(GetStatus), null);
        }

        private void GetStatus(object data)
        {
            try
            {
                ServerStatus serverStatus = ServerProxy.Instance.GetStatus();
                if (serverStatus != null)
                {
                    GlobalState.Instance.ServerStatus = serverStatus;
                    Dispatcher.BeginInvoke((Action)delegate()
                    {
                        this.DataContext = serverStatus.AgencyDeviceContexts;
                    }, null);
                }
            }
            catch (Exception e)
            {
                Dispatcher.BeginInvoke((Action)delegate()
                {
                    MessageBox.Show(e.Message, "Exception", MessageBoxButton.OK, MessageBoxImage.Exclamation);
                }, null);
            }
        }

        private void CommandBinding_CanExecute(object sender, CanExecuteRoutedEventArgs e)
        {
            if ((e.Command == AdminCommands.ChangeTrackerState) ||
                (e.Command == AdminCommands.ViewTracker) ||
                (e.Command == AdminCommands.EditTracker))
            {
                e.CanExecute = true;
            }
        }

        private void CommandBinding_Executed(object sender, ExecutedRoutedEventArgs e)
        {
            if (e.Command == AdminCommands.ChangeTrackerState)
            {
                NavigationService.Navigate(new ChangeTrackerStatePage((DeviceContext)e.Parameter));
            }
            else if (e.Command == AdminCommands.ViewTracker)
            {
                NavigationService.Navigate(new ViewTrackerPage((DeviceContext)e.Parameter));
            }
            else if (e.Command == AdminCommands.EditTracker)
            {
                AddDevicePageViewModel vm = new AddDevicePageViewModel(((DeviceContext)e.Parameter).MobileDevice);
                NavigationService.Navigate(new AddDevicePage(vm));
            }
        }
    }
}

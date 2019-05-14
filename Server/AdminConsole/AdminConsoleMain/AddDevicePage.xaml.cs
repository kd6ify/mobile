using System;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Navigation;
using FutureConcepts.Mobile.DataModel;

namespace FutureConcepts.Mobile.AdminConsole
{
    /// <summary>
    /// Interaction logic for mainpage.xaml
    /// </summary>
    public partial class AddDevicePage : Page
    {
        public AddDevicePage(AddDevicePageViewModel viewModel)
        {
            InitializeComponent();
            this.DataContext = viewModel;
        }

        public AddDevicePage()
        {
            InitializeComponent();
        }

        public ObservableCollection<Equipment> ExistingEquipment { get; set; }


        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            try
            {
                AddDevicePageViewModel m = this.DataContext as AddDevicePageViewModel;
                if (m == null) return;

                m.Initialize();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString(), "Error Hitting up ICDB", MessageBoxButton.OK);
            }
        }

        private void CommandBinding_CanExecute(object sender, CanExecuteRoutedEventArgs e)
        {
            AddDevicePageViewModel m = this.DataContext as AddDevicePageViewModel;
            if (m == null) return;

            e.CanExecute = m.CanSaveChanges();
        }

        private void CommandBinding_Executed(object sender, ExecutedRoutedEventArgs e)
        {
            AddDevicePageViewModel m = this.DataContext as AddDevicePageViewModel;
            if (m == null) return;

            m.ExecuteSaveChanges();

            NavigationService.GoBack();
            NavigationService.RemoveBackEntry();
        }

        private void DeviceId_TextChanged(object sender, TextChangedEventArgs e)
        {
            AddDevicePageViewModel m = this.DataContext as AddDevicePageViewModel;
            if (m == null) return;

            m.OnDeviceIdChanged(((TextBox)e.OriginalSource).Text);
        }
    }
}

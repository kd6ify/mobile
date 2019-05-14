using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Input;

namespace FutureConcepts.Mobile.AdminConsole
{
    public static class AdminCommands
    {
        private static RoutedUICommand _saveChanges = new RoutedUICommand("Save Changes", "SaveChanges", typeof(AdminCommands));
        public static RoutedUICommand SaveChanges
        {
            get
            {
                return _saveChanges;
            }
        }

        private static RoutedUICommand _changeTracker = new RoutedUICommand("Change Tracker State", "ChangeTracker", typeof(AdminCommands));
        public static RoutedUICommand ChangeTrackerState
        {
            get
            {
                return _changeTracker;
            }
        }

        private static RoutedUICommand _viewTracker = new RoutedUICommand("View Tracker State", "ViewTracker", typeof(AdminCommands));
        public static RoutedUICommand ViewTracker
        {
            get
            {
                return _viewTracker;
            }
        }

        private static RoutedUICommand _editTracker = new RoutedUICommand("Edit Tracker", "EditTracker", typeof(AdminCommands));
        public static RoutedUICommand EditTracker
        {
            get
            {
                return _editTracker;
            }
        }
    }
}

using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class TransactionContextV3
    {
        public TransactionContextV3()
        {
            _items = new List<TransactionItem>();
        }

        private List<TransactionItem> _items;

        [DataMember(Order = 1, EmitDefaultValue = false)]
        public String ErrorMessage { get; set; }

        [DataMember(Order = 2)]
        public int ItemCount { get; set; }

        [DataMember(Order = 3, EmitDefaultValue = false)]
        public List<TransactionItem> Items
        {
            get
            {
                return _items;
            }
            set
            {
                _items = value;
            }
        }

        public void PostContentAction(String action, Object content)
        {
            if (content != null)
            {
                TransactionItem item = new TransactionItem();
                item.Action = action;
                item.Content = content;
                _items.Add(item);
                if (content != null)
                {
                    Type contentType = content.GetType();
                    MethodInfo getCountMethod = contentType.GetMethod("get_Count");
                    if (getCountMethod != null)
                    {
                        ItemCount += Convert.ToInt32(getCountMethod.Invoke(content, null));
                    }
                    else
                    {
                        ItemCount += 1;
                    }
                }
            }
        }
    }
}

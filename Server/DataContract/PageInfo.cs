using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    public class PageInfo
    {
        public PageInfo()
        {
        }

        public PageInfo(int pageNumber, int pageSize)
        {
            PageNumber = pageNumber;
            PageSize = pageSize;
        }

        public int PageSize { get; set; }

        public int TotalPages { get; set; }

        public int PageNumber { get; set; }
    }
}

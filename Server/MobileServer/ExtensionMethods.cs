using System.Linq;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.Server
{
    public static class ExtensionMethods
    {
        public static IQueryable<T> Page<T>(this IQueryable<T> query, PageInfo pageInfo)
        {
            pageInfo.TotalPages = (query.Count() + (pageInfo.PageSize - 1)) / pageInfo.PageSize;
            return query.Skip(pageInfo.PageSize * (pageInfo.PageNumber - 1)).Take(pageInfo.PageSize);
        }
    }
}

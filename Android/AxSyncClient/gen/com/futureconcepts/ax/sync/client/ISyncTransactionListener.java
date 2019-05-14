/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Projects20\\Mobile\\Android\\AxSyncClient\\src\\com\\futureconcepts\\ax\\sync\\client\\ISyncTransactionListener.aidl
 */
package com.futureconcepts.ax.sync.client;
public interface ISyncTransactionListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.futureconcepts.ax.sync.client.ISyncTransactionListener
{
private static final java.lang.String DESCRIPTOR = "com.futureconcepts.ax.sync.client.ISyncTransactionListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.futureconcepts.ax.sync.client.ISyncTransactionListener interface,
 * generating a proxy if needed.
 */
public static com.futureconcepts.ax.sync.client.ISyncTransactionListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.futureconcepts.ax.sync.client.ISyncTransactionListener))) {
return ((com.futureconcepts.ax.sync.client.ISyncTransactionListener)iin);
}
return new com.futureconcepts.ax.sync.client.ISyncTransactionListener.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onActionChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onActionChanged(_arg0);
return true;
}
case TRANSACTION_onDatasetChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onDatasetChanged(_arg0);
return true;
}
case TRANSACTION_onTableChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onTableChanged(_arg0);
return true;
}
case TRANSACTION_onStatusChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onStatusChanged(_arg0);
return true;
}
case TRANSACTION_onServerFetch:
{
data.enforceInterface(DESCRIPTOR);
this.onServerFetch();
return true;
}
case TRANSACTION_onServerFetchDone:
{
data.enforceInterface(DESCRIPTOR);
this.onServerFetchDone();
return true;
}
case TRANSACTION_onProgress:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.onProgress(_arg0, _arg1);
return true;
}
case TRANSACTION_onError:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.SyncError _arg0;
if ((0!=data.readInt())) {
_arg0 = com.futureconcepts.ax.sync.client.SyncError.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onError(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.futureconcepts.ax.sync.client.ISyncTransactionListener
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onActionChanged(java.lang.String action) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(action);
mRemote.transact(Stub.TRANSACTION_onActionChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onDatasetChanged(java.lang.String dataset) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dataset);
mRemote.transact(Stub.TRANSACTION_onDatasetChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onTableChanged(java.lang.String table) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(table);
mRemote.transact(Stub.TRANSACTION_onTableChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onStatusChanged(java.lang.String status) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(status);
mRemote.transact(Stub.TRANSACTION_onStatusChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onServerFetch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onServerFetch, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onServerFetchDone() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onServerFetchDone, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onProgress(int position, int count) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(position);
_data.writeInt(count);
mRemote.transact(Stub.TRANSACTION_onProgress, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onError(com.futureconcepts.ax.sync.client.SyncError error) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((error!=null)) {
_data.writeInt(1);
error.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onError, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onActionChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDatasetChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onTableChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onStatusChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onServerFetch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_onServerFetchDone = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_onProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_onError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
}
public void onActionChanged(java.lang.String action) throws android.os.RemoteException;
public void onDatasetChanged(java.lang.String dataset) throws android.os.RemoteException;
public void onTableChanged(java.lang.String table) throws android.os.RemoteException;
public void onStatusChanged(java.lang.String status) throws android.os.RemoteException;
public void onServerFetch() throws android.os.RemoteException;
public void onServerFetchDone() throws android.os.RemoteException;
public void onProgress(int position, int count) throws android.os.RemoteException;
public void onError(com.futureconcepts.ax.sync.client.SyncError error) throws android.os.RemoteException;
}

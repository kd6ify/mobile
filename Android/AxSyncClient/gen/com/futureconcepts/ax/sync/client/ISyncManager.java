/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Projects20\\Mobile\\Android\\AxSyncClient\\src\\com\\futureconcepts\\ax\\sync\\client\\ISyncManager.aidl
 */
package com.futureconcepts.ax.sync.client;
public interface ISyncManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.futureconcepts.ax.sync.client.ISyncManager
{
private static final java.lang.String DESCRIPTOR = "com.futureconcepts.ax.sync.client.ISyncManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.futureconcepts.ax.sync.client.ISyncManager interface,
 * generating a proxy if needed.
 */
public static com.futureconcepts.ax.sync.client.ISyncManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.futureconcepts.ax.sync.client.ISyncManager))) {
return ((com.futureconcepts.ax.sync.client.ISyncManager)iin);
}
return new com.futureconcepts.ax.sync.client.ISyncManager.Stub.Proxy(obj);
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
case TRANSACTION_registerSyncListener:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.ISyncListener _arg0;
_arg0 = com.futureconcepts.ax.sync.client.ISyncListener.Stub.asInterface(data.readStrongBinder());
this.registerSyncListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterSyncListener:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.ISyncListener _arg0;
_arg0 = com.futureconcepts.ax.sync.client.ISyncListener.Stub.asInterface(data.readStrongBinder());
this.unregisterSyncListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getCurrentTransaction:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.ISyncTransaction _result = this.getCurrentTransaction();
reply.writeNoException();
reply.writeStrongBinder((((_result!=null))?(_result.asBinder()):(null)));
return true;
}
case TRANSACTION_setCurrentIncidentID:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setCurrentIncidentID(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getCurrentIncidentID:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCurrentIncidentID();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_startSyncing:
{
data.enforceInterface(DESCRIPTOR);
this.startSyncing();
reply.writeNoException();
return true;
}
case TRANSACTION_syncDataset:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.syncDataset(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_deleteDataset:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.deleteDataset(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_dropDataset:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.dropDataset(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_uploadInsert:
{
data.enforceInterface(DESCRIPTOR);
android.net.Uri _arg0;
if ((0!=data.readInt())) {
_arg0 = android.net.Uri.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.uploadInsert(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.futureconcepts.ax.sync.client.ISyncManager
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
@Override public void registerSyncListener(com.futureconcepts.ax.sync.client.ISyncListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerSyncListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterSyncListener(com.futureconcepts.ax.sync.client.ISyncListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterSyncListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public com.futureconcepts.ax.sync.client.ISyncTransaction getCurrentTransaction() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.futureconcepts.ax.sync.client.ISyncTransaction _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentTransaction, _data, _reply, 0);
_reply.readException();
_result = com.futureconcepts.ax.sync.client.ISyncTransaction.Stub.asInterface(_reply.readStrongBinder());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setCurrentIncidentID(java.lang.String incidentID) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(incidentID);
mRemote.transact(Stub.TRANSACTION_setCurrentIncidentID, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getCurrentIncidentID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentIncidentID, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void startSyncing() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startSyncing, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void syncDataset(java.lang.String datasetName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(datasetName);
mRemote.transact(Stub.TRANSACTION_syncDataset, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void deleteDataset(java.lang.String datasetName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(datasetName);
mRemote.transact(Stub.TRANSACTION_deleteDataset, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void dropDataset(java.lang.String datasetName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(datasetName);
mRemote.transact(Stub.TRANSACTION_dropDataset, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void uploadInsert(android.net.Uri uri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((uri!=null)) {
_data.writeInt(1);
uri.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_uploadInsert, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_registerSyncListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregisterSyncListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getCurrentTransaction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setCurrentIncidentID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getCurrentIncidentID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_startSyncing = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_syncDataset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_deleteDataset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_dropDataset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_uploadInsert = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
}
public void registerSyncListener(com.futureconcepts.ax.sync.client.ISyncListener listener) throws android.os.RemoteException;
public void unregisterSyncListener(com.futureconcepts.ax.sync.client.ISyncListener listener) throws android.os.RemoteException;
public com.futureconcepts.ax.sync.client.ISyncTransaction getCurrentTransaction() throws android.os.RemoteException;
public void setCurrentIncidentID(java.lang.String incidentID) throws android.os.RemoteException;
public java.lang.String getCurrentIncidentID() throws android.os.RemoteException;
public void startSyncing() throws android.os.RemoteException;
public void syncDataset(java.lang.String datasetName) throws android.os.RemoteException;
public void deleteDataset(java.lang.String datasetName) throws android.os.RemoteException;
public void dropDataset(java.lang.String datasetName) throws android.os.RemoteException;
public void uploadInsert(android.net.Uri uri) throws android.os.RemoteException;
}

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Projects20\\Mobile\\Android\\AxSyncClient\\src\\com\\futureconcepts\\ax\\sync\\client\\ISyncTransaction.aidl
 */
package com.futureconcepts.ax.sync.client;
public interface ISyncTransaction extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.futureconcepts.ax.sync.client.ISyncTransaction
{
private static final java.lang.String DESCRIPTOR = "com.futureconcepts.ax.sync.client.ISyncTransaction";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.futureconcepts.ax.sync.client.ISyncTransaction interface,
 * generating a proxy if needed.
 */
public static com.futureconcepts.ax.sync.client.ISyncTransaction asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.futureconcepts.ax.sync.client.ISyncTransaction))) {
return ((com.futureconcepts.ax.sync.client.ISyncTransaction)iin);
}
return new com.futureconcepts.ax.sync.client.ISyncTransaction.Stub.Proxy(obj);
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
case TRANSACTION_registerListener:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.ISyncTransactionListener _arg0;
_arg0 = com.futureconcepts.ax.sync.client.ISyncTransactionListener.Stub.asInterface(data.readStrongBinder());
this.registerListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterListener:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.ISyncTransactionListener _arg0;
_arg0 = com.futureconcepts.ax.sync.client.ISyncTransactionListener.Stub.asInterface(data.readStrongBinder());
this.unregisterListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_abort:
{
data.enforceInterface(DESCRIPTOR);
this.abort();
reply.writeNoException();
return true;
}
case TRANSACTION_getAction:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getAction();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getDataset:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getDataset();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getTable:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getTable();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getError:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.SyncError _result = this.getError();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.futureconcepts.ax.sync.client.ISyncTransaction
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
@Override public void registerListener(com.futureconcepts.ax.sync.client.ISyncTransactionListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterListener(com.futureconcepts.ax.sync.client.ISyncTransactionListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void abort() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_abort, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getAction() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAction, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getDataset() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDataset, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getTable() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getTable, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public com.futureconcepts.ax.sync.client.SyncError getError() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.futureconcepts.ax.sync.client.SyncError _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getError, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.futureconcepts.ax.sync.client.SyncError.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_registerListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregisterListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_abort = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getDataset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getTable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
public void registerListener(com.futureconcepts.ax.sync.client.ISyncTransactionListener listener) throws android.os.RemoteException;
public void unregisterListener(com.futureconcepts.ax.sync.client.ISyncTransactionListener listener) throws android.os.RemoteException;
public void abort() throws android.os.RemoteException;
public java.lang.String getAction() throws android.os.RemoteException;
public java.lang.String getDataset() throws android.os.RemoteException;
public java.lang.String getTable() throws android.os.RemoteException;
public com.futureconcepts.ax.sync.client.SyncError getError() throws android.os.RemoteException;
}

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Projects20\\Mobile\\Android\\AxSyncClient\\src\\com\\futureconcepts\\ax\\sync\\client\\ISyncListener.aidl
 */
package com.futureconcepts.ax.sync.client;
public interface ISyncListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.futureconcepts.ax.sync.client.ISyncListener
{
private static final java.lang.String DESCRIPTOR = "com.futureconcepts.ax.sync.client.ISyncListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.futureconcepts.ax.sync.client.ISyncListener interface,
 * generating a proxy if needed.
 */
public static com.futureconcepts.ax.sync.client.ISyncListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.futureconcepts.ax.sync.client.ISyncListener))) {
return ((com.futureconcepts.ax.sync.client.ISyncListener)iin);
}
return new com.futureconcepts.ax.sync.client.ISyncListener.Stub.Proxy(obj);
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
case TRANSACTION_onStart:
{
data.enforceInterface(DESCRIPTOR);
this.onStart();
return true;
}
case TRANSACTION_onRescheduled:
{
data.enforceInterface(DESCRIPTOR);
this.onRescheduled();
return true;
}
case TRANSACTION_onStop:
{
data.enforceInterface(DESCRIPTOR);
this.onStop();
return true;
}
case TRANSACTION_onTransaction:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.ISyncTransaction _arg0;
_arg0 = com.futureconcepts.ax.sync.client.ISyncTransaction.Stub.asInterface(data.readStrongBinder());
this.onTransaction(_arg0);
return true;
}
case TRANSACTION_onTransactionComplete:
{
data.enforceInterface(DESCRIPTOR);
com.futureconcepts.ax.sync.client.ISyncTransaction _arg0;
_arg0 = com.futureconcepts.ax.sync.client.ISyncTransaction.Stub.asInterface(data.readStrongBinder());
this.onTransactionComplete(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.futureconcepts.ax.sync.client.ISyncListener
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
@Override public void onStart() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onStart, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onRescheduled() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onRescheduled, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onStop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onStop, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onTransaction(com.futureconcepts.ax.sync.client.ISyncTransaction transaction) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((transaction!=null))?(transaction.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_onTransaction, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onTransactionComplete(com.futureconcepts.ax.sync.client.ISyncTransaction transaction) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((transaction!=null))?(transaction.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_onTransactionComplete, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onRescheduled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onStop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onTransaction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onTransactionComplete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void onStart() throws android.os.RemoteException;
public void onRescheduled() throws android.os.RemoteException;
public void onStop() throws android.os.RemoteException;
public void onTransaction(com.futureconcepts.ax.sync.client.ISyncTransaction transaction) throws android.os.RemoteException;
public void onTransactionComplete(com.futureconcepts.ax.sync.client.ISyncTransaction transaction) throws android.os.RemoteException;
}

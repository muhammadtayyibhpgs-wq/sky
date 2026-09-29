package com.asky.sunmiprinter;

import android.content.Context;
import android.os.RemoteException;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallbcak;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public final class PrinterBridge {
    private final Context context;
    private SunmiPrinterService service;
    private final InnerPrinterCallback callback = new InnerPrinterCallback() {
        @Override protected void onConnected(SunmiPrinterService s) { service = s; }
        @Override protected void onDisconnected() { service = null; }
    };
    public PrinterBridge(Context context) { this.context = context.getApplicationContext(); }
    public boolean connect() {
        try { return InnerPrinterManager.getInstance().bindService(context, callback); }
        catch (Exception e) { return false; }
    }
    public boolean isConnected() { return service != null; }
    public void disconnect() {
        try { InnerPrinterManager.getInstance().unBindService(context, callback); } catch (Exception ignored) {}
        service = null;
    }
    public void printText(String text, final Result result) {
        if (service == null) { if (result != null) result.done(false, "Printer service is not connected"); return; }
        try {
            service.printText(text.endsWith("\n") ? text : text + "\n", new InnerResultCallbcak() {
                @Override public void onRunResult(boolean ok) throws RemoteException { if (result != null) result.done(ok, ok ? "Print command accepted" : "Print command failed"); }
                @Override public void onReturnString(String s) throws RemoteException {}
                @Override public void onRaiseException(int code, String msg) throws RemoteException { if (result != null) result.done(false, "Printer error " + code + ": " + msg); }
                @Override public void onPrintResult(int code, String msg) throws RemoteException {}
            });
        } catch (RemoteException e) { if (result != null) result.done(false, e.toString()); }
    }
    public String status() {
        if (service == null) return "NOT_CONNECTED";
        try { return "CONNECTED / status=" + service.updatePrinterState() + " / model=" + service.getPrinterModal() + " / version=" + service.getPrinterVersion(); }
        catch (Exception e) { return "CONNECTED"; }
    }
    public interface Result { void done(boolean ok, String message); }
}

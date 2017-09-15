package com.mobile.device.manage.download.report.exceptions;

public class QueueDownloadNotStartedException extends IllegalStateException {

    public QueueDownloadNotStartedException(){
        super("Queue Download not started yet");
    }
}

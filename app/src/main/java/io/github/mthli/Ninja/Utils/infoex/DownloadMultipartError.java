package io.github.mthli.Ninja.Utils.infoex;


import io.github.mthli.Ninja.Utils.DownloadInfo;

public class DownloadMultipartError extends DownloadError {

    private static final long serialVersionUID = 7835308901669107488L;

    DownloadInfo info;

    public DownloadMultipartError(DownloadInfo info) {
        super("Multipart error");

        this.info = info;
    }

    public DownloadInfo getInfo() {
        return info;
    }

}

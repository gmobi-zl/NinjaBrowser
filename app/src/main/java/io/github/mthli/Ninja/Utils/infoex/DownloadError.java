package io.github.mthli.Ninja.Utils.infoex;

public class DownloadError extends RuntimeException {

    private static final long serialVersionUID = 7835308901669107488L;

    public DownloadError() {
    }

    public DownloadError(Throwable e) {
        super(e);
    }

    public DownloadError(String str) {
        super(str);
    }

}

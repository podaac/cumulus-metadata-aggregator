package gov.nasa.cumulus.metadata.exception;

public class GEOProcessException extends  RuntimeException{
    String _errorMsg = "";
    public GEOProcessException(String error) {
        this._errorMsg = error;
    }

    public String getError() {
        return this._errorMsg;
    }
}

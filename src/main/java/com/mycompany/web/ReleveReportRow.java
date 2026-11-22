package com.mycompany.web;

public final class ReleveReportRow {

    private final String ueCode;
    private final String ueLabel;
    private final String ecCode;
    private final String ecLabel;
    private final String coefficient;
    private final String ecAverage;
    private final String ueAverage;

    public ReleveReportRow(
            String ueCode,
            String ueLabel,
            String ecCode,
            String ecLabel,
            String coefficient,
            String ecAverage,
            String ueAverage) {
        this.ueCode = ueCode;
        this.ueLabel = ueLabel;
        this.ecCode = ecCode;
        this.ecLabel = ecLabel;
        this.coefficient = coefficient;
        this.ecAverage = ecAverage;
        this.ueAverage = ueAverage;
    }

    public String getUeCode() {
        return ueCode;
    }

    public String getUeLabel() {
        return ueLabel;
    }

    public String getEcCode() {
        return ecCode;
    }

    public String getEcLabel() {
        return ecLabel;
    }

    public String getCoefficient() {
        return coefficient;
    }

    public String getEcAverage() {
        return ecAverage;
    }

    public String getUeAverage() {
        return ueAverage;
    }
}

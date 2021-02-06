package models;

import enumerations.OnCallLicenseType;

public class OnCallLicense {
    private OnCallLicenseType type;
    private int amount;

    public OnCallLicense() {}

    public OnCallLicenseType getType() {
        return type;
    }

    public void setType(OnCallLicenseType type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "OnCallLicense{" +
                "type=" + type +
                ", amount=" + amount +
                '}';
    }
}

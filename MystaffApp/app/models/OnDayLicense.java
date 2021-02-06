package models;

import enumerations.OnDayLicenseType;

public class OnDayLicense {
    private OnDayLicenseType type;
    private int amount;

    public OnDayLicense() {}

    public OnDayLicenseType getType() {
        return type;
    }

    public void setType(OnDayLicenseType type) {
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
        return "OnDayLicense{" +
                "type=" + type +
                ", amount=" + amount +
                '}';
    }
}

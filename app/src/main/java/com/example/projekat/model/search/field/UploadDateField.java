package com.example.projekat.model.search.field;

public enum UploadDateField implements SearchField {

    HOUR(1, 1),
    DAY(1, 2),
    WEEK(1, 3),
    MONTH(1, 4),
    YEAR(1, 5);

    private final byte[] data;

    UploadDateField(int... data) {
        this.data = SearchField.convert(data);
    }

    @Override
    public byte[] data() {
        return data;
    }
}

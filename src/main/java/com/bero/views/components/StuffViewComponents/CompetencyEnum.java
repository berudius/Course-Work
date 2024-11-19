package com.bero.views.components.StuffViewComponents;

public enum CompetencyEnum {
    BEGINNER("Початківець"),
    INTERMEDIATE("Середній Спеціаліст"),
    PROFESSIONAL("Професіонал"),
    EXPERT("Експерт"),
    MASTER("Майстер");
    
    private final String status;


    CompetencyEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isGreaterOrEq(CompetencyEnum other) {
        boolean result = this.ordinal() >= other.ordinal();
        return result;
    }

    public boolean isLesserOrEq(CompetencyEnum other) {
        return this.ordinal() <= other.ordinal();
    }
}

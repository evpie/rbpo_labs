package com.example.lab2.model;

public class ReleaseCheck {
    private Long id;
    private Long releaseId; // Ссылка на ProductRelease
    private String checkName;
    private boolean isMandatory; // Обязательность проверки (true/false)
    private String result;       // Результат проверки

    public ReleaseCheck() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(Long releaseId) {
        this.releaseId = releaseId;
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    // ВАЖНО: для boolean геттер называется isMandatory(), а не getMandatory()
    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
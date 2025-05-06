package tn.esprit.examen.nomPrenomClasseExamen.dto;

import java.util.List;

public class TranslationRequest {
    private String text;
    private String sourceLang;
    private List<String> targetLangs;

    // Getters
    public String getText() {
        return text;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public List<String> getTargetLangs() {
        return targetLangs;
    }

    // Setters
    public void setText(String text) {
        this.text = text;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public void setTargetLangs(List<String> targetLangs) {
        this.targetLangs = targetLangs;
    }
}

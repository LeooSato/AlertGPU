package com.sato.alertsgpu.match;

import com.sato.alertsgpu.model.Alert;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MatchService {

    private static final Pattern VRAM_PATTERN = Pattern.compile("\\b(8|10|12|16|20|24)\\s?GB\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MODEL_PATTERN = Pattern.compile("\\b(\\d{4})\\b");

    public boolean matches(Alert alert, String rawTitle) {
        String title = normalize(rawTitle);

        // anti-falso-positivo
        if (title.contains("NOTEBOOK") || title.contains("LAPTOP")) return false;
        if (!(title.contains("RTX") || title.contains("GEFORCE"))) return false;

        Integer model = extractModel(title);
        Integer vram = extractVram(title);

        return model != null && vram != null
                && model.equals(alert.getGpuModel())
                && vram.equals(alert.getVramGb());
    }

    private String normalize(String s) {
        return s == null ? "" : s.toUpperCase(Locale.ROOT);
    }

    private Integer extractVram(String title) {
        Matcher m = VRAM_PATTERN.matcher(title);
        if (!m.find()) return null;
        return Integer.parseInt(m.group(1));
    }

    private Integer extractModel(String title) {
        Matcher m = MODEL_PATTERN.matcher(title);
        while (m.find()) {
            int val = Integer.parseInt(m.group(1));
            // filtra pra pegar modelos comuns e evitar pegar ano 2024 etc
            if (val >= 3000 && val <= 9999) return val;
        }
        return null;
    }
}
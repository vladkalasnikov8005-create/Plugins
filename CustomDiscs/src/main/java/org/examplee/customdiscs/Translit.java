package org.examplee.customdiscs;

import java.util.Locale;
import java.util.Map;

/** Транслитерация кириллицы и санитизация имён файлов в id пластинок. */
public final class Translit {

    private static final Map<String, String> CYR = Map.ofEntries(
            Map.entry("а","a"), Map.entry("б","b"), Map.entry("в","v"), Map.entry("г","g"),
            Map.entry("д","d"), Map.entry("е","e"), Map.entry("ё","e"), Map.entry("ж","zh"),
            Map.entry("з","z"), Map.entry("и","i"), Map.entry("й","y"), Map.entry("к","k"),
            Map.entry("л","l"), Map.entry("м","m"), Map.entry("н","n"), Map.entry("о","o"),
            Map.entry("п","p"), Map.entry("р","r"), Map.entry("с","s"), Map.entry("т","t"),
            Map.entry("у","u"), Map.entry("ф","f"), Map.entry("х","h"), Map.entry("ц","c"),
            Map.entry("ч","ch"), Map.entry("ш","sh"), Map.entry("щ","sch"), Map.entry("ъ",""),
            Map.entry("ы","y"), Map.entry("ь",""), Map.entry("э","e"), Map.entry("ю","yu"),
            Map.entry("я","ya"), Map.entry("і","i"), Map.entry("ї","yi"), Map.entry("є","e"),
            Map.entry("ґ","g")
    );

    private Translit() {
    }

    /** "Моя Крутая Песня" -> "moya_krutaya_pesnya" */
    public static String toId(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lower.length(); i++) {
            String ch = lower.substring(i, i + 1);
            if (CYR.containsKey(ch)) {
                sb.append(CYR.get(ch));
            } else if (ch.matches("[a-z0-9]")) {
                sb.append(ch);
            } else if (ch.equals(" ") || ch.equals("-") || ch.equals("_") || ch.equals(".")) {
                sb.append('_');
            }
        }
        // схлопнуть повторные подчёркивания и обрезать по краям
        String out = sb.toString().replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        if (out.length() > 48) out = out.substring(0, 48).replaceAll("_+$", "");
        return out;
    }
}

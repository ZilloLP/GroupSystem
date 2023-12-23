package de.zillolp.groupsystem.config.customconfigs;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageConfig extends CustomConfig {
    public LanguageConfig(GroupSystem plugin, String name) {
        super(plugin, name);
    }

    public String getReplacedColorCodes(String language) {
        return ChatColor.translateAlternateColorCodes('&', language);
    }

    private String getTranslatedString(String language) {
        return getReplacedColorCodes(fileConfiguration.getString(language, language + " "));
    }

    // This method is always used when player data needs to be replaced in a language.
    private String getReplacedString(String language, UUID uuid) {
        language = getTranslatedString(language);
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        if (!(playerProfiles.containsKey(uuid))) {
            return language;
        }
        PlayerProfile playerProfile = playerProfiles.get(uuid);
        String groupName = playerProfile.getGroup();
        GroupConfig groupConfig = plugin.getGroupConfig();
        language = language.replace("%player%", playerProfile.getName());
        language = language.replace("%group%", groupConfig.getGroupName(groupName));
        language = language.replace("%prefix%", ChatColor.translateAlternateColorCodes('&', groupConfig.getPrefix(groupName)));
        language = formatTime(language, playerProfile.getExpirationDate() - System.currentTimeMillis());
        return language;
    }

    public String getTranslatedLanguage(Language language) {
        return getTranslatedString(language.name());
    }

    public String getLanguageWithPrefix(Language language) {
        return getTranslatedLanguage(Language.PREFIX) + getTranslatedLanguage(language);
    }

    public String getReplacedLanguage(Language language, UUID uuid, boolean hasPrefix) {
        String languageString = getReplacedString(language.name(), uuid);
        if (hasPrefix) {
            languageString = getTranslatedLanguage(Language.PREFIX) + languageString;
        }
        return languageString;
    }

    public String[] getTranslatedLanguages(Language languageSection, boolean hasPrefix) {
        String sectionName = languageSection.name();
        if (!(fileConfiguration.isConfigurationSection(sectionName))) {
            return new String[]{sectionName};
        }
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(sectionName);
        if (configurationSection == null) {
            return new String[]{sectionName};
        }
        ArrayList<String> languages = new ArrayList<>();
        for (String languageName : configurationSection.getKeys(false)) {
            String language = getTranslatedString(sectionName + "." + languageName);
            if (hasPrefix) {
                language = getTranslatedLanguage(Language.PREFIX) + language;
            }
            languages.add(language);
        }
        return languages.toArray(new String[0]);
    }

    public String[] getReplacedLanguages(Language languageSection, UUID uuid, boolean hasPrefix) {
        String sectionName = languageSection.name();
        if (!(fileConfiguration.isConfigurationSection(sectionName))) {
            return new String[]{sectionName};
        }
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(sectionName);
        if (configurationSection == null) {
            return new String[]{sectionName};
        }
        ArrayList<String> languages = new ArrayList<>();
        for (String languageName : configurationSection.getKeys(false)) {
            String language = getReplacedString(sectionName + "." + languageName, uuid);
            if (hasPrefix) {
                language = getTranslatedLanguage(Language.PREFIX) + language;
            }
            languages.add(language);
        }
        return languages.toArray(new String[0]);
    }


    private String formatTime(String language, long time) {
        long seconds = time / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        long Hours = hours % 24;
        long Minutes = minutes % 60;
        long Seconds = seconds % 60;

        language = language.replace("%days%", String.valueOf(days));
        language = language.replace("%hours%", String.valueOf(Hours));
        language = language.replace("%minutes%", String.valueOf(Minutes));
        language = language.replace("%seconds%", String.valueOf(Seconds));
        language = language.replace("%duration%", String.format("%02d", days) + ":" + String.format("%02d", Hours) + ":" + String.format("%02d", Minutes) + ":" + String.format("%02d", Seconds));
        return language;
    }

    public boolean isNumeric(String value) {
        try {
            int number = Integer.parseInt(value);
            return number >= 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}

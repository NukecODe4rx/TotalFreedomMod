package me.totalfreedom.totalfreedommod.discord.pluralkit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record PluralKitMember(String name, @SerializedName("keep_proxy") boolean keepProxy, @SerializedName("proxy_tags") List<PluralKitTag> tags) {
    public boolean matches(final String message) {
        for (final PluralKitTag tag: this.tags()) {
            final String prefix = tag.prefix() != null ? tag.prefix() : "";
            final String suffix = tag.suffix() != null ? tag.suffix() : "";

            if (message.length() <= (prefix.length() + suffix.length())) continue;
            if (message.startsWith(prefix) && message.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    public String stripMessage(final String message) {
        if (this.keepProxy()) return message;

        for (final PluralKitTag tag: this.tags()) {
            final String prefix = tag.prefix() != null ? tag.prefix() : "";
            final String suffix = tag.suffix() != null ? tag.suffix() : "";

            if (message.length() <= (prefix.length() + suffix.length())) continue;
            if (message.startsWith(prefix) && message.endsWith(suffix)) {
                return message.substring(prefix.length(), message.length()-suffix.length()).trim();
            }
        }

        return null;
    }
}

package me.totalfreedom.totalfreedommod.discord.pluralkit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record PluralKitMember(String name, @SerializedName("autoproxy_enabled") Boolean autoProxy, @SerializedName("keep_proxy") boolean keepProxy, @SerializedName("proxy_tags") List<PluralKitTag> tags) {
    public boolean matches(final String message) {
        if (this.autoProxy() != null && this.autoProxy()) return true;

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
        if ((this.autoProxy() != null && this.autoProxy()) || this.keepProxy()) return message;

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

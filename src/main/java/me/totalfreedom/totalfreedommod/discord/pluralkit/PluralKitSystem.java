package me.totalfreedom.totalfreedommod.discord.pluralkit;

import java.util.List;

public record PluralKitSystem(List<PluralKitMember> members) {
    public PluralKitMember getMemberForMessage(final String message) {
        for (final PluralKitMember member: members) {
            if (member.matches(message)) return member;
        }

        return null; // Not proxied
    }
}

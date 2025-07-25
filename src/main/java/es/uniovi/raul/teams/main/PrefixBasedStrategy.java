package es.uniovi.raul.teams.main;

import es.uniovi.raul.teams.model.Group2TeamMapper;

class PrefixBasedStrategy implements Group2TeamMapper {
    private final String prefix;

    PrefixBasedStrategy(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("Prefix cannot be null");
        this.prefix = prefix;
    }

    @Override
    public String getTeamDisplayName(String groupId) {
        return prefix + groupId;
    }
}

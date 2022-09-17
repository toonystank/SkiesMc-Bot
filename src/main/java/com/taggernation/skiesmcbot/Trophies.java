package com.taggernation.skiesmcbot;

import com.taggernation.taggernationlib.config.ConfigManager;
import lombok.Getter;
import lombok.Setter;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.time.LocalDate;

@Getter @Setter
public class Trophies extends ConfigManager {


    private final Plugin plugin;
    private final PlayerPointsAPI pointsAPI;
    private LocalDate startingDate;
    private LocalDate endingDate;

    public Trophies(Plugin plugin, PlayerPointsAPI pointsAPI) throws IOException {
        super(plugin, "trophies.yml", false, true);
        this.plugin = plugin;
        this.pointsAPI = pointsAPI;
    }

    void setupDefaults() {
        startingDate = LocalDate.parse(this.getString("starting_date"));
        endingDate = LocalDate.parse(this.getString("ending_date"));
    }


}

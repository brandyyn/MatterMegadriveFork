package matteroverdrive.starmap;

import matteroverdrive.api.starmap.GalacticPosition;
import matteroverdrive.starmap.data.Galaxy;
import matteroverdrive.starmap.data.Planet;
import matteroverdrive.starmap.data.Quadrant;
import matteroverdrive.starmap.data.Star;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public abstract class GalaxyCommon {
    protected Galaxy theGalaxy;
    protected World world;
    protected Random random;
    protected HashMap<UUID, Planet> homePlanets;

    public GalaxyCommon() {
        random = new Random();
        homePlanets = new HashMap<>();
    }

    public void loadClaimedPlanets() {
        homePlanets.clear();

        for (Quadrant quadrant : theGalaxy.getQuadrants()) {
            for (Star star : quadrant.getStars()) {
                for (Planet planet : star.getPlanets()) {
                    if (planet.isHomeworld() && planet.hasOwner()) {
                        homePlanets.put(planet.getOwnerUUID(), planet);
                    }
                }
            }
        }
    }

    //region getters and setters
    public Planet getPlanet(GalacticPosition position) {
        if (theGalaxy != null) {
            return theGalaxy.getPlanet(position);
        }
        return null;
    }

    public Star getStar(GalacticPosition position) {
        if (theGalaxy != null) {
            return theGalaxy.getStar(position);
        }
        return null;
    }

    public Quadrant getQuadrant(GalacticPosition position) {
        if (theGalaxy != null) {
            return theGalaxy.getQuadrant(position);
        }
        return null;
    }

    public Planet getHomeworld(EntityPlayer player) {
        return homePlanets.get(EntityPlayer.func_146094_a(player.getGameProfile()));
    }

    public Galaxy getTheGalaxy() {
        return theGalaxy;
    }

    public void setTheGalaxy(Galaxy galaxy) {
        theGalaxy = galaxy;
        if (theGalaxy != null)
            loadClaimedPlanets();
    }
    //endregion
}

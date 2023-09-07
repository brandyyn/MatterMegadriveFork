package matteroverdrive.tile.pipes;

import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.List;

public class WeightedDirection {
    public float weight = 0;
    public ForgeDirection dir;

    public WeightedDirection(ForgeDirection dir, float weight) {
        this.weight = weight;
        this.dir = dir;
    }

    public static void Sort(List<WeightedDirection> dirs) {
        Collections.sort(dirs, (o1, o2) -> {
            if (o1.weight > o2.weight)
                return 1;
            else if (o1.weight < o2.weight)
                return -1;
            else
                return 0;
        });
    }
}

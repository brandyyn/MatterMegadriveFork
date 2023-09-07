package matteroverdrive.animation;

import matteroverdrive.util.math.MOMathHelper;

import java.util.ArrayList;
import java.util.List;

public class AnimationTimeline<T extends AnimationSegment> {
    boolean loopable;
    int time;
    int duration;
    List<T> segments;
    int lastSegmentBegin;

    public AnimationTimeline(boolean loopable, int duration) {
        segments = new ArrayList<>();
        this.loopable = loopable;
        this.duration = duration;
    }

    public double getPercent() {
        return (double) time / (double) duration;
    }

    public void addSegment(T segment) {
        segments.add(segment);
    }

    public void addSegmentSequential(T segment) {
        segment.begin = lastSegmentBegin;
        lastSegmentBegin += segment.length;
        segments.add(segment);
    }

    public T getCurrentSegment() {
        for (T segment : segments) {
            if (MOMathHelper.animationInRange(time, segment.begin, segment.length)) {
                return segment;
            }
        }
        return null;
    }

    public void tick() {
        if (time < duration) {
            time++;
        } else if (loopable) {
            time = 0;
        }
    }

    public void setTime(int time) {
        this.time = time;
    }
}

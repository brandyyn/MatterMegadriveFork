package matteroverdrive.animation;

import matteroverdrive.util.MOStringHelper;

public class AnimationSegmentText extends AnimationSegment {
    String string;
    int animationType;

    public AnimationSegmentText(String string, int begin, int length, int animationType) {
        super(begin, length);
        this.string = string;
        this.animationType = animationType;
    }

    public AnimationSegmentText(String string, int length, int animationType) {
        this(string, 0, length, animationType);
    }

    public AnimationSegmentText setLengthPerCharacter(int length) {
        this.length = string.length() * length;
        return this;
    }

    public String getText(int time) {
        if (animationType == 1) {
            return MOStringHelper.typingAnimation(string, (time - begin), length);
        }
        return string;
    }
}

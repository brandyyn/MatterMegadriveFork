package matteroverdrive.machines.configs;

public abstract class ConfigPropertyAbstract implements IConfigProperty {
    protected String key;
    protected String unlocalizedName;

    public ConfigPropertyAbstract(String key, String unlocalizedName) {
        this.key = key;
        this.unlocalizedName = unlocalizedName;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getUnlocalizedName() {
        return unlocalizedName;
    }
}

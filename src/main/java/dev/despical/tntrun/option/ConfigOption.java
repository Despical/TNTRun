package dev.despical.tntrun.option;

import dev.despical.tntrun.Main;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public interface ConfigOption<T> {

    String getPath();

    Class<T> getType();

    T getDefaultValue();

    default T value() {
        return Main.getInstance().getOptions().get(this);
    }
}

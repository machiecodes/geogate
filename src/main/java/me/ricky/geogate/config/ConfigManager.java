package me.ricky.geogate.config;

import me.ricky.geogate.Geogate;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Path;

public class ConfigManager {
    private static File configFile;
    private static final Yaml yaml;

    static {
        LoaderOptions loaderOptions = new LoaderOptions();
        Constructor constructor = new Constructor(GeogateConfig.class, loaderOptions);

        DumperOptions dumperOptions = new DumperOptions();
        // Include spaces between brackets and settings in arrays
        dumperOptions.setPrettyFlow(true);
        // Format list objects as a bulleted list rather than an array
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer representer = new Representer(dumperOptions);

        yaml = new Yaml(constructor, representer, dumperOptions);
    }

    /**
     * Creates a new {@link GeogateConfig} object and sets its fields based on the
     * config/geogate.yaml file. If no such file exists or the existing file is
     * formatted incorrectly, a new default config will be created.
     *
     * @return The deserialized GeogateConfig object
     */
    public static GeogateConfig load() {
        Path configFolder = FabricLoader.getInstance().getConfigDir();
        configFile = new File(configFolder.toFile(), "geogate.yaml");

        GeogateConfig config = new GeogateConfig();

        if (!configFile.exists()) {
            save(config);
            return config;
        }

        try (FileInputStream stream = new FileInputStream(configFile)) {
            config = yaml.load(stream);
        } catch (IOException e) {
            Geogate.LOG.error("Failed to read config file!", e);
        } catch (YAMLException e) {
            Geogate.LOG.error("Config file has broken formatting!", e);
        }

        if (config == null) {
            config = new GeogateConfig();
            save(config);
        }

        return config;
    }

    /**
     * Serializes a {@link GeogateConfig} object to a config file, adding comments
     * above fields as indicated by {@link Comment} annotations and creating the
     * needed directories/files if they don't already exist.
     *
     * @param config The GeogateConfig object to serialize
     */
    public static void save(GeogateConfig config) {
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException e) {
                Geogate.LOG.error("Failed to create config file!", e);
            }
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            String configYaml = yaml.dump(config);
            configYaml = addComments(configYaml);

            writer.write(configYaml);
        } catch (IOException e) {
            Geogate.LOG.error("Failed to write config file!", e);
        }
    }

    private static String addComments(String yaml) {
        // TODO parse string and add comments via annotations
        return yaml;
    }
}

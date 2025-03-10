package me.machie.geogate.config;

import me.machie.geogate.Geogate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    private static File configFile;
    private static final Yaml yaml;

    static {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag ->
            tag.getClassName().equals(GeogateConfig.class.getName()));

        Constructor constructor = new Constructor(GeogateConfig.class, loaderOptions);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setProcessComments(true);
        dumperOptions.setSplitLines(false);

        PrettyRepresenter representer = new PrettyRepresenter(dumperOptions);
        representer.setPropertyUtils(new OrderedPropertyUtils());

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

        GeogateConfig config = null;

        if (!configFile.exists()) {
            config = new GeogateConfig();
            save(config);
            return config;
        }

        try (FileInputStream stream = new FileInputStream(configFile)) {
            config = yaml.load(stream);
        } catch (IOException e) {
            Geogate.LOG.error("Encountered error while reading config:", e);
        } catch (YAMLException e) {
            Geogate.LOG.error("Encountered bad formatting in config:", e);
        }

        if (config == null) {
            File brokenFile = new File(configFolder.toFile(), "geogate.yaml.broken");
            configFile.renameTo(brokenFile);
            configFile.delete();

            Geogate.LOG.error("""
                \nGeogate encountered an error while attempting to load your config.
                To prevent players from outside locations joining, the server startup
                has been halted.
                
                If this was an I/O exception, please open and attach this log make sure
                the files in the config folder can be accessed without admin privileges.
                
                If this was a problem with your config's formatting, it has been renamed
                to geogate.yaml.broken. You can fix the issue (which should be indicated
                in the error message), or create a new default config by restarting.
                """
            );
        }

        return config;
    }

    /**
     * Serializes a {@link GeogateConfig} object to a config file, adding comments
     * around fields as indicated by annotations and creating the needed directories
     * or files if they don't already exist.
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
            yaml.dump(config, writer);
        } catch (IOException e) {
            Geogate.LOG.error("Failed to write config file!", e);
        }
    }

    /**
     * Custom version of PropertyUtils that replaces the TreeSet with a LinkedHashSet
     * when getting all the properties of a bean, keeping the order of declaration.
     */
    private static class OrderedPropertyUtils extends PropertyUtils {
        @Override
        protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
            Set<Property> properties = new LinkedHashSet<>();

            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                properties.add(new FieldProperty(field));
            }

            return properties;
        }
    }

    /**
     * Custom representer with several changes to improve config readability, including:
     * <ul>
     *     <li>Support for comments added via field annotations</li>
     *     <li>Spaces between commented fields</li>
     *     <li>Wrapping string values with quotes</li>
     * </ul>
     */
    private static class PrettyRepresenter extends Representer {
        private boolean lastHadComments = false;

        public PrettyRepresenter(DumperOptions options) {
            super(options);
        }

        @Override
        protected NodeTuple representJavaBeanProperty(
            Object javaBean, Property property,
            Object propertyValue, Tag customTag
        ) {
            // Doesn't seem like these matter after digging around a bit so just make dummies
            Mark start = new Mark("", 0, 0, 0, new char[0], 0);
            Mark end = new Mark("", 0, 0, 0, new char[0], 0);

            Node keyNode = new ScalarNode(Tag.STR, property.getName(), start, end, DumperOptions.ScalarStyle.PLAIN);
            Node valueNode = representData(propertyValue);

            try {
                Field field = javaBean.getClass().getDeclaredField(property.getName());

                PreComment preComment = field.getAnnotation(PreComment.class);
                PostComment postComment = field.getAnnotation(PostComment.class);

                if (preComment != null) {
                    List<CommentLine> comments = new ArrayList<>();

                    if (!lastHadComments) comments.add(new CommentLine(start, end, "", CommentType.BLANK_LINE));

                    for (String line : preComment.value().split("\n")) {
                        CommentLine comment = new CommentLine(start, end, " " + line, CommentType.BLOCK);
                        comments.add(comment);
                    }

                    keyNode.setBlockComments(comments);
                }

                if (postComment != null) {
                    List<CommentLine> comments = new ArrayList<>();

                    for (String line : postComment.value().split("\n")) {
                        CommentLine comment = new CommentLine(start, end, " " + line, CommentType.BLOCK);
                        comments.add(comment);
                    }

                    comments.add(new CommentLine(start, end, "", CommentType.BLANK_LINE));

                    valueNode.setEndComments(comments);
                }

                lastHadComments = preComment != null || postComment != null;

                return new NodeTuple(keyNode, valueNode);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            lastHadComments = false;
            return super.representJavaBean(properties, javaBean);
        }

        @Override
        protected Node representScalar(Tag tag, String value, DumperOptions.ScalarStyle style) {
            if (tag.equals(Tag.STR)) style = DumperOptions.ScalarStyle.DOUBLE_QUOTED;
            return super.representScalar(tag, value, style);
        }
    }
}

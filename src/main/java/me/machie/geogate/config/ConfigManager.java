package me.machie.geogate.config;

import me.machie.geogate.Geogate;
import net.fabricmc.loader.api.FabricLoader;
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
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
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
        // Ensure we don't try to serialize objects not of the config type
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag ->
            tag.getClassName().equals(GeogateConfig.class.getName()));

        Constructor constructor = new Constructor(GeogateConfig.class, loaderOptions);

        // Configure dumping an object to a file
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
            Geogate.LOG.error("Config file had invalid formatting:", e);

            // TODO Figure out what happens if there's invalid formatting
        }

        if (config == null) {
            config = new GeogateConfig();
            save(config);
        }

        return config;
    }

    /**
     * Serializes a {@link GeogateConfig} object to a config file, adding comments
     * above fields as indicated by {@link PreComment} annotations and creating the
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

            // Get properties in the order they were declared
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                properties.add(new FieldProperty(field));
            }

            return properties;
        }
    }

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
            NodeTuple original = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);

            Node keyNode = original.getKeyNode();
            Node valueNode = original.getValueNode();

            try {
                Field field = javaBean.getClass().getDeclaredField(property.getName());

                PreComment preComment = field.getAnnotation(PreComment.class);
                PostComment postComment = field.getAnnotation(PostComment.class);

                Mark start = keyNode.getStartMark();
                Mark end = keyNode.getEndMark();

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

                return original;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            lastHadComments = false;
            return super.representJavaBean(properties, javaBean);
        }
    }
}

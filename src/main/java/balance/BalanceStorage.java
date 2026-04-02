package balance;

import com.google.gson.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public final class BalanceStorage {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void save(Class<?> clazz, String path) {
        try {
            Map<String, Object> map = new HashMap<>();
            for (Field field : clazz.getDeclaredFields()) {
                int mods = field.getModifiers();
                if (Modifier.isStatic(mods) && Modifier.isPublic(mods) && !Modifier.isFinal(mods)) {
                    field.setAccessible(true);
                    map.put(field.getName(), field.get(null));
                }
            }

            try (Writer writer = new FileWriter(path)) {
                gson.toJson(map, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load(Class<?> clazz, String path) {
        Map<String, Object> jsonMap = new HashMap<>();

        try (Reader reader = new FileReader(path)) {
            // Use Map<String, Object> to read JSON
            jsonMap = gson.fromJson(reader, Map.class);
        } catch (FileNotFoundException e) {
            System.out.println("No balance file found, using defaults.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean modified = false;

        for (Field field : clazz.getDeclaredFields()) {
            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) && Modifier.isPublic(mods) && !Modifier.isFinal(mods)) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = jsonMap.get(name);

                try {
                    if (value != null) {
                        Object casted = ReflectionUtil.castValue(field.getType(), value);
                        field.set(null, casted);
                    } else {
                        // Missing field, write default to JSON
                        jsonMap.put(name, field.get(null));
                        modified = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (modified) {
            save(clazz, path);
        }
    }
}
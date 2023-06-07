package me.totalfreedom.totalfreedommod.util;

import com.google.common.reflect.ClassPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Reflections {
    private final String packageName;

    public Reflections(final @NotNull String packageName) {
        this.packageName = packageName;
    }

    @SuppressWarnings("UnstableApiUsage")
    public @Unmodifiable <T> Set<Class<? extends T>> getSubTypesOf(final @NotNull Class<T> typeClass) {
        final Set<Class<? extends T>> classes = new LinkedHashSet<>();

        final ClassLoader classLoader = this.getClass().getClassLoader();
        final ClassPath classPath;

        try {
            classPath = ClassPath.from(classLoader);
        } catch (IOException e) {
            throw new UncheckedIOException("IOException during reflective operation", e);
        }

        final Set<ClassPath.ClassInfo> classInfos = classPath.getTopLevelClassesRecursive(this.packageName);

        for (final ClassPath.ClassInfo classInfo : classInfos) {
            final Class<?> loadedClass = classInfo.load();

            if (!typeClass.isAssignableFrom(loadedClass)) {
                continue;
            }

            if (typeClass.equals(loadedClass)) {
                continue;
            }

            classes.add(loadedClass.asSubclass(typeClass));
        }

        return Collections.unmodifiableSet(classes);
    }
}

package net.glxn.mirrah;

import net.glxn.mirrah.exception.ReflectionException;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Reflection
 *
 * Class contains utility reflection methods.
 * See: {@link Reflection#hierarchy(Class)} and {@link Reflection#createInstance(Class)}
 */
@SuppressWarnings("UnusedDeclaration")
public class Reflection {

    /**
     * This method returns a complete list of all {@link Class}es in the hierarchy of the object sent in as the argument including itself and all the way up to and including {@link Object}.<br/>
     * e.g. for class hierarchy:
     * <pre>
     *     Object
     *       |
     *    Vehicle
     *    |     |
     *   Car  Motorcycle
     * </pre>
     *
     * Calling {@code Reflection.hierarchy(Car.class)} will return [Car.class, Vehicle.class, Object.class] as a list
     *
     * @param clazz the class for which to get the class hierarchy list
     * @return a list of classes that are in the hierarchy of the argument class from, and including, itself up to, and including, Object
     */
    public static List<Class<?>> hierarchy(Class<?> clazz) {
        List<Class<?>> list = new ArrayList<>();
        if (clazz != null) {
            list.add(clazz);
            list.addAll(hierarchy(clazz.getSuperclass()));
        }
        return list;
    }


    /**
     * This method attempts to load the given class using {@link ClassLoader#loadClass(String)}.
     * Given that the class is found, it then creates an instance using {@link Reflection#createInstance(Class)}
     *
     * @param name the fully qualified name of the class to load
     * @param <T> the type of the resulting class
     * @return an instance of the class for the given fully qualified domain name
     * @throws ReflectionException if the class is not found or if an instance can not be created
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstance(String name) throws ReflectionException {
        String message = "failed to create instance of class [%s]. Must be FQDN of a class on the classpath.";
        Class<T> clazz;
        try {
            clazz = (Class<T>) ClassLoader.getSystemClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new ReflectionException(format(message, name), e);
        }
        return createInstance(clazz);
    }

    /**
     * creates a new instance of the given class.
     * Class must have a no args constructor. The constructor can be below public access(e.g. private)
     *
     * @param clazz class of the object you want instantiated.
     * @param <T>   the class type
     * @return an instance of the given class
     * @throws ReflectionException if an instance could not be created
     */
    public static <T> T createInstance(Class<T> clazz) throws ReflectionException {
        String message = "Failed to create instance of type [%s] Make sure the class has a no args constructor";

        T t;
        try {
            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
            if (!Modifier.isPublic(declaredConstructor.getModifiers())) {
                declaredConstructor.setAccessible(true);
            }
            t = declaredConstructor.newInstance();
        } catch (Exception e) {
            throw new ReflectionException(format(message, clazz.getCanonicalName()), e);
        }
        return t;
    }

    /**
     * find all fields in the list of classes. Typically used in conjunction with {@link Reflection#hierarchy(Class)}.
     * To get all fields in a hierarchy
     * <pre>
     *     Reflection.fields(Reflection.hierarchy(Foo.class))}
     * </pre>
     *
     * @param classes list of classes to get fields for
     * @return all fields in the list of classes
     */
    public static List<Field> fields(List<Class<?>> classes) {
        ArrayList<Field> fields = new ArrayList<>();
        for (Class<?> clazz : classes) {
            fields.addAll(asList(clazz.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * finds all fields in the given class.
     * @param clazz the class to get fields for
     * @param includeInheritedFields whether or not to return fields that are inherited.
     * @return all fields for the given class
     */
    public static List<Field> fields(Class<?> clazz, boolean includeInheritedFields) {
        return fields(includeInheritedFields ? hierarchy(clazz) : Arrays.<Class<?>>asList(clazz));
    }

    /**
     * finds all fields with the given annotation in the supplied list of classes.
     * @param annotation the annotation to find fields for
     * @param classes the classes to search
     * @return list of all fields with the given annotation
     */
    public static Collection<Field> fieldsWithAnnotation(Class<? extends Annotation> annotation, List<Class<?>> classes) {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : fields(classes)) {
            Annotation fieldAnnotation = field.getAnnotation(annotation);
            if (fieldAnnotation != null) {
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * finds all fields in the hierarchy of the supplied class with the given annotation.
     * @param annotation the annotation to find fields for
     * @param clazz the class to search
     * @return list of all fields with the given annotation
     */
    public static Collection<Field> fieldsWithAnnotation(Class<? extends Annotation> annotation, Class clazz) {
        return fieldsWithAnnotation(annotation, hierarchy(clazz));
    }

    /**
     * Gets the given annotation defined in a class.
     * @param annotation The annotation to get
     * @param clazz The class in which to look for the annotation
     * @param target Whether the annotation @Target is FIELD or TYPE.
     * @return The annotation
     */
    public static <T extends Annotation> T getAnnotation(Class<T> annotation, Class clazz, ElementType target) {
        switch (target) {
            case FIELD:
                return getAnnotationOnField(annotation, clazz);
            case TYPE:
                return getAnnotationOnClass(annotation, clazz);
            default:
                return null;
        }
    }

    /**
     * Gets the given class level annotation defined in a class.
     * @param annotation The annotation to get
     * @param clazz The class in which to look for the annotation
     * @return The annotation
     */
    public static <T extends Annotation> T getAnnotationOnClass(Class<T> annotation, Class clazz) {
        List<Class<?>> hierarchy = hierarchy(clazz);
        for (Class<?> node : hierarchy) {
            T targetAnnotation = node.getAnnotation(annotation);
            if (targetAnnotation != null) {
                return targetAnnotation;
            }
        }
        return null;
    }

    /**
     * Gets the given field level annotation defined in a class.
     * @param annotation the annotation to get
     * @param clazz The class in which to look for the annotation
     * @return The annotation
     */
    public static <T extends Annotation> T getAnnotationOnField(Class<T> annotation, Class clazz) {
        List<Field> fields = fields(clazz, true);
        for (Field field : fields) {
            T targetAnnotation = field.getAnnotation(annotation);
            if (targetAnnotation != null) {
                return targetAnnotation;
            }
        }
        return null;
    }


    /**
     * gets a declared field by name from the given type
     *
     * @param type      the type to get field from
     * @param fieldName the name of the field to get
     * @return the field found by name
     * @throws ReflectionException if field is not found on class
     */
    public static Field getDeclaredField(Class type, String fieldName) {
        for (Field field : fields(hierarchy(type))) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        throw new ReflectionException("no field named %s on type %s", fieldName, type);
    }

    /**
     * checks if a class has a field with the given name in the class hierarchy
     *
     * @param type      the type to get field from
     * @param fieldName the name of the field to check for
     * @return true if the type has a field with the given name, false otherwise
     */
    public static boolean hasDeclaredField(Class<?> type, String fieldName) {
        for (Field field : fields(hierarchy(type))) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set value on field of an instance.
     *
     * @param field    the field to set value for
     * @param value    the value to set
     * @param instance the instance to set value on
     */
    public static void setValueOnField(Field field, Object value, Object instance) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Set value on field of an instance.
     *
     * @param fieldName the name of the field to set value for
     * @param value     the value to set
     * @param instance  the instance to set value on
     */
    public static void setValueOnField(String fieldName, Object value, Object instance) {
        setValueOnField(getDeclaredField(instance.getClass(), fieldName), value, instance);
    }

    /**
     * Get value from field of an instance.
     *
     * @param field    the field to get value from
     * @param instance the instance to get value from
     */
    public static Object getValueFromField(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Get value from field of an instance.
     *
     * @param fieldName the name ot the field to get value from
     * @param instance  the instance to get value from
     */
    public static Object getValueFromField(String fieldName, Object instance) {
        return getValueFromField(getDeclaredField(instance.getClass(), fieldName), instance);
    }



}

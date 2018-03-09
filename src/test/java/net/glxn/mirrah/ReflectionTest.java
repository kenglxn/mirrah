package net.glxn.mirrah;

import net.glxn.mirrah.exception.ReflectionException;
import org.junit.Test;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static net.glxn.mirrah.ReflectionTest.AspirationType.NATURAL;
import static org.junit.Assert.*;

public class ReflectionTest {

    @Test
    public void finds_all_classes_in_hierarchy() {
        assertEquals(
            asList(Car.class, Vehicle.class, Object.class),
            Reflection.hierarchy(Car.class)
        );
    }

    @Test
    public void creates_instance_using_fully_qualified_domain_name() {
        assertEquals(
            Car.class,
            Reflection.createInstance("net.glxn.mirrah.ReflectionTest$Car").getClass()
        );
    }

    @Test
    public void creates_instance_using_class() {
        assertEquals(
            Car.class,
            Reflection.createInstance(Car.class).getClass()
        );
    }

    @Test
    public void finds_fields_in_list_of_classes() {
        List<String> fieldNames = toFieldNames(
            Reflection.fields(asList(Car.class, Motorcycle.class))
        );

        assertEquals(3, fieldNames.size());
        assertTrue(fieldNames.contains("doors"));
        assertTrue(fieldNames.contains("cc"));
    }

    @Test
    public void finds_fields_in_class_without_inheritance() {
        List<String> fieldNames = toFieldNames(
            Reflection.fields(Car.class, false)
        );

        assertEquals(2, fieldNames.size());
        assertTrue(fieldNames.contains("doors"));
    }

    @Test
    public void finds_fields_in_class_with_inheritance() {
        List<String> fieldNames = toFieldNames(
            Reflection.fields(Car.class, true)
        );

        assertEquals(3, fieldNames.size());
        assertTrue(fieldNames.contains("doors"));
        assertTrue(fieldNames.contains("wheels"));
        assertTrue(fieldNames.contains("engine"));
    }

    @Test
    public void finds_fields_with_annotation() {
        List<Field> fields = Reflection.fieldsWithAnnotation(XmlElement.class, Car.class);

        assertEquals(1, fields.size());
        assertEquals("wheels", fields.get(0).getName());
    }

    @Test
    public void gets_annotation_on_class() {
        XmlSeeAlso annotation = Reflection.getAnnotationOnClass(XmlSeeAlso.class, Car.class);

        assertNotNull(annotation);
        assertArrayEquals(new Class[]{ReflectionTest.class}, annotation.value());
    }

    @Test
    public void gets_annotation_on_field() {
        XmlElement annotation = Reflection.getAnnotationOnField(XmlElement.class, Car.class);

        assertNotNull(annotation);
        assertEquals("number_of_wheels", annotation.name());
        assertEquals(true, annotation.nillable());
    }

    @Test
    public void gets_declared_field() {
        Field wheels = Reflection.getDeclaredField(Car.class, "wheels");

        assertNotNull(wheels);
        assertEquals("wheels", wheels.getName());
    }

    @Test
    public void has_declared_field() {
        assertTrue(Reflection.hasDeclaredField(Car.class, "wheels"));
        assertFalse(Reflection.hasDeclaredField(Car.class, "cc"));
    }

    @Test
    public void sets_value_on_field() {
        Car car = new Car();

        Reflection.setValueOnField("wheels", 4, car);

        assertEquals(car.wheels, 4);
    }

    @Test
    public void gets_value_from_field() {
        Car car = new Car();
        car.wheels = 4;

        assertEquals(
            4,
            Reflection.getValueFromField("wheels", car)
        );
    }

    @Test(expected = ReflectionException.class)
    public void get_value_from_non_existant_field_throws() {
        Reflection.getValueFromField("wings", new Car());
    }

    @Test
    public void gets_value_from_field_recursively() {
        Car car = new Car();
        Engine engine = new Engine();
        Aspiration aspiration = new Aspiration();
        aspiration.type = NATURAL;
        engine.aspiration = aspiration;
        engine.cylinders = 8;
        car.engine = engine;

        assertEquals(
            NATURAL,
            Reflection.getValueFromField("engine.aspiration.type", car)
        );
        assertEquals(
            8,
            Reflection.getValueFromField("engine.cylinders", car)
        );
    }

    @Test(expected = ReflectionException.class)
    public void gets_value_from_field_recursively_throws_if_field_does_not_exist() {
        Car car = new Car();
        car.engine = new Engine();

        assertEquals(
            null,
            Reflection.getValueFromField("engine.foo", car)
        );
    }


    private List<String> toFieldNames(List<Field> fields) {
        return fields.stream().map(Field::getName).collect(Collectors.toList());
    }

    // TEST CLASSES
    @XmlSeeAlso(ReflectionTest.class)
    @SuppressWarnings("unused")
    static class Vehicle {

        @XmlElement(name = "number_of_wheels", nillable = true)
        int wheels;
    }

    @SuppressWarnings("unused")
    static class Car extends Vehicle {
        Engine engine;
        String doors;
    }

    @SuppressWarnings("unused")
    static class Motorcycle extends Vehicle {

        String cc;
    }

    static class Engine {
        int cylinders;
        Aspiration aspiration;
    }

    static class Aspiration {
        AspirationType type;
    }

    @SuppressWarnings("unused")
    enum AspirationType {
        NATURAL, TURBOCHARGED, SUPERCHARGED
    }
}
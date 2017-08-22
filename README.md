[![Release](https://img.shields.io/github/tag/kenglxn/mirrah.svg?label=JitPack)](https://jitpack.io/#kenglxn/mirrah)

# mirrah
Mirrah is a small helper utility for using Java reflection.

## Get it

Mirrah is available through [jitpack](https://jitpack.io/#kenglxn/mirrah/1.0.0):

Add repo:
```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

And dependency:

```xml
    <dependencies>
      <dependency>
        <groupId>com.github.kenglxn</groupId>
        <artifactId>mirrah</artifactId>
        <version>1.0.0</version>
      </dependency>
    </dependencies>
```

## Usage

```java
// Assuming a codebase with the following objects:
//    Object
//      |
//    Vehicle
//    |     |
//   Car  Motorcycle
//   
// Get a list of all classes in a hierarchy given a class.
// will return [Car.class, Vehicle.class, Object.class] as a list
List classes = Reflection.hierarchy(Car.class)

// Create an instance given class
// uses the default constructor, overriding accessibility if necessary
Car newCar = Reflection.createInstance(Car.class)

// Create an instance given the FQDN of a class
// Loads class from system class loader before delegating to createInstance(Class)
Car newCar = Reflection.createInstance("net.glxn.Car")

// Get all fields in a given class
List<Field> fields = Reflection.fields(Car.class)

// Get all fields in a given class including all inherited fields from superclasses
List<Field> fields = Reflection.fields(Car.class, true)
List<Fields> fields = Reflection.fields(Reflection.hierarchy(Car.class)) // semantically the same

// Get all fields in a class hierarchy with a given annotation
List<Field> fields = Reflection.fieldsWithAnnotation(SomeAnnotation.class, Car.class)
List<Field> fields = Reflection.fieldsWithAnnotation(SomeAnnotation.class, Reflection.hierarchy(Car.class)) // semantically the same

// If you need to inspect the actual annotation values you can:
// Get an annotation defined on class level.
SomeAnnotation annotation = Reflection.getAnnotationOnClass(SomeAnnotation.class, Car.class)
annotation.value() // the value for the annotation
annotation.foo() // custom annotation properties are also accessible
// or:
// Get an annotation defined on field level.
SomeAnnotation annotation = Reflection.getAnnotationOnField(SomeAnnotation.class, Car.class)
annotation.value() // the value for the annotation
annotation.foo() // custom annotation properties are also accessible

// Check if a class has a declared field in its hierarchy
Reflection.hasDeclaredField(Car.class, "wheels") // true
Reflection.hasDeclaredField(Car.class, "wings") // false

// Get the declared field of a class
Field wheels = Reflection.getDeclaredField(Car.class, "wheels")
Field wings = Reflection.getDeclaredField(Car.class, "wings") // throws ReflectionException

// Get the value of a field on an object instance (also works for private fields)
Integer wheels = (Integer) Reflection.getValueFromField("wheels", car)
// same as
Reflection.getValueFromField(Reflection.getDeclaredField(Car.class, "wheels"), car)

// Set the value of a field on an object instance (also works for private fields)
Reflection.setValueOnField("wheels", 4, car)
// same as
Reflection.setValueOnField(Reflection.getDeclaredField(Car.class, "wheels"), 4, car)


```


see: [Reflection.java](./src/main/java/net/glxn/mirrah/Reflection.java)

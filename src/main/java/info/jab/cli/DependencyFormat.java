package info.jab.cli;

/**
 * Represents the different formats available for dependency declarations.
 */
public enum DependencyFormat {
    /**
     * Maven dependency format using XML syntax
     */
    MAVEN,
    
    /**
     * Gradle dependency format using Kotlin syntax
     */
    GRADLE,
    
    /**
     * Gradle dependency format using Kotlin script syntax
     */
    GRADLEKTS,
    
    /**
     * Gradle dependency format using Groovy syntax
     */
    GRADLEGROOVY,
    
    /**
     * SBT (Scala Build Tool) dependency format
     */
    SBT
} 
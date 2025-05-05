/**
 * Junita Sirait - COS583
 * 
 * Constraint interface to be implemented by all constraints. 
 * 
 * TODO: `compareTo` could be beneficial to allow us to prioritize some constraints over others. 
 * I also need to be consistent about what sqError unit -- pixels? 
 */
public interface Constraint { 
    void apply(); 
    double error(); 
}
